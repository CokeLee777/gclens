plugins {
    id("java-platform")
}

group = "io.github.cokelee777"
version = "0.0.1-SNAPSHOT"

dependencies {
    constraints {
        api("io.github.cokelee777:gclens-parser:${project.version}")
        api("io.github.cokelee777:gclens-analyzer:${project.version}")
        api("io.github.cokelee777:gclens-reporter:${project.version}")
    }
}