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
package io.github.cokelee777.gclens.g1gc.parse;

import io.github.cokelee777.gclens.g1gc.model.G1FullGC;
import io.github.cokelee777.gclens.model.MemorySize;
import io.github.cokelee777.gclens.parse.GCLogLineParser;
import io.github.cokelee777.gclens.parse.ParseResult;
import java.time.Duration;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Parses a single G1 full GC log line. */
public class G1FullGCLineParser implements GCLogLineParser {

  private static final Pattern PATTERN =
      Pattern.compile("\\[([\\d.]+)s].*Pause Full.*?(\\d+)M->(\\d+)M\\((\\d+)M\\)\\s+([\\d.]+)ms");

  /** Creates a new parser. */
  public G1FullGCLineParser() {}

  /**
   * Attempts to parse one log line into a {@link io.github.cokelee777.gclens.g1gc.model.G1FullGC}
   * event.
   *
   * @param line one line from a G1 GC log
   * @return success with an event, a warning if the line looked relevant but did not match, or skip
   */
  @Override
  public ParseResult parse(String line) {
    Objects.requireNonNull(line, "line must not be null");
    if (line.isBlank() || !line.contains("Pause Full")) {
      return new ParseResult.Skip();
    }
    Matcher m = PATTERN.matcher(line);
    if (!m.find()) {
      return new ParseResult.Warn(line);
    }
    return new ParseResult.Success(
        new G1FullGC(
            Duration.ofMillis((long) (Double.parseDouble(m.group(1)) * 1000)),
            Duration.ofNanos((long) (Double.parseDouble(m.group(5)) * 1_000_000)),
            MemorySize.ofMegabytes(Long.parseLong(m.group(2))),
            MemorySize.ofMegabytes(Long.parseLong(m.group(3))),
            MemorySize.ofMegabytes(Long.parseLong(m.group(4)))));
  }
}
