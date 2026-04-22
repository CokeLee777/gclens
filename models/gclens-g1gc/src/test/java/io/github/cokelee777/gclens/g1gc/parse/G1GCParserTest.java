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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.cokelee777.gclens.g1gc.model.G1FullGC;
import io.github.cokelee777.gclens.g1gc.model.G1MixedGC;
import io.github.cokelee777.gclens.g1gc.model.G1YoungGC;
import io.github.cokelee777.gclens.model.GCLogVersion;
import io.github.cokelee777.gclens.parse.GCLogParseException;
import io.github.cokelee777.gclens.parse.ParsedLog;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Unit tests for {@link G1GCParser}. */
class G1GCParserTest {

  private final G1GCParser parser = new G1GCParser();

  @Test
  void parse_validLog_returnsEvents(@TempDir Path dir) throws IOException, GCLogParseException {
    Path log = dir.resolve("gc.log");
    Files.writeString(
        log,
        """
        [0.001s][info][gc,init] Version: 21.0.2+13-LTS
        [1.234s][info][gc] GC(0) Pause Young (Normal) (G1 Evacuation Pause) 512M->256M(1024M) 12.345ms
        [2.345s][info][gc] GC(1) Pause Mixed (G1 Evacuation Pause) 768M->384M(1024M) 18.234ms
        [3.456s][info][gc] GC(2) Pause Full (G1 Compaction Pause) 900M->512M(1024M) 1234.567ms
        """);

    ParsedLog result = parser.parse(log);

    assertThat(result.jdkVersion()).isEqualTo(GCLogVersion.JDK_21);
    assertThat(result.events()).hasSize(3);
    assertThat(result.events().get(0)).isInstanceOf(G1YoungGC.class);
    assertThat(result.events().get(1)).isInstanceOf(G1MixedGC.class);
    assertThat(result.events().get(2)).isInstanceOf(G1FullGC.class);
    assertThat(result.parseWarnings()).isEmpty();
  }

  @Test
  void parse_malformedLines_collectsWarnings(@TempDir Path dir)
      throws IOException, GCLogParseException {
    Path log = dir.resolve("gc.log");
    Files.writeString(
        log,
        """
        [0.001s][info][gc,init] Version: 21.0.2+13-LTS
        [1.234s][info][gc] GC(0) Pause Young (Normal) (G1 Evacuation Pause) 512M->256M(1024M) 12.345ms
        [2.345s][info][gc] GC(1) Pause Young MALFORMED LINE
        """);

    ParsedLog result = parser.parse(log);

    assertThat(result.events()).hasSize(1);
    assertThat(result.parseWarnings()).hasSize(1);
    assertThat(result.parseWarnings().get(0)).contains("Pause Young MALFORMED");
  }

  @Test
  void parse_nonGcLinesOnly_throwsException(@TempDir Path dir) throws IOException {
    Path log = dir.resolve("gc.log");
    Files.writeString(log, "[0.001s][info][gc,init] CardTable entry size: 512\n");

    assertThatThrownBy(() -> parser.parse(log))
        .isInstanceOf(GCLogParseException.class)
        .hasMessageContaining("No GC events");
  }

  @Test
  void parse_nonExistentFile_throwsException() {
    Path missing = Path.of("/tmp/does-not-exist-12345.log");
    assertThatThrownBy(() -> parser.parse(missing)).isInstanceOf(GCLogParseException.class);
  }

  @Test
  void supports_g1GcLog_returnsTrue(@TempDir Path dir) throws IOException {
    Path log = dir.resolve("gc.log");
    Files.writeString(
        log,
        "[1.234s][info][gc] GC(0) Pause Young (Normal) (G1 Evacuation Pause) 512M->256M(1024M) 12.345ms\n");
    assertThat(parser.supports(log)).isTrue();
  }

  @Test
  void supports_nonGcFile_returnsFalse(@TempDir Path dir) throws IOException {
    Path log = dir.resolve("app.log");
    Files.writeString(log, "some application log content\n");
    assertThat(parser.supports(log)).isFalse();
  }
}
