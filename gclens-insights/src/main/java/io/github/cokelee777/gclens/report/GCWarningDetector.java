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

import java.util.List;

/** Produces warnings from computed GC metrics. */
public interface GCWarningDetector {

  /**
   * Detects warnings from the aggregated analysis results.
   *
   * @param summary summary metrics for the log
   * @param pause pause statistics
   * @param heap heap statistics
   * @param throughput throughput ratio in the range {@code 0.0} to {@code 1.0}
   * @return detected warnings
   */
  List<GCWarning> detect(Summary summary, PauseStats pause, HeapStats heap, double throughput);
}
