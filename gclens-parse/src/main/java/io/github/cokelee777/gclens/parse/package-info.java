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
 * Contracts for reading JVM GC logs: whole-file {@link
 * io.github.cokelee777.gclens.parse.GCLogParser}, single-line {@link
 * io.github.cokelee777.gclens.parse.GCLogLineParser}, {@link
 * io.github.cokelee777.gclens.parse.GCLogParserFactory} for discovery, {@link
 * io.github.cokelee777.gclens.parse.ParsedLog} / {@link
 * io.github.cokelee777.gclens.parse.ParseResult}, and {@link
 * io.github.cokelee777.gclens.parse.GCLogParseException}. Log format version is represented by
 * {@link io.github.cokelee777.gclens.model.GCLogVersion}.
 */
@NullMarked
package io.github.cokelee777.gclens.parse;

import org.jspecify.annotations.NullMarked;
