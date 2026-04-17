plugins {
    id("application")
}

application {
    mainClass = "io.github.cokelee777.gclens.reporter.GCLensReporter"
}

dependencies {
    implementation(project(":gclens-analyzer"))
    implementation(rootProject.libs.picocli)
    annotationProcessor(rootProject.libs.picocli.codegen)
}