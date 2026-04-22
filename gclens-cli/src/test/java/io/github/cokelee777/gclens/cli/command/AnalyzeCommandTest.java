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

import static org.assertj.core.api.Assertions.assertThatNoException;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Integration tests for {@link AnalyzeCommand}. */
class AnalyzeCommandTest {

  @Test
  void run_validLogFile_doesNotThrow() throws URISyntaxException {
    Path logPath = resourcePath("g1-jdk21-normal.log");
    AnalyzeCommand command = new AnalyzeCommand(logPath, false);
    assertThatNoException().isThrownBy(command::run);
  }

  @Test
  void run_validLogFileVerbose_doesNotThrow() throws URISyntaxException {
    Path logPath = resourcePath("g1-jdk21-normal.log");
    AnalyzeCommand command = new AnalyzeCommand(logPath, true);
    assertThatNoException().isThrownBy(command::run);
  }

  private Path resourcePath(String filename) throws URISyntaxException {
    return Path.of(
        Objects.requireNonNull(
                getClass().getClassLoader().getResource("gc-logs/" + filename),
                "Test resource not found: gc-logs/" + filename)
            .toURI());
  }
}
