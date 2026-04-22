# GCLens

[![CI](https://github.com/cokelee777/gclens/actions/workflows/ci.yml/badge.svg)](https://github.com/cokelee777/gclens/actions/workflows/ci.yml)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

**GCLens** is a JVM GC log analysis toolkit. It turns raw GC logs into structured events, pause and heap statistics, throughput metrics, and heuristic tuning hints—usable from the command line or as a Java library.

## Overview

- Parses **G1** GC logs (unified logging style) via a pluggable [`GCLogParser`](gclens-parse/src/main/java/io/github/cokelee777/gclens/parse/GCLogParser.java) SPI (`ServiceLoader`).
- Aggregates **pause**, **heap**, and **throughput** views and emits a [`GCReport`](gclens-insights/src/main/java/io/github/cokelee777/gclens/report/GCReport.java) with optional [`GCWarning`](gclens-insights/src/main/java/io/github/cokelee777/gclens/report/GCWarning.java) hints.
- Ships a **CLI** that prints a human-readable report to standard output.

## Features

- G1 young / mixed / full pause line parsing and event model
- Pause statistics (min, avg, percentiles, max), heap usage and trend, allocation throughput
- Heuristic warnings (e.g. frequent full GC, heap pressure) driven by [`GCWarningDetector`](gclens-insights/src/main/java/io/github/cokelee777/gclens/report/GCWarningDetector.java)
- Modular Gradle build: core contracts, engine orchestration, optional collector implementations under `models/`

## Installation

```bash
curl -fsSL https://raw.githubusercontent.com/cokelee777/gclens/main/install.sh | sh
```

The script installs the latest release:

- Requires **Java** to be available on `$PATH`
- Installs to `/usr/local/bin/gclens` (uses `sudo` if needed)
- If `sudo` is unavailable, falls back to `~/.local/bin/gclens` — add it to `PATH` if not already:

  ```bash
  export PATH="$PATH:$HOME/.local/bin"
  ```

To verify:

```bash
gclens --help
```

## Requirements

- **JDK 25** (toolchain configured in the build)

## Build

```bash
./gradlew build
```

Run the CLI from the workspace (requires a G1 GC log file):

```bash
./gradlew :gclens-cli:run --args="analyze /path/to/gc.log"
```

Installable distributions (ZIP/TAR) are produced by `:gclens-cli:distZip` / `:gclens-cli:distTar`.

## Usage

The executable name is **`gclens`**. It is a Picocli application: run **`gclens`** (or **`java -jar …`**) with a **subcommand**. Today the only subcommand is **`analyze`**.

### Global

| Command | Description |
|--------|-------------|
| `gclens` | Prints top-level help (available subcommands). |
| `gclens --help` | Same as above. |

### `analyze` — analyze one GC log file

Parses a **G1** GC log, runs statistics and warnings, and prints a text report to **standard output** (via SLF4J at INFO).

```text
gclens analyze [<log-file>] [--verbose]
```

| Argument / option | Description |
|-------------------|-------------|
| `<log-file>` | Path to the GC log file. If omitted, defaults to **`gc.log`** in the current working directory. |
| `--verbose` | After the report, lists **unparseable** lines in detail (when the parser recorded any). |

**Examples**

```bash
# Help for the analyze subcommand
gclens analyze --help

# Analyze ./gc.log (explicit path)
gclens analyze ./gc.log

# Use default path gc.log in the current directory
gclens analyze

# Verbose: show lines that could not be parsed
gclens analyze /var/log/app/gc.log --verbose
```

**From a development build** (same flags; pass arguments after `--args`):

```bash
./gradlew :gclens-cli:run --args="analyze --help"
./gradlew :gclens-cli:run --args="analyze /path/to/gc.log --verbose"
```

**Local install layout** (after `./gradlew :gclens-cli:installDist`):

```bash
gclens-cli/build/install/gclens/bin/gclens analyze /path/to/gc.log
```

**Distribution archive** (`./gradlew :gclens-cli:distZip`): unzip the archive under `gclens-cli/build/distributions/`, then run `bin/gclens` from the unpacked application directory.

A **fat JAR** (dependencies embedded) is built by `./gradlew :gclens-cli:jar`; run `java -jar gclens-cli/build/libs/gclens-cli-<version>.jar analyze …` (replace `<version>` with the project version from `gradle.properties`).

**Exit codes:** `0` on success; **`1`** if parsing fails (missing file, unreadable log, no parser supports the file, no GC events found, etc.).

## Modules

| Module | Role |
|--------|------|
| [`gclens-bom`](gclens-bom/build.gradle.kts) | Gradle BOM for aligned dependency versions |
| [`gclens-model`](gclens-model/) | Core domain types (`GCEvent`, `MemorySize`, `GCLogVersion`, …) and [`GCLogVersionDetector`](gclens-model/src/main/java/io/github/cokelee777/gclens/utils/GCLogVersionDetector.java) |
| [`gclens-parse`](gclens-parse/) | Parse contracts (`GCLogParser`, `GCLogLineParser`, `ParsedLog`, …) |
| [`gclens-insights`](gclens-insights/) | Analysis APIs, report DTOs, and [`GCWarningDetector`](gclens-insights/src/main/java/io/github/cokelee777/gclens/report/GCWarningDetector.java) |
| [`gclens-engine`](gclens-engine/) | [`GCLogAnalyzer`](gclens-engine/src/main/java/io/github/cokelee777/gclens/engine/GCLogAnalyzer.java) orchestration and [`ServiceLoaderGCLogParserFactory`](gclens-engine/src/main/java/io/github/cokelee777/gclens/engine/ServiceLoaderGCLogParserFactory.java) |
| [`gclens-cli`](gclens-cli/) | Picocli entrypoint ([`GCLensReporter`](gclens-cli/src/main/java/io/github/cokelee777/gclens/cli/GCLensReporter.java)), `analyze` command, console reporter |
| [`gclens-g1gc`](models/gclens-g1gc/) | G1 parser implementation (under `models/` for additional collectors later) |

## Library coordinates

Published artifacts use the Maven coordinates `io.github.cokelee777` with artifact IDs matching module names (for example `gclens-model`, `gclens-parse`, `gclens-insights`, `gclens-engine`). Releases are published to **GitHub Packages**; configure the `maven.pkg.github.com` repository and credentials described in [GitHub’s documentation](https://docs.github.com/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry) to consume them from Gradle or Maven.

Use the BOM to align versions:

```kotlin
dependencies {
    implementation(platform("io.github.cokelee777:gclens-bom:0.1.0"))
    implementation("io.github.cokelee777:gclens-engine")
    runtimeOnly("io.github.cokelee777:gclens-g1gc")
}
```

(Replace the version with the release you depend on.)

## License

Licensed under the **Apache License 2.0** — see [`LICENSE`](LICENSE).

## Acknowledgements

Built with [Gradle](https://gradle.org/), [Picocli](https://picocli.info/), [JUnit 5](https://junit.org/junit5/), and [AssertJ](https://assertj.github.io/doc/).
