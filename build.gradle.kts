import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication

plugins {
    java
    alias(libs.plugins.spotless) apply false
}

group = findProperty("group") as String
version = findProperty("version") as String

java {
    sourceSets.named("main") { java.setSrcDirs(emptyList<File>()) }
    sourceSets.named("test") { java.setSrcDirs(emptyList<File>()) }
}

tasks.named("build") { enabled = false }
tasks.named("assemble") { enabled = false }
tasks.named("jar") { enabled = false }

fun Project.githubPackagesSlug(): String =
    findProperty("github.packages.repository")?.toString()
        ?: System.getenv("GITHUB_REPOSITORY")
        ?: "cokelee777/gclens"

fun PublishingExtension.mavenGitHubPackages(project: Project) {
    repositories {
        maven {
            name = "GitHubPackages"
            url = project.uri("https://maven.pkg.github.com/${project.githubPackagesSlug()}")
            credentials {
                username =
                    System.getenv("GITHUB_ACTOR")?.takeIf { it.isNotEmpty() }
                        ?: project.findProperty("gpr.user")?.toString()
                password =
                    System.getenv("GITHUB_TOKEN")?.takeIf { it.isNotEmpty() }
                        ?: project.findProperty("gpr.key")?.toString()
            }
        }
    }
}

subprojects {
    group = rootProject.group
    version = rootProject.version

    if (name == "gclens-bom") {
        apply(plugin = "maven-publish")
        afterEvaluate {
            configure<PublishingExtension> {
                publications {
                    register<MavenPublication>("maven") {
                        from(components["javaPlatform"])
                        pom {
                            name.set("gclens-bom")
                            description.set("Gradle BOM for gclens")
                        }
                    }
                }
                mavenGitHubPackages(this@subprojects)
            }
        }
        return@subprojects
    }

    apply(plugin = "java-library")
    apply(plugin = "com.diffplug.spotless")
    apply(plugin = "maven-publish")

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

    configure<PublishingExtension> {
        publications {
            register<MavenPublication>("maven") {
                from(components["java"])
                pom {
                    name.set(project.name)
                    description.set("gclens module ${project.name}")
                }
            }
        }
        mavenGitHubPackages(this@subprojects)
    }
}
