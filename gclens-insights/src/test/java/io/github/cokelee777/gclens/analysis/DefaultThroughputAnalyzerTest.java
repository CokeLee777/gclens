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
import static org.assertj.core.data.Offset.offset;

import io.github.cokelee777.gclens.model.GCEvent;
import io.github.cokelee777.gclens.model.GCType;
import io.github.cokelee777.gclens.model.MemorySize;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link DefaultThroughputAnalyzer}. */
class DefaultThroughputAnalyzerTest {

  private final DefaultThroughputAnalyzer analyzer = new DefaultThroughputAnalyzer();

  @Test
  void analyze_lowGcOverhead_returnsHighThroughput() {
    // totalTime = 99.5s, totalPause = 1s → throughput ≈ 0.990
    List<GCEvent> events =
        List.of(
            eventAt(Duration.ofSeconds(0), Duration.ofMillis(500)),
            eventAt(Duration.ofSeconds(99), Duration.ofMillis(500)));

    assertThat(analyzer.analyze(events)).isCloseTo(0.99, offset(0.001));
  }

  @Test
  void analyze_highGcOverhead_returnsLowThroughput() {
    // totalTime = 10.5s, totalPause = 5s → throughput ≈ 0.524
    List<GCEvent> events =
        List.of(
            eventAt(Duration.ofSeconds(0), Duration.ofSeconds(2)),
            eventAt(Duration.ofSeconds(3), Duration.ofSeconds(2)),
            eventAt(Duration.ofSeconds(8), Duration.ofSeconds(1)));

    assertThat(analyzer.analyze(events)).isLessThan(0.60);
  }

  @Test
  void analyze_singleEvent_returnsZero() {
    List<GCEvent> events = List.of(eventAt(Duration.ZERO, Duration.ofMillis(100)));
    // totalTime = 0.1s, totalPause = 0.1s → throughput = 0
    assertThat(analyzer.analyze(events)).isCloseTo(0.0, offset(0.001));
  }

  private static GCEvent eventAt(Duration timestamp, Duration duration) {
    return new StubEvent(timestamp, duration);
  }

  private record StubEvent(Duration timestamp, Duration duration) implements GCEvent {
    @Override
    public GCType gcType() {
      return GCType.YOUNG_GC;
    }

    @Override
    public MemorySize heapBefore() {
      return MemorySize.ofMegabytes(512);
    }

    @Override
    public MemorySize heapAfter() {
      return MemorySize.ofMegabytes(256);
    }

    @Override
    public MemorySize heapTotal() {
      return MemorySize.ofMegabytes(1024);
    }
  }
}
