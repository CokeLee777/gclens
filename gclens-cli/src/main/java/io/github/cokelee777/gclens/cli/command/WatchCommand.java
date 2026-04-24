/*
 * Copyright 2026-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.cokelee777.gclens.cli.command;

import io.github.cokelee777.gclens.analysis.DefaultHeapAnalyzer;
import io.github.cokelee777.gclens.analysis.DefaultPauseAnalyzer;
import io.github.cokelee777.gclens.analysis.DefaultThroughputAnalyzer;
import io.github.cokelee777.gclens.cli.output.CliReporter;
import io.github.cokelee777.gclens.engine.GCLogAnalyzer;
import io.github.cokelee777.gclens.engine.ServiceLoaderGCLogParserFactory;
import io.github.cokelee777.gclens.model.GCEvent;
import io.github.cokelee777.gclens.parse.GCLogParseException;
import io.github.cokelee777.gclens.parse.GCLogParser;
import io.github.cokelee777.gclens.parse.GCLogParserFactory;
import io.github.cokelee777.gclens.parse.GCTailParser;
import io.github.cokelee777.gclens.parse.ParsedLog;
import io.github.cokelee777.gclens.parse.TailSessionState;
import io.github.cokelee777.gclens.parse.TailTickResult;
import io.github.cokelee777.gclens.report.DefaultGCWarningDetector;
import io.github.cokelee777.gclens.report.GCReport;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/** Watches a GC log file and prints an updated report on each tick. */
@Command(
    name = "watch",
    description =
        "Watch a growing GC log (cumulative stats). Rotations/truncations reset the session. "
            + "Events are accumulated in memory for the lifetime of the process.")
public class WatchCommand implements Runnable {

  private static final Logger log = LoggerFactory.getLogger(WatchCommand.class);
  private static final long SLEEP_SLICE_MS = 200L;

  private static final Pattern HUMAN_INTERVAL =
      Pattern.compile("^(\\d+(?:\\.\\d+)?)\\s*(ms|s|m|h)$", Pattern.CASE_INSENSITIVE);
  private static final Pattern PLAIN_DECIMAL_SECONDS = Pattern.compile("^\\d+\\.\\d+$");

  @Parameters(
      index = "0",
      description = "Path to the GC log file",
      defaultValue = "gc.log",
      showDefaultValue = CommandLine.Help.Visibility.ALWAYS)
  @Nullable
  private Path logPath;

  @Option(
      names = "--interval",
      description =
          "Time to sleep after each tick: whole seconds (5), decimal seconds (5.0 = 5s), 5s, 1m, 200ms, 0.5h, or ISO-8601 like PT1M, PT0.5S (default: ${DEFAULT-VALUE})",
      defaultValue = "5s")
  private String intervalSpec;

  @Option(names = "--verbose", description = "Log parse warnings newly added on each tick")
  private boolean verbose;

  @Option(
      names = "--from-end",
      description = "Start at EOF: ignore historical GC events; only new appends are analyzed")
  private boolean fromEnd;

  @Option(
      names = "--no-clear",
      description = "Do not clear the screen on TTY; append each tick instead of a dashboard view")
  private boolean noClear;

  @Option(
      names = "--once",
      description = "Run a single tick (initial sync + one append) then exit (for tests/scripts)")
  private boolean once;

  private final GCLogParserFactory parserFactory;
  private final GCLogAnalyzer analyzer;
  private final CliReporter reporter;
  private final PrintStream stdout;

  private final AtomicBoolean running = new AtomicBoolean(true);

  /** Picocli entry constructor. */
  public WatchCommand() {
    this(
        new ServiceLoaderGCLogParserFactory(),
        new GCLogAnalyzer(
            new ServiceLoaderGCLogParserFactory(),
            new DefaultPauseAnalyzer(),
            new DefaultHeapAnalyzer(),
            new DefaultThroughputAnalyzer(),
            new DefaultGCWarningDetector()),
        new CliReporter(),
        System.out);
  }

  WatchCommand(
      GCLogParserFactory parserFactory,
      GCLogAnalyzer analyzer,
      CliReporter reporter,
      PrintStream stdout) {
    this.parserFactory = parserFactory;
    this.analyzer = analyzer;
    this.reporter = reporter;
    this.stdout = stdout;
  }

  @Override
  public void run() {
    Objects.requireNonNull(logPath, "logPath must not be null");
    Runtime.getRuntime().addShutdownHook(new Thread(() -> running.set(false)));

    GCLogParser parser;
    try {
      parser = parserFactory.createFor(logPath);
    } catch (GCLogParseException e) {
      log.error("Error: {}", e.getMessage());
      System.exit(1);
      return;
    }

    if (!(parser instanceof GCTailParser tail)) {
      log.error("Incremental tail parsing is not supported for this log format.");
      System.exit(1);
      return;
    }

    if (!tail.supportsTail(logPath)) {
      log.error("Tail parsing is not supported for this log file.");
      System.exit(1);
      return;
    }

    final long tickPauseMs;
    try {
      tickPauseMs = parseIntervalToMillis(intervalSpec);
    } catch (DateTimeParseException | NumberFormatException | ArithmeticException e) {
      log.error("Invalid --interval: {}", e.getMessage());
      System.exit(1);
      return;
    }
    if (tickPauseMs < 0L) {
      log.error("--interval must not be negative.");
      System.exit(1);
      return;
    }

    TailSessionState state = TailSessionState.initial();
    ArrayList<GCEvent> events = new ArrayList<>();
    ArrayList<String> warnings = new ArrayList<>();

    try {
      if (fromEnd) {
        tail.seedHeaderAndSeekEof(state, logPath);
      } else {
        ParsedLog parsed = parser.parse(logPath);
        events.addAll(parsed.events());
        warnings.addAll(parsed.parseWarnings());
        tail.syncCursorToEof(state, logPath);
      }

      BasicFileAttributes attrs0 = Files.readAttributes(logPath, BasicFileAttributes.class);
      state.setLastFileKey(attrs0.fileKey());
      state.setLastKnownSize(attrs0.size());

      boolean tty = System.console() != null && !noClear;

      do {
        BasicFileAttributes attrs = Files.readAttributes(logPath, BasicFileAttributes.class);
        if (shouldReset(state, attrs)) {
          resetSession(parser, tail, state, events, warnings);
          attrs = Files.readAttributes(logPath, BasicFileAttributes.class);
        }

        TailTickResult tick = tail.append(state, logPath);
        events.addAll(tick.newEvents());
        warnings.addAll(tick.newParseWarnings());

        if (verbose && !tick.newParseWarnings().isEmpty()) {
          tick.newParseWarnings().forEach(w -> log.info("[Unparseable] {}", w));
        }

        GCReport report =
            analyzer.buildReport(
                List.copyOf(events), state.getDetectedVersion(), List.copyOf(warnings));
        printReport(report, tty);

        BasicFileAttributes afterTick = Files.readAttributes(logPath, BasicFileAttributes.class);
        state.setLastFileKey(afterTick.fileKey());
        state.setLastKnownSize(afterTick.size());

        if (once) {
          break;
        }

        for (long remaining = tickPauseMs; remaining > 0 && running.get(); /* */ ) {
          long step = Math.min(SLEEP_SLICE_MS, remaining);
          try {
            Thread.sleep(step);
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
          }
          remaining -= step;
        }
      } while (running.get());
    } catch (GCLogParseException e) {
      log.error("Error: {}", e.getMessage());
      System.exit(1);
    } catch (IOException e) {
      log.error("IO error: {}", e.getMessage());
      System.exit(1);
    }
  }

  private static boolean shouldReset(TailSessionState state, BasicFileAttributes attrs) {
    if (state.getNextReadOffset() > attrs.size()) {
      return true;
    }
    Object prevKey = state.getLastFileKey();
    Object curKey = attrs.fileKey();
    if (prevKey != null && curKey != null && !prevKey.equals(curKey)) {
      return true;
    }
    if (state.getLastKnownSize() > 0L && attrs.size() < state.getLastKnownSize()) {
      return true;
    }
    return false;
  }

  private void resetSession(
      GCLogParser parser,
      GCTailParser tail,
      TailSessionState state,
      List<GCEvent> events,
      List<String> warnings)
      throws GCLogParseException, IOException {
    events.clear();
    warnings.clear();
    state.resetToInitial();
    ParsedLog parsed = parser.parse(logPath);
    events.addAll(parsed.events());
    warnings.addAll(parsed.parseWarnings());
    tail.syncCursorToEof(state, logPath);
    BasicFileAttributes attrs = Files.readAttributes(logPath, BasicFileAttributes.class);
    state.setLastFileKey(attrs.fileKey());
    state.setLastKnownSize(attrs.size());
  }

  private void printReport(GCReport report, boolean tty) {
    String text = reporter.formatReportText(report);
    if (tty) {
      stdout.print("\u001b[2J\u001b[H");
    } else {
      stdout.println();
      stdout.println("--- " + Instant.now() + " ---");
    }
    stdout.print(text);
    stdout.flush();
  }

  /**
   * Parses the {@code --interval} string into milliseconds. Package-private for tests. Plain digits
   * = whole seconds; otherwise ISO-8601 {@link Duration#parse} or a suffix of ms/s/m/h.
   */
  static long parseIntervalToMillis(String raw) {
    if (raw == null || raw.isBlank()) {
      return 5_000L;
    }
    String s = raw.trim();
    if (s.isEmpty()) {
      return 5_000L;
    }
    if (s.length() > 0 && (s.charAt(0) == 'P' || s.charAt(0) == 'p')) {
      if (s.charAt(0) == 'p') {
        s = "P" + s.substring(1);
      }
      return Duration.parse(s).toMillis();
    }
    if (s.chars().allMatch(Character::isDigit)) {
      return Math.multiplyExact(Long.parseLong(s), 1_000L);
    }
    if (PLAIN_DECIMAL_SECONDS.matcher(s).matches()) {
      double sec = Double.parseDouble(s);
      if (sec < 0.0) {
        throw new DateTimeParseException("negative --interval: " + s, s, 0);
      }
      return (long) Math.rint(sec * 1000.0);
    }
    return parseHumanIntervalToMillis(s);
  }

  private static long parseHumanIntervalToMillis(String s) {
    Matcher m = HUMAN_INTERVAL.matcher(s);
    if (!m.matches()) {
      throw new DateTimeParseException("unrecognized --interval: " + s, s, 0);
    }
    double v = Double.parseDouble(m.group(1));
    if (v < 0.0) {
      throw new DateTimeParseException("negative --interval: " + s, s, 0);
    }
    String u = m.group(2).toLowerCase();
    return switch (u) {
      case "ms" -> (long) Math.rint(v);
      case "s" -> (long) Math.rint(v * 1000.0);
      case "m" -> (long) Math.rint(v * 60_000.0);
      case "h" -> (long) Math.rint(v * 3_600_000.0);
      default -> throw new DateTimeParseException("bad unit: " + u, s, 0);
    };
  }
}
