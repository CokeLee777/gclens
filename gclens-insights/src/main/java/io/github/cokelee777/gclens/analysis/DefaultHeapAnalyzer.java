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

import io.github.cokelee777.gclens.model.GCEvent;
import io.github.cokelee777.gclens.model.MemorySize;
import io.github.cokelee777.gclens.report.HeapStats;
import io.github.cokelee777.gclens.report.HeapTrend;
import java.util.List;
import java.util.Objects;

/** Default {@link HeapAnalyzer} implementation based on post-GC heap occupancy. */
public class DefaultHeapAnalyzer implements HeapAnalyzer {

  private static final double TREND_THRESHOLD = 0.10;

  /** Creates a new heap analyzer. */
  public DefaultHeapAnalyzer() {}

  @Override
  public HeapStats analyze(List<GCEvent> events) {
    Objects.requireNonNull(events, "events must not be null");
    if (events.isEmpty()) {
      throw new IllegalArgumentException("events must not be empty");
    }

    long avgUsageBytes =
        (long) events.stream().mapToLong(e -> e.heapAfter().bytes()).average().orElse(0);
    long avgCommittedBytes =
        (long) events.stream().mapToLong(e -> e.heapTotal().bytes()).average().orElse(0);
    long peakUsageBytes = events.stream().mapToLong(e -> e.heapAfter().bytes()).max().orElse(0);
    long peakCommittedBytes = events.stream().mapToLong(e -> e.heapTotal().bytes()).max().orElse(0);

    HeapTrend trend = computeTrend(events);

    return new HeapStats(
        new MemorySize(avgUsageBytes),
        new MemorySize(avgCommittedBytes),
        new MemorySize(peakUsageBytes),
        new MemorySize(peakCommittedBytes),
        trend);
  }

  private HeapTrend computeTrend(List<GCEvent> events) {
    int count = events.size();
    int quartileSize = Math.max(1, count / 4);

    double firstQ =
        events.subList(0, quartileSize).stream()
            .mapToDouble(e -> (double) e.heapAfter().bytes() / e.heapTotal().bytes())
            .average()
            .orElse(0);

    double lastQ =
        events.subList(count - quartileSize, count).stream()
            .mapToDouble(e -> (double) e.heapAfter().bytes() / e.heapTotal().bytes())
            .average()
            .orElse(0);

    double diff = lastQ - firstQ;
    if (diff > TREND_THRESHOLD) return HeapTrend.INCREASING;
    if (diff < -TREND_THRESHOLD) return HeapTrend.DECREASING;
    return HeapTrend.STABLE;
  }
}
