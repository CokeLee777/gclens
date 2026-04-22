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

plugins {
    id("java-platform")
}

dependencies {
    constraints {
        api("io.github.cokelee777:gclens-model:${project.version}")
        api("io.github.cokelee777:gclens-parse:${project.version}")
        api("io.github.cokelee777:gclens-insights:${project.version}")
        api("io.github.cokelee777:gclens-g1gc:${project.version}")
        api("io.github.cokelee777:gclens-engine:${project.version}")
        api("io.github.cokelee777:gclens-cli:${project.version}")
    }
}