pluginManagement {
    plugins {
        kotlin("jvm") version "1.8.10"
    }
}

rootProject.name = "ktor-kafka"

include("feature", "example")
