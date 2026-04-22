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
 * G1 GC log parsing: {@link io.github.cokelee777.gclens.g1gc.parse.G1GCParser} implements {@link
 * io.github.cokelee777.gclens.parse.GCLogParser}; {@link
 * io.github.cokelee777.gclens.g1gc.parse.G1YoungGCLineParser}, {@link
 * io.github.cokelee777.gclens.g1gc.parse.G1MixedGCLineParser}, and {@link
 * io.github.cokelee777.gclens.g1gc.parse.G1FullGCLineParser} implement {@link
 * io.github.cokelee777.gclens.parse.GCLogLineParser} for individual pause lines.
 */
@NullMarked
package io.github.cokelee777.gclens.g1gc.parse;

import org.jspecify.annotations.NullMarked;
