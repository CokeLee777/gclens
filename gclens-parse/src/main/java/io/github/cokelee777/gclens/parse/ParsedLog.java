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
package io.github.cokelee777.gclens.parse;

import io.github.cokelee777.gclens.model.GCEvent;
import io.github.cokelee777.gclens.model.GCLogVersion;
import java.util.List;

/**
 * The result of parsing a GC log file.
 *
 * @param jdkVersion the detected JDK log format version
 * @param events the parsed GC events in log order
 * @param parseWarnings raw lines that were recognized as GC-related but could not be parsed
 */
public record ParsedLog(
    GCLogVersion jdkVersion, List<GCEvent> events, List<String> parseWarnings) {}
