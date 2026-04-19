rootProject.name = "gclens"

include(
    "gclens-bom",
    "gclens-model",
    "gclens-parse",
    "gclens-insights",
    "gclens-g1gc",
    "gclens-engine",
    "gclens-cli",
)

project(":gclens-g1gc").projectDir = file("models/gclens-g1gc")
