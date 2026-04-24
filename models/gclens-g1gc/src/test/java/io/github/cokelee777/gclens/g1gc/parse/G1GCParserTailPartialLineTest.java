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
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Tests partial-line buffering across tail ticks. */
class G1GCParserTailPartialLineTest {

  private final G1GCParser parser = new G1GCParser();

  @Test
  void append_splitsLineAcrossTicks(@TempDir Path dir) throws Exception {
    Path log = dir.resolve("gc.log");
    String header = "[0.001s][info][gc,init] Version: 21.0.2+13-LTS\n";
    String partA = "[1.234s][info][gc] GC(0) Pause Young (Normal) (G1 Evacuation Pause) ";
    String partB = "512M->256M(1024M) 12.345ms\n";

    TailSessionState state = TailSessionState.initial();
    List<GCEvent> events = new ArrayList<>();

    Files.writeString(log, header + partA, StandardCharsets.UTF_8);
    TailTickResult t1 = parser.append(state, log);
    assertThat(t1.newEvents()).isEmpty();

    Files.writeString(log, header + partA + partB, StandardCharsets.UTF_8);
    TailTickResult t2 = parser.append(state, log);
    assertThat(t2.newEvents()).hasSize(1);

    events.addAll(t1.newEvents());
    events.addAll(t2.newEvents());

    ParsedLog once = parser.parse(log);
    assertThat(events).containsExactlyElementsOf(once.events());
  }
}
