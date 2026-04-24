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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.format.DateTimeParseException;
import org.junit.jupiter.api.Test;

class WatchCommandIntervalTest {

  @Test
  void parseInterval_plainSeconds() {
    assertThat(WatchCommand.parseIntervalToMillis("5")).isEqualTo(5_000L);
    assertThat(WatchCommand.parseIntervalToMillis("0")).isZero();
    assertThat(WatchCommand.parseIntervalToMillis("5.0")).isEqualTo(5_000L);
    assertThat(WatchCommand.parseIntervalToMillis("0.5")).isEqualTo(500L);
  }

  @Test
  void parseInterval_humanSuffixes() {
    assertThat(WatchCommand.parseIntervalToMillis("5s")).isEqualTo(5_000L);
    assertThat(WatchCommand.parseIntervalToMillis("0.5s")).isEqualTo(500L);
    assertThat(WatchCommand.parseIntervalToMillis("1m")).isEqualTo(60_000L);
    assertThat(WatchCommand.parseIntervalToMillis("200ms")).isEqualTo(200L);
    assertThat(WatchCommand.parseIntervalToMillis("1H")).isEqualTo(3_600_000L);
  }

  @Test
  void parseInterval_isoDuration() {
    assertThat(WatchCommand.parseIntervalToMillis("PT5S")).isEqualTo(5_000L);
    assertThat(WatchCommand.parseIntervalToMillis("pT0.5S")).isEqualTo(500L);
    assertThat(WatchCommand.parseIntervalToMillis("PT1M")).isEqualTo(60_000L);
  }

  @Test
  void parseInterval_blankFallsBackTo5s() {
    assertThat(WatchCommand.parseIntervalToMillis("   ")).isEqualTo(5_000L);
  }

  @Test
  void parseInterval_rejectsJunk() {
    assertThatThrownBy(() -> WatchCommand.parseIntervalToMillis("nope"))
        .isInstanceOfAny(DateTimeParseException.class, NumberFormatException.class);
  }
}
