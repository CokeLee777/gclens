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

import static org.assertj.core.api.Assertions.assertThatNoException;

import io.github.cokelee777.gclens.model.GCLogVersion;
import io.github.cokelee777.gclens.model.MemorySize;
import io.github.cokelee777.gclens.report.GCReport;
import io.github.cokelee777.gclens.report.GCWarning;
import io.github.cokelee777.gclens.report.GCWarningType;
import io.github.cokelee777.gclens.report.HeapStats;
import io.github.cokelee777.gclens.report.HeapTrend;
import io.github.cokelee777.gclens.report.PauseStats;
import io.github.cokelee777.gclens.report.Severity;
import io.github.cokelee777.gclens.report.Summary;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link CliReporter}. */
class CliReporterTest {

  private final CliReporter reporter = new CliReporter();

  @Test
  void report_normalReport_doesNotThrow() {
    assertThatNoException().isThrownBy(() -> reporter.report(normalReport()));
  }

  @Test
  void report_reportWithWarnings_doesNotThrow() {
    GCReport report =
        new GCReport(
            defaultSummary(),
            defaultPause(),
            defaultHeap(),
            0.94,
            List.of(
                new GCWarning(GCWarningType.HIGH_PAUSE_TIME, Severity.HIGH, "p99 too high"),
                new GCWarning(GCWarningType.LOW_THROUGHPUT, Severity.HIGH, "throughput low")),
            List.of());
    assertThatNoException().isThrownBy(() -> reporter.report(report));
  }

  @Test
  void report_reportWithParseWarnings_doesNotThrow() {
    GCReport report =
        new GCReport(
            defaultSummary(),
            defaultPause(),
            defaultHeap(),
            0.99,
            List.of(),
            List.of("[1.234s][info][gc] GC(0) Pause Young MALFORMED"));
    assertThatNoException().isThrownBy(() -> reporter.report(report));
  }

  @Test
  void report_increasingHeapTrend_doesNotThrow() {
    HeapStats heap =
        new HeapStats(
            MemorySize.ofMegabytes(512),
            MemorySize.ofMegabytes(1024),
            MemorySize.ofMegabytes(900),
            MemorySize.ofMegabytes(1024),
            HeapTrend.INCREASING);
    GCReport report =
        new GCReport(defaultSummary(), defaultPause(), heap, 0.99, List.of(), List.of());
    assertThatNoException().isThrownBy(() -> reporter.report(report));
  }

  @Test
  void report_decreasingHeapTrend_doesNotThrow() {
    HeapStats heap =
        new HeapStats(
            MemorySize.ofMegabytes(512),
            MemorySize.ofMegabytes(1024),
            MemorySize.ofMegabytes(600),
            MemorySize.ofMegabytes(1024),
            HeapTrend.DECREASING);
    GCReport report =
        new GCReport(defaultSummary(), defaultPause(), heap, 0.99, List.of(), List.of());
    assertThatNoException().isThrownBy(() -> reporter.report(report));
  }

  // --- helpers ---

  private GCReport normalReport() {
    return new GCReport(
        defaultSummary(), defaultPause(), defaultHeap(), 0.99, List.of(), List.of());
  }

  private Summary defaultSummary() {
    return new Summary(
        100, Duration.ofSeconds(5), Duration.ofHours(1), 95L, 5L, 0L, GCLogVersion.JDK_21);
  }

  private PauseStats defaultPause() {
    return new PauseStats(
        Duration.ofMillis(5),
        Duration.ofMillis(30),
        Duration.ofMillis(80),
        Duration.ofMillis(120),
        Duration.ofMillis(200));
  }

  private HeapStats defaultHeap() {
    return new HeapStats(
        MemorySize.ofMegabytes(512),
        MemorySize.ofMegabytes(1024),
        MemorySize.ofMegabytes(800),
        MemorySize.ofMegabytes(1024),
        HeapTrend.STABLE);
  }
}
