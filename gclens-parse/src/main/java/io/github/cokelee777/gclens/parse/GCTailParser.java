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

/**
 * Contract for incremental parsing of append-only GC logs. Kept separate from {@link GCLogParser}
 * to avoid crowding the main SPI.
 */
public interface GCTailParser {

  /** Returns whether tail/incremental parsing is supported for the given path. */
  boolean supportsTail(Path logPath);

  /**
   * Reads bytes from {@code state.getNextReadOffset()} through EOF, updates {@code state}, and
   * returns newly parsed events and warnings. The caller must reset {@code state} after rotation or
   * truncation before calling again.
   *
   * @param state mutable session state
   * @param logPath log file path
   * @return new events and warnings from this tick
   * @throws GCLogParseException if the file cannot be read
   */
  TailTickResult append(TailSessionState state, Path logPath) throws GCLogParseException;

  /**
   * Aligns {@code state} to EOF after a full {@link GCLogParser#parse(Path)} so subsequent {@link
   * #append} calls only see newly appended bytes. Does not emit events.
   */
  void syncCursorToEof(TailSessionState state, Path logPath) throws GCLogParseException;

  /**
   * Reads the first header lines from the start of the file, detects the JDK format version, then
   * positions the cursor at EOF with only an EOF partial-line tail in {@code
   * TailSessionState#getPendingPrefix()}. Does not parse GC events from the skipped middle of the
   * file (for {@code watch --from-end}).
   */
  void seedHeaderAndSeekEof(TailSessionState state, Path logPath) throws GCLogParseException;
}
