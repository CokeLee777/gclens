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

import org.gradle.api.file.DuplicatesStrategy

plugins {
    id("application")
}

application {
    mainClass = "io.github.cokelee777.gclens.cli.GCLensReporter"
    applicationName = "gclens"
}

dependencies {
    implementation(project(":gclens-engine"))
    runtimeOnly(project(":gclens-g1gc"))
    testImplementation(project(":gclens-g1gc"))
    implementation(rootProject.libs.picocli)
    annotationProcessor(rootProject.libs.picocli.codegen)
}

tasks.jar {
    dependsOn(configurations.runtimeClasspath)
    manifest {
        attributes["Main-Class"] = application.mainClass.get()
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from({
        configurations.runtimeClasspath.get()
            .filter { it.name.endsWith(".jar") }
            .map { zipTree(it) }
    })
}
