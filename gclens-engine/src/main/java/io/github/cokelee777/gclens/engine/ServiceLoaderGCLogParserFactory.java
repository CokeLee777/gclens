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
package io.github.cokelee777.gclens.engine;

import io.github.cokelee777.gclens.parse.GCLogParseException;
import io.github.cokelee777.gclens.parse.GCLogParser;
import io.github.cokelee777.gclens.parse.GCLogParserFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.ServiceLoader;

/** Discovers available {@link GCLogParser} implementations via Java {@link ServiceLoader}. */
public class ServiceLoaderGCLogParserFactory implements GCLogParserFactory {

  /** Creates a new factory that uses the default {@link ServiceLoader} class loader. */
  public ServiceLoaderGCLogParserFactory() {}

  @Override
  public GCLogParser createFor(Path logPath) throws GCLogParseException {
    Objects.requireNonNull(logPath, "logPath must not be null");

    if (!Files.exists(logPath)) {
      throw new GCLogParseException("Log file not found: " + logPath);
    }
    if (!Files.isReadable(logPath)) {
      throw new GCLogParseException("Log file is not readable: " + logPath);
    }

    return ServiceLoader.load(GCLogParser.class).stream()
        .map(ServiceLoader.Provider::get)
        .filter(p -> p.supports(logPath))
        .findFirst()
        .orElseThrow(() -> new GCLogParseException("No parser found for: " + logPath));
  }
}
