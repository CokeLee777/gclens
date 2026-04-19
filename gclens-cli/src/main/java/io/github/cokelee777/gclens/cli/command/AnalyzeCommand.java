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
import io.github.cokelee777.gclens.parse.GCLogParseException;
import io.github.cokelee777.gclens.report.DefaultGCWarningDetector;
import io.github.cokelee777.gclens.report.GCReport;
import io.github.cokelee777.gclens.report.GCReporter;
import java.nio.file.Path;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/** Picocli command that analyzes a single GC log file. */
@Command(name = "analyze", description = "Analyze a GC log file and print a report.")
public class AnalyzeCommand implements Runnable {

  private static final Logger log = LoggerFactory.getLogger(AnalyzeCommand.class);

  @Parameters(
      index = "0",
      description = "Path to the GC log file",
      defaultValue = "gc.log",
      showDefaultValue = CommandLine.Help.Visibility.ALWAYS)
  @Nullable
  private Path logPath;

  @Option(names = "--verbose", description = "Print unparseable lines in detail")
  private boolean verbose;

  private final GCLogAnalyzer analyzer =
      new GCLogAnalyzer(
          new ServiceLoaderGCLogParserFactory(),
          new DefaultPauseAnalyzer(),
          new DefaultHeapAnalyzer(),
          new DefaultThroughputAnalyzer(),
          new DefaultGCWarningDetector());
  private final GCReporter reporter = new CliReporter();

  /**
   * Creates a command with explicit options (primarily for tests; Picocli uses the no-arg
   * constructor).
   *
   * @param logPath path to the GC log file, or {@code null} to use the default
   * @param verbose whether to print unparseable lines in detail
   */
  public AnalyzeCommand(@Nullable Path logPath, boolean verbose) {
    this.logPath = logPath;
    this.verbose = verbose;
  }

  @Override
  public void run() {
    try {
      GCReport report = analyzer.analyze(logPath);
      reporter.report(report);
      if (verbose && !report.parseWarnings().isEmpty()) {
        log.info("[Unparseable Lines]");
        report.parseWarnings().forEach(line -> log.info("  {}", line));
      }
    } catch (GCLogParseException e) {
      log.error("Error: {}", e.getMessage());
      System.exit(1);
    }
  }
}
