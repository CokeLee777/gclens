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
package io.github.cokelee777.gclens.engine;

import io.github.cokelee777.gclens.analysis.HeapAnalyzer;
import io.github.cokelee777.gclens.analysis.PauseAnalyzer;
import io.github.cokelee777.gclens.analysis.ThroughputAnalyzer;
import io.github.cokelee777.gclens.model.GCEvent;
import io.github.cokelee777.gclens.parse.GCLogParseException;
import io.github.cokelee777.gclens.parse.GCLogParser;
import io.github.cokelee777.gclens.parse.GCLogParserFactory;
import io.github.cokelee777.gclens.parse.ParsedLog;
import io.github.cokelee777.gclens.report.GCReport;
import io.github.cokelee777.gclens.report.GCWarning;
import io.github.cokelee777.gclens.report.GCWarningDetector;
import io.github.cokelee777.gclens.report.HeapStats;
import io.github.cokelee777.gclens.report.PauseStats;
import io.github.cokelee777.gclens.report.Summary;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

/** High-level service that parses a GC log and computes a report. */
public class GCLogAnalyzer {

  private final GCLogParserFactory parserFactory;
  private final PauseAnalyzer pauseAnalyzer;
  private final HeapAnalyzer heapAnalyzer;
  private final ThroughputAnalyzer throughputAnalyzer;
  private final GCWarningDetector warningDetector;

  /**
   * Creates an analyzer with the supplied collaborators.
   *
   * @param parserFactory factory that selects a {@link GCLogParser} for the log file
   * @param pauseAnalyzer pause statistics calculator
   * @param heapAnalyzer heap statistics calculator
   * @param throughputAnalyzer throughput calculator
   * @param warningDetector heuristic warning detector
   */
  public GCLogAnalyzer(
      GCLogParserFactory parserFactory,
      PauseAnalyzer pauseAnalyzer,
      HeapAnalyzer heapAnalyzer,
      ThroughputAnalyzer throughputAnalyzer,
      GCWarningDetector warningDetector) {
    this.parserFactory = parserFactory;
    this.pauseAnalyzer = pauseAnalyzer;
    this.heapAnalyzer = heapAnalyzer;
    this.throughputAnalyzer = throughputAnalyzer;
    this.warningDetector = warningDetector;
  }

  /**
   * Parses the log at {@code logPath} and builds a {@link GCReport}.
   *
   * @param logPath path to the GC log file
   * @return the aggregated analysis result
   * @throws GCLogParseException if the file cannot be read or parsed
   */
  public GCReport analyze(Path logPath) throws GCLogParseException {
    Objects.requireNonNull(logPath, "logPath must not be null");

    GCLogParser parser = parserFactory.createFor(logPath);
    ParsedLog parsed = parser.parse(logPath);
    List<GCEvent> events = parsed.events();

    PauseStats pause = pauseAnalyzer.analyze(events);
    HeapStats heap = heapAnalyzer.analyze(events);
    double throughput = throughputAnalyzer.analyze(events);
    Summary summary = buildSummary(events, parsed);

    List<GCWarning> warnings = warningDetector.detect(summary, pause, heap, throughput);

    return new GCReport(summary, pause, heap, throughput, warnings, parsed.parseWarnings());
  }

  private Summary buildSummary(List<GCEvent> events, ParsedLog parsed) {
    long youngCount = 0;
    long mixedCount = 0;
    long fullCount = 0;
    for (GCEvent e : events) {
      switch (e.gcType()) {
        case YOUNG_GC -> youngCount++;
        case MIXED_GC -> mixedCount++;
        case FULL_GC -> fullCount++;
      }
    }

    Duration totalPause =
        events.stream().map(GCEvent::duration).reduce(Duration.ZERO, Duration::plus);

    GCEvent first = events.getFirst();
    GCEvent last = events.getLast();
    Duration analysisPeriod = last.timestamp().plus(last.duration()).minus(first.timestamp());

    return new Summary(
        events.size(),
        totalPause,
        analysisPeriod,
        youngCount,
        mixedCount,
        fullCount,
        parsed.jdkVersion());
  }
}
