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
package io.github.cokelee777.gclens.cli.output;

import io.github.cokelee777.gclens.model.MemorySize;
import io.github.cokelee777.gclens.report.GCReport;
import io.github.cokelee777.gclens.report.GCReporter;
import io.github.cokelee777.gclens.report.GCWarning;
import io.github.cokelee777.gclens.report.HeapStats;
import io.github.cokelee777.gclens.report.PauseStats;
import io.github.cokelee777.gclens.report.Summary;
import java.time.Duration;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Writes a human-readable GC report to standard output. */
public class CliReporter implements GCReporter {

  /** Creates a new reporter that logs formatted output at INFO level. */
  public CliReporter() {}

  private static final Logger log = LoggerFactory.getLogger(CliReporter.class);
  private static final String SEPARATOR = "═".repeat(51);

  @Override
  public void report(GCReport report) {
    log.info("{}", formatReportText(report));
  }

  /** Formats {@code report} using the same layout as {@link #report}, without logging. */
  public String formatReportText(GCReport report) {
    StringBuilder output = new StringBuilder();
    appendLine(output, SEPARATOR);
    appendLine(output, " GCLens Report");
    appendLine(output, SEPARATOR);
    appendLine(output, "");
    printSummary(output, report.summary());
    printPause(output, report.pause());
    printThroughput(output, report.throughput());
    printHeap(output, report.heap());
    printWarnings(output, report.warnings());
    printParseWarnings(output, report.parseWarnings());
    appendLine(output, SEPARATOR);
    return output.toString();
  }

  private void printSummary(StringBuilder output, Summary s) {
    appendLine(output, "[Summary]");
    appendLine(output, "  Total GC Events  : %,d".formatted(s.totalEvents()));
    appendLine(output, "  Total Pause Time : %s".formatted(formatMs(s.totalPauseTime())));
    appendLine(output, "  Analysis Period  : %s".formatted(formatSeconds(s.analysisPeriod())));
    appendLine(
        output,
        "  GC Types         : YoungGC(%,d)  MixedGC(%,d)  FullGC(%,d)"
            .formatted(s.youngGcCount(), s.mixedGcCount(), s.fullGcCount()));
    appendLine(output, "  JDK Version      : %s".formatted(s.jdkVersion()));
    appendLine(output, "");
  }

  private void printPause(StringBuilder output, PauseStats p) {
    appendLine(output, "[Pause Time]");
    appendLine(output, "  Min     : %s".formatted(formatMillis(p.min())));
    appendLine(output, "  Average : %s".formatted(formatMillis(p.avg())));
    appendLine(output, "  p95     : %s".formatted(formatMillis(p.p95())));
    appendLine(output, "  p99     : %s".formatted(formatMillis(p.p99())));
    appendLine(output, "  Max     : %s".formatted(formatMillis(p.max())));
    appendLine(output, "");
  }

  private void printThroughput(StringBuilder output, double throughput) {
    appendLine(output, "[Throughput]");
    appendLine(output, "  %.2f%%".formatted(throughput * 100));
    appendLine(output, "");
  }

  private void printHeap(StringBuilder output, HeapStats h) {
    appendLine(output, "[Heap Usage]");
    appendLine(output, "  Average : %s".formatted(formatHeapRatio(h.avgUsage(), h.avgCommitted())));
    appendLine(
        output, "  Peak    : %s".formatted(formatHeapRatio(h.peakUsage(), h.peakCommitted())));
    appendLine(
        output,
        "  Trend   : %s"
            .formatted(
                switch (h.trend()) {
                  case INCREASING -> "INCREASING ↑";
                  case DECREASING -> "DECREASING ↓";
                  case STABLE -> "STABLE →";
                }));
    appendLine(output, "");
  }

  private void printWarnings(StringBuilder output, List<GCWarning> warnings) {
    if (warnings.isEmpty()) return;
    appendLine(output, "[Warnings]");
    for (GCWarning w : warnings) {
      appendLine(output, "  ⚠ [%s] %s : %s".formatted(w.severity(), w.type(), w.message()));
    }
    appendLine(output, "");
  }

  private void printParseWarnings(StringBuilder output, List<String> parseWarnings) {
    if (parseWarnings.isEmpty()) return;
    appendLine(output, "[Parse Warnings]");
    appendLine(output, "  %,d line(s) could not be parsed.".formatted(parseWarnings.size()));
    appendLine(output, "");
  }

  private static void appendLine(StringBuilder output, String line) {
    output.append(line).append(System.lineSeparator());
  }

  private static String formatMillis(Duration d) {
    return String.format("%.2f ms", d.toNanos() / 1_000_000.0);
  }

  private static String formatMs(Duration d) {
    return String.format("%,.0f ms", d.toNanos() / 1_000_000.0);
  }

  private static String formatSeconds(Duration d) {
    return String.format("%.3f s", d.toMillis() / 1000.0);
  }

  private static String formatHeapRatio(MemorySize usage, MemorySize total) {
    long usageMB = usage.toMegabytes();
    long totalMB = total.toMegabytes();
    double pct = totalMB > 0 ? 100.0 * usageMB / totalMB : 0;
    return String.format("%,d MB / %,d MB (%.1f%%)", usageMB, totalMB, pct);
  }
}
