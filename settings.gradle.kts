pluginManagement {
    plugins {
        kotlin("jvm") version "1.4.30"
    }
}
rootProject.name = "ktor-kafka"

include("feature", "example")
