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

import static org.assertj.core.api.Assertions.assertThat;

import io.github.cokelee777.gclens.g1gc.model.G1YoungGC;
import io.github.cokelee777.gclens.model.MemorySize;
import io.github.cokelee777.gclens.parse.ParseResult;
import java.time.Duration;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link G1YoungGCLineParser}. */
class G1YoungGCLineParserTest {

  private final G1YoungGCLineParser parser = new G1YoungGCLineParser();

  @Test
  void parse_validLine_returnsSuccess() {
    String line =
        "[2.345s][info][gc] GC(42) Pause Young (Normal) (G1 Evacuation Pause) 512M->256M(1024M) 12.345ms";

    ParseResult result = parser.parse(line);

    assertThat(result).isInstanceOf(ParseResult.Success.class);
    G1YoungGC event = (G1YoungGC) ((ParseResult.Success) result).event();
    assertThat(event.timestamp()).isEqualTo(Duration.ofMillis(2345));
    assertThat(event.duration()).isEqualTo(Duration.ofNanos(12_345_000));
    assertThat(event.heapBefore()).isEqualTo(MemorySize.ofMegabytes(512));
    assertThat(event.heapAfter()).isEqualTo(MemorySize.ofMegabytes(256));
    assertThat(event.heapTotal()).isEqualTo(MemorySize.ofMegabytes(1024));
  }

  @Test
  void parse_nonGcLine_returnsSkip() {
    String line = "[0.001s][info][gc,init] CardTable entry size: 512";
    assertThat(parser.parse(line)).isInstanceOf(ParseResult.Skip.class);
  }

  @Test
  void parse_malformedGcLine_returnsWarn() {
    String line = "[2.345s][info][gc] GC(42) Pause Young MALFORMED";
    ParseResult result = parser.parse(line);
    assertThat(result).isInstanceOf(ParseResult.Warn.class);
    assertThat(((ParseResult.Warn) result).line()).isEqualTo(line);
  }

  @Test
  void parse_jdk21PreventiveCollection_returnsSuccess() {
    String line =
        "[5.678s][info][gc] GC(10) Pause Young (Normal) (G1 Preventive Collection) 200M->100M(512M) 8.123ms";
    assertThat(parser.parse(line)).isInstanceOf(ParseResult.Success.class);
  }

  @Test
  void parse_blankLine_returnsSkip() {
    assertThat(parser.parse("")).isInstanceOf(ParseResult.Skip.class);
    assertThat(parser.parse("   ")).isInstanceOf(ParseResult.Skip.class);
  }
}
