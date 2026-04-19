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

import io.github.cokelee777.gclens.model.GCLogVersion;
import java.time.Duration;

/**
 * Aggregate counts and time ranges for an analyzed GC log.
 *
 * @param totalEvents total number of GC events
 * @param totalPauseTime sum of all event pause durations
 * @param analysisPeriod wall-clock span from first to last event
 * @param youngGcCount number of young collection events
 * @param mixedGcCount number of mixed collection events
 * @param fullGcCount number of full collection events
 * @param jdkVersion the detected JDK log format version
 */
public record Summary(
    long totalEvents,
    Duration totalPauseTime,
    Duration analysisPeriod,
    long youngGcCount,
    long mixedGcCount,
    long fullGcCount,
    GCLogVersion jdkVersion) {}
