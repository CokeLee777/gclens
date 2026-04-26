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

import io.github.cokelee777.gclens.model.GCEvent;
import io.github.cokelee777.gclens.parse.ParsedLog;
import io.github.cokelee777.gclens.parse.TailSessionState;
import io.github.cokelee777.gclens.parse.TailTickResult;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Incremental tail parsing tests for {@link G1GCParser}. */
class G1GCParserTailTest {

  private final G1GCParser parser = new G1GCParser();

  @Test
  void append_incrementalWrites_matchesSingleParse(@TempDir Path dir) throws Exception {
    Path log = dir.resolve("gc.log");
    String full =
        """
        [0.001s][info][gc,init] Version: 21.0.2+13-LTS
        [1.234s][info][gc] GC(0) Pause Young (Normal) (G1 Evacuation Pause) 512M->256M(1024M) 12.345ms
        [2.345s][info][gc] GC(1) Pause Mixed (G1 Evacuation Pause) 768M->384M(1024M) 18.234ms
        """;
    byte[] bytes = full.getBytes(StandardCharsets.UTF_8);

    List<GCEvent> incremental = new ArrayList<>();
    TailSessionState state = TailSessionState.initial();
    int pos = 0;
    while (pos < bytes.length) {
      int chunk = Math.min(7, bytes.length - pos);
      Files.write(log, Arrays.copyOf(bytes, pos + chunk));
      pos += chunk;
      TailTickResult tick = parser.append(state, log);
      incremental.addAll(tick.newEvents());
    }

    ParsedLog once = parser.parse(log);
    assertThat(incremental).hasSize(once.events().size());
    assertThat(incremental).containsExactlyElementsOf(once.events());
  }

  /**
   * When the file is shorter than 20 lines, sync/seed set headerComplete true, but the log can
   * still grow. Tail append must keep filling the 20-line header buffer (same as full {@code
   * parse}).
   */
  @Test
  void afterShortFileSync_growingFile_appendsToHeaderBuffer(@TempDir Path dir) throws Exception {
    Path log = dir.resolve("gc.log");
    String first =
        """
        [0.001s][info][gc,init] a
        [0.002s][info][gc,init] b
        """;
    Files.writeString(log, first, StandardCharsets.UTF_8);
    TailSessionState state = TailSessionState.initial();
    parser.syncCursorToEof(state, log);
    assertThat(state.getHeaderLines()).hasSize(2);
    assertThat(state.isHeaderComplete()).isTrue();
    String grown =
        first
            + """
            [0.003s][info][gc,init] c
            [0.004s][info][gc,init] d
            [0.005s][info][gc,init] e
            """;
    Files.writeString(log, grown, StandardCharsets.UTF_8);
    parser.append(state, log);
    assertThat(state.getHeaderLines()).hasSize(5);
  }
}
