
val logback_version: String by project
val ktor_version: String by project
val kotlin_version: String by project
val confluent_version: String by project
val ak_version: String by project

plugins {
    application
    kotlin("jvm")
    id("io.ktor.plugin") version "2.2.3"
    id("com.avast.gradle.docker-compose") version "0.14.1"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "io.confluent.developer"
version = "0.0.1-SNAPSHOT"

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

dockerCompose.isRequiredBy(project.tasks.named("run"))

repositories {
    mavenCentral()
    //region extras
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
    //endregion
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
        vendor.set(JvmVendorSpec.AZUL)
    }
//    targetCompatibility = JavaVersion.VERSION_17
//    sourceCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(project(":feature"))
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.confluent:kafka-json-schema-serializer:$confluent_version")

    implementation("io.confluent:kafka-streams-json-schema-serde:$confluent_version") {
        exclude("org.apache.kafka", "kafka-clients")
    }

    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-locations")
    implementation("io.ktor:ktor-server-html-builder")
    implementation("io.ktor:ktor-serialization")
    implementation("io.ktor:ktor-server-netty")
    implementation("io.ktor:ktor-server-websockets")
    implementation("io.ktor:ktor-serialization-jackson")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-server-websockets")
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-websockets-jvm")
    testImplementation("io.ktor:ktor-server-tests")

    testImplementation("org.apache.kafka:kafka-streams-test-utils:$ak_version")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = "17"
}
