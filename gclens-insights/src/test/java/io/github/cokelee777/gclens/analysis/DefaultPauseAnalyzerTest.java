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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.cokelee777.gclens.model.GCEvent;
import io.github.cokelee777.gclens.model.GCType;
import io.github.cokelee777.gclens.model.MemorySize;
import io.github.cokelee777.gclens.report.PauseStats;
import java.time.Duration;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link DefaultPauseAnalyzer}. */
class DefaultPauseAnalyzerTest {

  private final DefaultPauseAnalyzer analyzer = new DefaultPauseAnalyzer();

  @Test
  void analyze_multipleEvents_returnsCorrectStats() {
    List<GCEvent> events =
        IntStream.rangeClosed(1, 100)
            .<GCEvent>mapToObj(i -> eventWithDuration(Duration.ofMillis(i)))
            .toList();

    PauseStats stats = analyzer.analyze(events);

    assertThat(stats.avg().toMillis()).isEqualTo(50);
    assertThat(stats.p95().toMillis()).isEqualTo(95);
    assertThat(stats.p99().toMillis()).isEqualTo(99);
    assertThat(stats.max().toMillis()).isEqualTo(100);
    assertThat(stats.min().toMillis()).isEqualTo(1);
  }

  @Test
  void analyze_singleEvent_allStatsEqual() {
    List<GCEvent> events = List.of(eventWithDuration(Duration.ofMillis(42)));

    PauseStats stats = analyzer.analyze(events);

    assertThat(stats.avg()).isEqualTo(stats.p95()).isEqualTo(stats.max()).isEqualTo(stats.min());
  }

  @Test
  void analyze_emptyEvents_throwsException() {
    assertThatThrownBy(() -> analyzer.analyze(List.of()))
        .isInstanceOf(IllegalArgumentException.class);
  }

  private static GCEvent eventWithDuration(Duration duration) {
    return new StubEvent(Duration.ofSeconds(1), duration);
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
