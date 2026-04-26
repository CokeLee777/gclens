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
package io.github.cokelee777.gclens.cli.command;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.cokelee777.gclens.cli.GCLensReporter;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

/** Smoke tests for {@link WatchCommand} via Picocli. */
class WatchCommandTest {

  @Test
  void watch_once_printsSummary(@TempDir Path dir) throws Exception {
    Path log = dir.resolve("gc.log");
    Files.writeString(
        log,
        """
        [0.001s][info][gc,init] Version: 21.0.2+13-LTS
        [1.234s][info][gc] GC(0) Pause Young (Normal) (G1 Evacuation Pause) 512M->256M(1024M) 12.345ms
        """);

    ByteArrayOutputStream captured = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(captured, true, StandardCharsets.UTF_8);
    PrintStream oldOut = System.out;
    System.setOut(ps);
    try {
      int exit = new CommandLine(new GCLensReporter()).execute("watch", "--once", log.toString());
      assertThat(exit).isZero();
    } finally {
      System.setOut(oldOut);
    }

    assertThat(captured.toString(StandardCharsets.UTF_8)).contains("[Summary]");
  }
}
