plugins {
    kotlin("jvm") version "1.4.30" apply false
}

allprojects {
    repositories {
        mavenCentral()
        jcenter()
        google()
        maven("https://packages.confluent.io/maven")
        maven("https://repository.mulesoft.org/nexus/content/repositories/public/")
    }
}