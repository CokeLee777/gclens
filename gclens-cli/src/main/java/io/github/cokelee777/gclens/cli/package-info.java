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
 * Command-line application root: {@link io.github.cokelee777.gclens.cli.GCLensReporter} (Picocli
 * {@code main}). Subcommands live under {@link io.github.cokelee777.gclens.cli.command}; console
 * output under {@link io.github.cokelee777.gclens.cli.output}. Library types such as {@link
 * io.github.cokelee777.gclens.report.GCReporter} are defined in {@code gclens-insights} (with parse
 * contracts in {@code gclens-parse} and core types in {@code gclens-model}).
 */
@NullMarked
package io.github.cokelee777.gclens.cli;

import org.jspecify.annotations.NullMarked;
