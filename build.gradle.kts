val jspecifyVersion by extra("1.0.0")
val junitVersion by extra("5.12.1")
val assertjVersion by extra("3.27.3")
val googleJavaFormatVersion by extra("1.35.0")

plugins {
    java
    id("com.diffplug.spotless") version "8.4.0" apply false
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
        implementation("org.jspecify:jspecify:$jspecifyVersion")

        testImplementation(platform("org.junit:junit-bom:$junitVersion"))
        testImplementation("org.junit.jupiter:junit-jupiter")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
        testImplementation("org.assertj:assertj-core:$assertjVersion")
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }

    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        java {
            googleJavaFormat(googleJavaFormatVersion)
            target("src/**/*.java")
        }
    }
}
