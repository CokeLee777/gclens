# GCLens

A free, open-source GC log analysis tool for JVM applications.

GCLens parses G1GC, ZGC, and Shenandoah GC logs to provide CLI reports and HTML visualizations with actionable tuning insights.

## Features

- GC log parsing for G1GC, ZGC, and Shenandoah
- Pause time, throughput, and heap usage analysis
- CLI text report and HTML visualization
- Tuning recommendations via `GCWarning` system
- Usable as a Java library via Maven Central

## Modules

| Module | Description |
|--------|-------------|
| `gclens-parser` | Parses GC log files into domain model events |
| `gclens-analyzer` | Aggregates events and computes statistics |
| `gclens-reporter` | Generates CLI and HTML reports |
| `gclens-bom` | BOM for version management |

## Requirements

- Java 25+

## Build

```bash
./gradlew build
```

## License

Apache 2.0