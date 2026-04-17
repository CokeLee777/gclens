plugins {
    id("java-platform")
}

dependencies {
    constraints {
        api("io.github.cokelee777:gclens-parser:${project.version}")
        api("io.github.cokelee777:gclens-analyzer:${project.version}")
        api("io.github.cokelee777:gclens-reporter:${project.version}")
    }
}