plugins {
    kotlin("jvm") version "1.4.30" apply false
}

allprojects {
    repositories {
        mavenCentral()
        jcenter()
        maven("https://packages.confluent.io/maven")
    }
}
