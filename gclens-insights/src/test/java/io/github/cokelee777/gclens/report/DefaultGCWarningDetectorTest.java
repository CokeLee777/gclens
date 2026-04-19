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
package io.github.cokelee777.gclens.report;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.cokelee777.gclens.model.GCLogVersion;
import io.github.cokelee777.gclens.model.MemorySize;
import java.time.Duration;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link DefaultGCWarningDetector}. */
class DefaultGCWarningDetectorTest {

  private final DefaultGCWarningDetector detector = new DefaultGCWarningDetector();

  @Test
  void detect_p99ExceedsThreshold_returnsHighPauseWarning() {
    PauseStats pause = pauseWithP99(Duration.ofMillis(501));
    assertThat(detector.detect(defaultSummary(), pause, defaultHeap(), 0.99))
        .extracting(GCWarning::type)
        .contains(GCWarningType.HIGH_PAUSE_TIME);
  }

  @Test
  void detect_p99AtThreshold_noHighPauseWarning() {
    PauseStats pause = pauseWithP99(Duration.ofMillis(500));
    assertThat(detector.detect(defaultSummary(), pause, defaultHeap(), 0.99))
        .extracting(GCWarning::type)
        .doesNotContain(GCWarningType.HIGH_PAUSE_TIME);
  }

  @Test
  void detect_throughputBelowThreshold_returnsLowThroughputWarning() {
    assertThat(detector.detect(defaultSummary(), defaultPause(), defaultHeap(), 0.94))
        .extracting(GCWarning::type)
        .contains(GCWarningType.LOW_THROUGHPUT);
  }

  @Test
  void detect_frequentFullGc_returnsFrequentFullGcWarning() {
    // 6 Full GC events in 1 hour: threshold = 3600/720 = 5 → 6 > 5
    Summary summary = summaryWithFullGcCount(6, Duration.ofHours(1));
    assertThat(detector.detect(summary, defaultPause(), defaultHeap(), 0.99))
        .extracting(GCWarning::type)
        .contains(GCWarningType.FREQUENT_FULL_GC);
  }

  @Test
  void detect_heapUsageExceedsThreshold_returnsHeapExhaustionWarning() {
    // 950/1024 = 92.8% > 90%
    HeapStats heap = heapWithPeak(MemorySize.ofMegabytes(950), MemorySize.ofMegabytes(1024));
    assertThat(detector.detect(defaultSummary(), defaultPause(), heap, 0.99))
        .extracting(GCWarning::type)
        .contains(GCWarningType.HEAP_EXHAUSTION);
  }

  @Test
  void detect_allMetricsNormal_returnsEmptyWarnings() {
    assertThat(detector.detect(defaultSummary(), defaultPause(), defaultHeap(), 0.99)).isEmpty();
  }

  // --- helpers ---

  private PauseStats pauseWithP99(Duration p99) {
    return new PauseStats(
        Duration.ofMillis(10),
        Duration.ofMillis(50),
        Duration.ofMillis(200),
        p99,
        Duration.ofMillis(600));
  }

  private Summary summaryWithFullGcCount(int fullGcCount, Duration analysisPeriod) {
    return new Summary(
        100 + fullGcCount,
        Duration.ofSeconds(5),
        analysisPeriod,
        100L,
        0L,
        (long) fullGcCount,
        GCLogVersion.JDK_21);
  }

  private HeapStats heapWithPeak(MemorySize peakUsage, MemorySize peakCommitted) {
    return new HeapStats(
        MemorySize.ofMegabytes(512),
        MemorySize.ofMegabytes(1024),
        peakUsage,
        peakCommitted,
        HeapTrend.STABLE);
  }

  private Summary defaultSummary() {
    return new Summary(
        100, Duration.ofSeconds(5), Duration.ofHours(1), 98L, 2L, 0L, GCLogVersion.JDK_21);
  }

  private PauseStats defaultPause() {
    return new PauseStats(
        Duration.ofMillis(10),
        Duration.ofMillis(50),
        Duration.ofMillis(150),
        Duration.ofMillis(200),
        Duration.ofMillis(300));
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
