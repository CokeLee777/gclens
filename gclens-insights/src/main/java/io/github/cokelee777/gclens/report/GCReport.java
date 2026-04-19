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

/**
 * The complete analysis result returned to reporters.
 *
 * @param summary aggregate counts and time ranges
 * @param pause pause-time statistics
 * @param heap heap-usage statistics
 * @param throughput application throughput ratio in the range {@code 0.0} to {@code 1.0}
 * @param warnings detected GC warnings
 * @param parseWarnings raw lines that could not be parsed
 */
public record GCReport(
    Summary summary,
    PauseStats pause,
    HeapStats heap,
    double throughput,
    List<GCWarning> warnings,
    List<String> parseWarnings) {}
