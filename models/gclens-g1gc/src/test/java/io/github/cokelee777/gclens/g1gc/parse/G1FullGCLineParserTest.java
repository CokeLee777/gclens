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

import io.github.cokelee777.gclens.g1gc.model.G1FullGC;
import io.github.cokelee777.gclens.model.MemorySize;
import io.github.cokelee777.gclens.parse.ParseResult;
import java.time.Duration;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link G1FullGCLineParser}. */
class G1FullGCLineParserTest {

  private final G1FullGCLineParser parser = new G1FullGCLineParser();

  @Test
  void parse_validLine_returnsSuccess() {
    String line =
        "[8.901s][info][gc] GC(44) Pause Full (G1 Compaction Pause) 900M->512M(1024M) 1234.567ms";

    ParseResult result = parser.parse(line);

    assertThat(result).isInstanceOf(ParseResult.Success.class);
    G1FullGC event = (G1FullGC) ((ParseResult.Success) result).event();
    assertThat(event.timestamp()).isEqualTo(Duration.ofMillis(8901));
    assertThat(event.heapBefore()).isEqualTo(MemorySize.ofMegabytes(900));
    assertThat(event.heapAfter()).isEqualTo(MemorySize.ofMegabytes(512));
    assertThat(event.heapTotal()).isEqualTo(MemorySize.ofMegabytes(1024));
  }

  @Test
  void parse_nonFullGcLine_returnsSkip() {
    String line = "[5.678s][info][gc] GC(43) Pause Young (Normal) 512M->256M(1024M) 10ms";
    assertThat(parser.parse(line)).isInstanceOf(ParseResult.Skip.class);
  }

  @Test
  void parse_malformedLine_returnsWarn() {
    String line = "[8.901s][info][gc] GC(44) Pause Full MALFORMED";
    assertThat(parser.parse(line)).isInstanceOf(ParseResult.Warn.class);
  }
}
