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

import io.github.cokelee777.gclens.g1gc.model.G1MixedGC;
import io.github.cokelee777.gclens.model.MemorySize;
import io.github.cokelee777.gclens.parse.ParseResult;
import java.time.Duration;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link G1MixedGCLineParser}. */
class G1MixedGCLineParserTest {

  private final G1MixedGCLineParser parser = new G1MixedGCLineParser();

  @Test
  void parse_validLine_returnsSuccess() {
    String line =
        "[5.678s][info][gc] GC(43) Pause Mixed (G1 Evacuation Pause) 768M->384M(1024M) 18.234ms";

    ParseResult result = parser.parse(line);

    assertThat(result).isInstanceOf(ParseResult.Success.class);
    G1MixedGC event = (G1MixedGC) ((ParseResult.Success) result).event();
    assertThat(event.timestamp()).isEqualTo(Duration.ofMillis(5678));
    assertThat(event.heapBefore()).isEqualTo(MemorySize.ofMegabytes(768));
    assertThat(event.heapAfter()).isEqualTo(MemorySize.ofMegabytes(384));
    assertThat(event.heapTotal()).isEqualTo(MemorySize.ofMegabytes(1024));
  }

  @Test
  void parse_nonMixedLine_returnsSkip() {
    String line = "[5.678s][info][gc] GC(43) Pause Young (Normal) 512M->256M(1024M) 10ms";
    assertThat(parser.parse(line)).isInstanceOf(ParseResult.Skip.class);
  }

  @Test
  void parse_malformedLine_returnsWarn() {
    String line = "[5.678s][info][gc] GC(43) Pause Mixed MALFORMED";
    assertThat(parser.parse(line)).isInstanceOf(ParseResult.Warn.class);
  }
}
