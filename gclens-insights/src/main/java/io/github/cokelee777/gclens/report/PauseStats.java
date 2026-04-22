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

/**
 * Pause-time distribution metrics derived from GC events.
 *
 * @param min the minimum pause duration
 * @param avg the arithmetic mean pause duration
 * @param p95 the 95th percentile pause duration
 * @param p99 the 99th percentile pause duration
 * @param max the maximum pause duration
 */
public record PauseStats(Duration min, Duration avg, Duration p95, Duration p99, Duration max) {}
