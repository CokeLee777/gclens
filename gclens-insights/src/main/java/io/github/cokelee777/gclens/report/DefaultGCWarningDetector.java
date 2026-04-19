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

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Default {@link GCWarningDetector} implementation using threshold-based heuristics. */
public class DefaultGCWarningDetector implements GCWarningDetector {

  /** Creates a new warning detector with default thresholds. */
  public DefaultGCWarningDetector() {}

  private static final Duration HIGH_PAUSE_THRESHOLD = Duration.ofMillis(500);
  private static final double LOW_THROUGHPUT_THRESHOLD = 0.95;
  private static final double HEAP_EXHAUSTION_THRESHOLD = 0.90;

  @Override
  public List<GCWarning> detect(
      Summary summary, PauseStats pause, HeapStats heap, double throughput) {
    Objects.requireNonNull(summary, "summary must not be null");
    Objects.requireNonNull(pause, "pause must not be null");
    Objects.requireNonNull(heap, "heap must not be null");

    List<GCWarning> warnings = new ArrayList<>();

    if (pause.p99().compareTo(HIGH_PAUSE_THRESHOLD) > 0) {
      warnings.add(
          new GCWarning(
              GCWarningType.HIGH_PAUSE_TIME,
              Severity.HIGH,
              "p99 pause "
                  + pause.p99().toMillis()
                  + " ms exceeds threshold ("
                  + HIGH_PAUSE_THRESHOLD.toMillis()
                  + " ms). Consider tuning -XX:MaxGCPauseMillis."));
    }

    if (throughput < LOW_THROUGHPUT_THRESHOLD) {
      warnings.add(
          new GCWarning(
              GCWarningType.LOW_THROUGHPUT,
              Severity.HIGH,
              String.format(
                  "Throughput %.1f%% is below threshold (%.0f%%). GC overhead is too high.",
                  throughput * 100, LOW_THROUGHPUT_THRESHOLD * 100)));
    }

    long peakUsed = heap.peakUsage().bytes();
    long peakTotal = heap.peakCommitted().bytes();
    if (peakTotal > 0 && (double) peakUsed / peakTotal > HEAP_EXHAUSTION_THRESHOLD) {
      warnings.add(
          new GCWarning(
              GCWarningType.HEAP_EXHAUSTION,
              Severity.HIGH,
              String.format(
                  "Heap usage reached %.1f%% of committed size. Consider increasing -Xmx.",
                  100.0 * peakUsed / peakTotal)));
    }

    long analysisSeconds = summary.analysisPeriod().toSeconds();
    if (analysisSeconds > 0) {
      long threshold = analysisSeconds / 720;
      if (summary.fullGcCount() > threshold) {
        warnings.add(
            new GCWarning(
                GCWarningType.FREQUENT_FULL_GC,
                Severity.HIGH,
                "Full GC occurred "
                    + summary.fullGcCount()
                    + " times — more than 5/hour. Check for memory leaks or heap misconfiguration."));
      }
    }

    return List.copyOf(warnings);
  }
}
