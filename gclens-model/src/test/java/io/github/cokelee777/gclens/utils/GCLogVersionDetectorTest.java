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

import static org.assertj.core.api.Assertions.assertThat;

import io.github.cokelee777.gclens.model.GCLogVersion;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link io.github.cokelee777.gclens.utils.GCLogVersionDetector}. */
class GCLogVersionDetectorTest {

  private final io.github.cokelee777.gclens.utils.GCLogVersionDetector detector =
      new io.github.cokelee777.gclens.utils.GCLogVersionDetector();

  @Test
  void detect_jdk17Header_returnsJdk17() {
    List<String> lines = List.of("[0.001s][info][gc,init] Version: 17.0.9+9-LTS");
    assertThat(detector.detect(lines)).isEqualTo(GCLogVersion.JDK_17);
  }

  @Test
  void detect_jdk21Header_returnsJdk21() {
    List<String> lines = List.of("[0.001s][info][gc,init] Version: 21.0.2+13-LTS");
    assertThat(detector.detect(lines)).isEqualTo(GCLogVersion.JDK_21);
  }

  @Test
  void detect_jdk25Header_returnsJdk25() {
    List<String> lines = List.of("[0.001s][info][gc,init] Version: 25.0.0+1");
    assertThat(detector.detect(lines)).isEqualTo(GCLogVersion.JDK_25);
  }

  @Test
  void detect_noVersionLine_returnsUnknown() {
    List<String> lines =
        List.of("[0.001s][info][gc] GC(0) Pause Young (Normal) 512M->256M(1024M) 10ms");
    assertThat(detector.detect(lines)).isEqualTo(GCLogVersion.UNKNOWN);
  }

  @Test
  void detect_emptyLines_returnsUnknown() {
    assertThat(detector.detect(List.of())).isEqualTo(GCLogVersion.UNKNOWN);
  }
}
