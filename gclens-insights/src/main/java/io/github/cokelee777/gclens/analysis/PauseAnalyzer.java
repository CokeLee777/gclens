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
import java.util.List;

/** Computes pause-time statistics from parsed GC events. */
public interface PauseAnalyzer {

  /**
   * Analyzes pause durations from parsed events.
   *
   * @param events parsed GC events
   * @return aggregate pause statistics
   */
  PauseStats analyze(List<GCEvent> events);
}
