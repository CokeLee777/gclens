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
package io.github.cokelee777.gclens.utils;

import io.github.cokelee777.gclens.model.GCLogVersion;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Detects the JDK version from GC log header lines. */
public class GCLogVersionDetector {

  private static final Pattern VERSION_PATTERN =
      Pattern.compile("\\[gc,init\\].*Version:\\s+(\\d+)\\.");

  /** Creates a new detector. */
  public GCLogVersionDetector() {}

  /**
   * Inspects header lines for a {@code [gc,init]} JDK version marker.
   *
   * @param lines log lines to scan (typically the first lines of the file)
   * @return the inferred {@link GCLogVersion}, or {@link GCLogVersion#UNKNOWN} if none match
   */
  public GCLogVersion detect(List<String> lines) {
    Objects.requireNonNull(lines, "lines must not be null");

    for (String line : lines) {
      Matcher m = VERSION_PATTERN.matcher(line);
      if (m.find()) {
        return switch (m.group(1)) {
          case "17" -> GCLogVersion.JDK_17;
          case "21" -> GCLogVersion.JDK_21;
          case "25" -> GCLogVersion.JDK_25;
          default -> GCLogVersion.UNKNOWN;
        };
      }
    }
    return GCLogVersion.UNKNOWN;
  }
}
