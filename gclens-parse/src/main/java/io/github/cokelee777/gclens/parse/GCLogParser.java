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

import java.nio.file.Path;

/** Parses GC log files into structured events and warnings. */
public interface GCLogParser {

  /**
   * Parses the supplied log file.
   *
   * @param logPath the log file to parse
   * @return the parsed log contents
   * @throws GCLogParseException if the file cannot be parsed
   */
  ParsedLog parse(Path logPath) throws GCLogParseException;

  /**
   * Returns whether this parser can handle the supplied log file.
   *
   * @param logPath the candidate log file
   * @return {@code true} if this parser supports the file
   */
  boolean supports(Path logPath);
}
