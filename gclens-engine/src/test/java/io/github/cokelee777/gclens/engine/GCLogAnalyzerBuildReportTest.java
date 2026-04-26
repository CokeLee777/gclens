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
import io.github.cokelee777.gclens.parse.ParsedLog;
import io.github.cokelee777.gclens.report.DefaultGCWarningDetector;
import io.github.cokelee777.gclens.report.GCReport;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Tests for {@link GCLogAnalyzer#buildReport}. */
class GCLogAnalyzerBuildReportTest {

  private final GCLogAnalyzer analyzer =
      new GCLogAnalyzer(
          new ServiceLoaderGCLogParserFactory(),
          new DefaultPauseAnalyzer(),
          new DefaultHeapAnalyzer(),
          new DefaultThroughputAnalyzer(),
          new DefaultGCWarningDetector());

  @Test
  void buildReport_emptyEvents_sentinelSummaryAndNoGcWarnings() {
    GCReport report = analyzer.buildReport(List.of(), GCLogVersion.JDK_21, List.of("warn line"));

    assertThat(report.summary().totalEvents()).isZero();
    assertThat(report.summary().totalPauseTime()).isEqualTo(Duration.ZERO);
    assertThat(report.summary().analysisPeriod()).isEqualTo(Duration.ZERO);
    assertThat(report.summary().jdkVersion()).isEqualTo(GCLogVersion.JDK_21);
    assertThat(report.throughput()).isZero();
    assertThat(report.warnings()).isEmpty();
    assertThat(report.parseWarnings()).containsExactly("warn line");
  }

  @Test
  void buildReport_matchesAnalyzePath(@TempDir Path dir) throws Exception {
    Path log = dir.resolve("gc.log");
    Files.writeString(
        log,
        """
        [0.001s][info][gc,init] Version: 21.0.2+13-LTS
        [1.234s][info][gc] GC(0) Pause Young (Normal) (G1 Evacuation Pause) 512M->256M(1024M) 12.345ms
        [2.345s][info][gc] GC(1) Pause Mixed (G1 Evacuation Pause) 768M->384M(1024M) 18.234ms
        """);

    GCReport fromAnalyze = analyzer.analyze(log);
    ParsedLog parsed = new ServiceLoaderGCLogParserFactory().createFor(log).parse(log);
    GCReport fromBuild =
        analyzer.buildReport(parsed.events(), parsed.jdkVersion(), parsed.parseWarnings());

    assertThat(fromBuild.summary()).isEqualTo(fromAnalyze.summary());
    assertThat(fromBuild.pause()).isEqualTo(fromAnalyze.pause());
    assertThat(fromBuild.heap()).isEqualTo(fromAnalyze.heap());
    assertThat(fromBuild.throughput()).isEqualTo(fromAnalyze.throughput());
    assertThat(fromBuild.warnings()).isEqualTo(fromAnalyze.warnings());
    assertThat(fromBuild.parseWarnings()).isEqualTo(fromAnalyze.parseWarnings());
  }
}
