plugins {
    id("application")
}

application {
    mainClass = "io.github.cokelee777.gclens.reporter.GCLensReporter"
}

dependencies {
    implementation(project(":gclens-analyzer"))
    implementation("info.picocli:picocli:4.7.7")
    annotationProcessor("info.picocli:picocli-codegen:4.7.7")
}