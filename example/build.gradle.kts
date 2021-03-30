plugins {
    application
    kotlin("jvm")
    id("com.avast.gradle.docker-compose") version "0.14.1"
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

val logback_version: String by project
val ktor_version: String by project
val kotlin_version: String by project
val confluent_version: String by project
val ak_version: String by project

group = "io.confluent.developer"
version = "0.0.1-SNAPSHOT"

application {
    // Replace with mainClass when PR612 is released (shadow 6.2.0?)
    // see https://github.com/johnrengelman/shadow/pull/612
    // mainClass.set("io.ktor.server.netty.EngineMain")
    @Suppress("DEPRECATION")
    mainClassName = "io.ktor.server.netty.EngineMain"
}

dockerCompose.isRequiredBy(project.tasks.named("run"))

repositories {
    mavenCentral()
    maven("https://kotlin.bintray.com/ktor") {
        content {
            includeGroup("io.ktor")
        }
    }
    maven("https://repository.mulesoft.org/nexus/content/repositories/public/") {
        content {
            includeModule("com.github.everit-org.json-schema", "org.everit.json.schema")
        }
    }
    maven("https://packages.confluent.io/maven") {
        content {
            includeGroup("io.confluent")
            includeModule("org.apache.kafka", "kafka-clients")
        }
    }
    // Remove when kotlinx-html is published to Central: https://github.com/Kotlin/kotlinx.html/issues/173
    jcenter {
        content {
            includeModule("org.jetbrains.kotlinx", "kotlinx-html-jvm")
        }
    }
}

dependencies {
    implementation(project(":feature"))
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.confluent:kafka-json-schema-serializer:$confluent_version")

    implementation("io.confluent:kafka-streams-json-schema-serde:$confluent_version") {
        exclude("org.apache.kafka", "kafka-clients")
    }
    implementation(platform("io.ktor:ktor-bom:$ktor_version"))
    implementation("io.ktor:ktor-locations")
    implementation("io.ktor:ktor-html-builder")
    implementation("io.ktor:ktor-serialization")
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-netty")
    implementation("io.ktor:ktor-websockets")
    implementation("io.ktor:ktor-jackson")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.1.0")
    testImplementation("io.ktor:ktor-server-tests")
    testImplementation("org.apache.kafka:kafka-streams-test-utils:$ak_version")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = "1.8"
}
