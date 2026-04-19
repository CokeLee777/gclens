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
package io.github.cokelee777.gclens.analysis;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.cokelee777.gclens.model.GCEvent;
import io.github.cokelee777.gclens.model.GCType;
import io.github.cokelee777.gclens.model.MemorySize;
import io.github.cokelee777.gclens.report.HeapStats;
import io.github.cokelee777.gclens.report.HeapTrend;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link DefaultHeapAnalyzer}. */
class DefaultHeapAnalyzerTest {

  private final DefaultHeapAnalyzer analyzer = new DefaultHeapAnalyzer();

  @Test
  void analyze_increasingHeap_returnsTrendIncreasing() {
    List<GCEvent> events =
        List.of(
            heapEvent(0, MemorySize.ofMegabytes(100)),
            heapEvent(1, MemorySize.ofMegabytes(150)),
            heapEvent(2, MemorySize.ofMegabytes(200)),
            heapEvent(3, MemorySize.ofMegabytes(300)));

    assertThat(analyzer.analyze(events).trend()).isEqualTo(HeapTrend.INCREASING);
  }

  @Test
  void analyze_stableHeap_returnsTrendStable() {
    List<GCEvent> events =
        List.of(
            heapEvent(0, MemorySize.ofMegabytes(250)),
            heapEvent(1, MemorySize.ofMegabytes(260)),
            heapEvent(2, MemorySize.ofMegabytes(255)),
            heapEvent(3, MemorySize.ofMegabytes(248)));

    assertThat(analyzer.analyze(events).trend()).isEqualTo(HeapTrend.STABLE);
  }

  @Test
  void analyze_decreasingHeap_returnsTrendDecreasing() {
    List<GCEvent> events =
        List.of(
            heapEvent(0, MemorySize.ofMegabytes(300)),
            heapEvent(1, MemorySize.ofMegabytes(200)),
            heapEvent(2, MemorySize.ofMegabytes(150)),
            heapEvent(3, MemorySize.ofMegabytes(100)));

    assertThat(analyzer.analyze(events).trend()).isEqualTo(HeapTrend.DECREASING);
  }

  @Test
  void analyze_peakUsage_returnsMaxHeapAfter() {
    List<GCEvent> events =
        List.of(
            heapEvent(0, MemorySize.ofMegabytes(300)),
            heapEvent(1, MemorySize.ofMegabytes(900)),
            heapEvent(2, MemorySize.ofMegabytes(400)));

    HeapStats stats = analyzer.analyze(events);

    assertThat(stats.peakUsage()).isEqualTo(MemorySize.ofMegabytes(900));
  }

  private static GCEvent heapEvent(int timestampSec, MemorySize heapAfter) {
    return new StubEvent(
        Duration.ofSeconds(timestampSec),
        Duration.ofMillis(10),
        MemorySize.ofMegabytes(600),
        heapAfter,
        MemorySize.ofMegabytes(512));
  }

  private record StubEvent(
      Duration timestamp,
      Duration duration,
      MemorySize heapBefore,
      MemorySize heapAfter,
      MemorySize heapTotal)
      implements GCEvent {
    @Override
    public GCType gcType() {
      return GCType.YOUNG_GC;
    }
  }
}
