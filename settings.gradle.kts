pluginManagement {
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
        jcenter()
    }
}
rootProject.name = "ktor-kafka"

include("feature", "example")
