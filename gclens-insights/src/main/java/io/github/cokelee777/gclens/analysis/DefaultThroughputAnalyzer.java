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
import java.time.Duration;
import java.util.List;
import java.util.Objects;

/** Default {@link ThroughputAnalyzer} implementation based on total GC pause time. */
public class DefaultThroughputAnalyzer implements ThroughputAnalyzer {

  /** Creates a new throughput analyzer. */
  public DefaultThroughputAnalyzer() {}

  @Override
  public double analyze(List<GCEvent> events) {
    Objects.requireNonNull(events, "events must not be null");
    if (events.isEmpty()) {
      throw new IllegalArgumentException("events must not be empty");
    }

    GCEvent first = events.getFirst();
    GCEvent last = events.getLast();

    Duration totalTime = last.timestamp().plus(last.duration()).minus(first.timestamp());
    Duration totalPause =
        events.stream().map(GCEvent::duration).reduce(Duration.ZERO, Duration::plus);

    long totalNanos = totalTime.toNanos();
    if (totalNanos == 0) return 0.0;

    return (double) (totalNanos - totalPause.toNanos()) / totalNanos;
  }
}
