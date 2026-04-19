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
package io.github.cokelee777.gclens.g1gc.parse;

import io.github.cokelee777.gclens.model.GCEvent;
import io.github.cokelee777.gclens.model.GCLogVersion;
import io.github.cokelee777.gclens.parse.GCLogLineParser;
import io.github.cokelee777.gclens.parse.GCLogParseException;
import io.github.cokelee777.gclens.parse.GCLogParser;
import io.github.cokelee777.gclens.parse.ParseResult;
import io.github.cokelee777.gclens.parse.ParsedLog;
import io.github.cokelee777.gclens.utils.GCLogVersionDetector;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Parses G1 GC log files into structured events. */
public class G1GCParser implements GCLogParser {

  private static final int HEADER_LINES = 20;

  private final GCLogVersionDetector versionDetector;
  private final GCLogLineParser youngParser;
  private final GCLogLineParser mixedParser;
  private final GCLogLineParser fullParser;

  /** Creates a parser using default line parsers and a {@link GCLogVersionDetector}. */
  public G1GCParser() {
    this(
        new GCLogVersionDetector(),
        new G1YoungGCLineParser(),
        new G1MixedGCLineParser(),
        new G1FullGCLineParser());
  }

  /**
   * Creates a parser with the supplied collaborators (useful for testing).
   *
   * @param versionDetector detector for JDK log format version from header lines
   * @param youngParser parser for young GC lines
   * @param mixedParser parser for mixed GC lines
   * @param fullParser parser for full GC lines
   */
  public G1GCParser(
      GCLogVersionDetector versionDetector,
      GCLogLineParser youngParser,
      GCLogLineParser mixedParser,
      GCLogLineParser fullParser) {
    this.versionDetector = versionDetector;
    this.youngParser = youngParser;
    this.mixedParser = mixedParser;
    this.fullParser = fullParser;
  }

  @Override
  public ParsedLog parse(Path logPath) throws GCLogParseException {
    Objects.requireNonNull(logPath, "logPath must not be null");

    List<GCEvent> events = new ArrayList<>();
    List<String> parseWarnings = new ArrayList<>();
    List<String> headerBuffer = new ArrayList<>(HEADER_LINES);
    GCLogVersion version = GCLogVersion.UNKNOWN;

    try (BufferedReader reader = Files.newBufferedReader(logPath)) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (headerBuffer.size() < HEADER_LINES) {
          headerBuffer.add(line);
          if (headerBuffer.size() == HEADER_LINES) {
            version = versionDetector.detect(headerBuffer);
          }
        }
        switch (dispatchLine(line)) {
          case ParseResult.Success s -> events.add(s.event());
          case ParseResult.Warn w -> parseWarnings.add(w.line());
          case ParseResult.Skip ignored -> {}
        }
      }
      if (headerBuffer.size() < HEADER_LINES) {
        version = versionDetector.detect(headerBuffer);
      }
    } catch (IOException e) {
      throw new GCLogParseException("Failed to read log file: " + logPath, e);
    }

    if (events.isEmpty()) {
      throw new GCLogParseException("No GC events found in the log file.");
    }

    return new ParsedLog(version, List.copyOf(events), List.copyOf(parseWarnings));
  }

  /** Returns true if the file contains G1 GC log patterns (checks first 50 lines). */
  @Override
  public boolean supports(Path logPath) {
    if (!Files.isReadable(logPath)) return false;
    try (BufferedReader reader = Files.newBufferedReader(logPath)) {
      String line;
      int checked = 0;
      while ((line = reader.readLine()) != null && checked < 50) {
        if (line.contains("[gc]")
            && (line.contains("Pause Young")
                || line.contains("Pause Mixed")
                || line.contains("Pause Full"))) {
          return true;
        }
        checked++;
      }
    } catch (IOException e) {
      return false;
    }
    return false;
  }

  private ParseResult dispatchLine(String line) {
    if (!line.contains("[gc]")) {
      return new ParseResult.Skip();
    }
    if (line.contains("Pause Young")) return youngParser.parse(line);
    if (line.contains("Pause Mixed")) return mixedParser.parse(line);
    if (line.contains("Pause Full")) return fullParser.parse(line);
    return new ParseResult.Skip();
  }
}
