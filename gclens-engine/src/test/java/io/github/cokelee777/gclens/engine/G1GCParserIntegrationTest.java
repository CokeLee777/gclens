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
package io.github.cokelee777.gclens.engine;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.cokelee777.gclens.analysis.DefaultHeapAnalyzer;
import io.github.cokelee777.gclens.analysis.DefaultPauseAnalyzer;
import io.github.cokelee777.gclens.analysis.DefaultThroughputAnalyzer;
import io.github.cokelee777.gclens.model.GCLogVersion;
import io.github.cokelee777.gclens.report.DefaultGCWarningDetector;
import io.github.cokelee777.gclens.report.GCReport;
import io.github.cokelee777.gclens.report.GCWarning;
import io.github.cokelee777.gclens.report.GCWarningType;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Integration tests for {@link GCLogAnalyzer}. */
class G1GCParserIntegrationTest {

  private final GCLogAnalyzer analyzer =
      new GCLogAnalyzer(
          new ServiceLoaderGCLogParserFactory(),
          new DefaultPauseAnalyzer(),
          new DefaultHeapAnalyzer(),
          new DefaultThroughputAnalyzer(),
          new DefaultGCWarningDetector());

  @Test
  void analyze_jdk17NormalLog_parsesAllEvents() throws Exception {
    GCReport report = analyzer.analyze(resourcePath("g1-jdk17-normal.log"));

    assertThat(report.summary().jdkVersion()).isEqualTo(GCLogVersion.JDK_17);
    assertThat(report.summary().totalEvents()).isGreaterThan(0);
    assertThat(report.parseWarnings()).isEmpty();
    assertThat(report.throughput()).isGreaterThan(0.90);
  }

  @Test
  void analyze_jdk21NormalLog_parsesAllEvents() throws Exception {
    GCReport report = analyzer.analyze(resourcePath("g1-jdk21-normal.log"));

    assertThat(report.summary().jdkVersion()).isEqualTo(GCLogVersion.JDK_21);
    assertThat(report.summary().totalEvents()).isGreaterThan(0);
    assertThat(report.parseWarnings()).isEmpty();
  }

  @Test
  void analyze_jdk25NormalLog_parsesAllEvents() throws Exception {
    GCReport report = analyzer.analyze(resourcePath("g1-jdk25-normal.log"));

    assertThat(report.summary().jdkVersion()).isEqualTo(GCLogVersion.JDK_25);
    assertThat(report.summary().totalEvents()).isGreaterThan(0);
  }

  @Test
  void analyze_frequentFullGcLog_detectsWarning() throws Exception {
    GCReport report = analyzer.analyze(resourcePath("g1-full-gc-frequent.log"));

    assertThat(report.warnings())
        .extracting(GCWarning::type)
        .contains(GCWarningType.FREQUENT_FULL_GC);
  }

  @Test
  void analyze_heapExhaustionLog_detectsWarning() throws Exception {
    GCReport report = analyzer.analyze(resourcePath("g1-heap-exhaustion.log"));

    assertThat(report.warnings())
        .extracting(GCWarning::type)
        .contains(GCWarningType.HEAP_EXHAUSTION);
  }

  @Test
  void analyze_mixedFormatLog_collectsParseWarnings() throws Exception {
    GCReport report = analyzer.analyze(resourcePath("g1-mixed-format.log"));

    assertThat(report.parseWarnings()).isNotEmpty();
    assertThat(report.summary().totalEvents()).isGreaterThan(0);
  }

  private Path resourcePath(String filename) throws URISyntaxException {
    return Path.of(
        Objects.requireNonNull(
                getClass().getClassLoader().getResource("gc-logs/" + filename),
                "Test resource not found: gc-logs/" + filename)
            .toURI());
  }
}
