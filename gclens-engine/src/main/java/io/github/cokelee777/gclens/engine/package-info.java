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

/**
 * End-to-end log analysis orchestration and parser discovery. {@link
 * io.github.cokelee777.gclens.engine.GCLogAnalyzer} runs a {@link
 * io.github.cokelee777.gclens.parse.GCLogParser} from a {@link
 * io.github.cokelee777.gclens.parse.GCLogParserFactory}, delegates to the statistical analyzers and
 * {@link io.github.cokelee777.gclens.report.GCWarningDetector}, and builds a {@link
 * io.github.cokelee777.gclens.report.GCReport}. {@link
 * io.github.cokelee777.gclens.engine.ServiceLoaderGCLogParserFactory} loads {@link
 * io.github.cokelee777.gclens.parse.GCLogParser} implementations from the classpath via {@code
 * META-INF/services}.
 */
@NullMarked
package io.github.cokelee777.gclens.engine;

import org.jspecify.annotations.NullMarked;
