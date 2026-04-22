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
import io.github.cokelee777.gclens.report.PauseStats;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/** Default {@link PauseAnalyzer} implementation using percentile calculations. */
public class DefaultPauseAnalyzer implements PauseAnalyzer {

  /** Creates a new pause analyzer. */
  public DefaultPauseAnalyzer() {}

  @Override
  public PauseStats analyze(List<GCEvent> events) {
    Objects.requireNonNull(events, "events must not be null");
    if (events.isEmpty()) {
      throw new IllegalArgumentException("events must not be empty");
    }

    long[] nanos = events.stream().mapToLong(e -> e.duration().toNanos()).sorted().toArray();

    int count = nanos.length;
    long sum = Arrays.stream(nanos).sum();

    return new PauseStats(
        Duration.ofNanos(nanos[0]),
        Duration.ofNanos(sum / count),
        Duration.ofNanos(nanos[(int) ((count - 1) * 0.95)]),
        Duration.ofNanos(nanos[(int) ((count - 1) * 0.99)]),
        Duration.ofNanos(nanos[count - 1]));
  }
}
