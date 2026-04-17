plugins {
    java
    alias(libs.plugins.spotless) apply false
}

java {
    sourceSets.named("main") { java.setSrcDirs(emptyList<File>()) }
    sourceSets.named("test") { java.setSrcDirs(emptyList<File>()) }
}

tasks.named("build") { enabled = false }
tasks.named("assemble") { enabled = false }
tasks.named("jar") { enabled = false }

subprojects {
    if (name == "gclens-bom") return@subprojects

    apply(plugin = "java-library")
    apply(plugin = "com.diffplug.spotless")

    repositories {
        mavenCentral()
    }

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(25)
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
    }

    dependencies {
        implementation(rootProject.libs.jspecify)

        testImplementation(platform(rootProject.libs.junit.bom))
        testImplementation(rootProject.libs.junit.jupiter)
        testRuntimeOnly(rootProject.libs.junit.platform.launcher)
        testImplementation(rootProject.libs.assertj)
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }

    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        java {
            googleJavaFormat(rootProject.libs.versions.googleJavaFormat.get())
            target("src/**/*.java")
        }
    }
}
