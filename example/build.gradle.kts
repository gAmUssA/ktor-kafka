val logback_version: String by project
val ktor_version: String by project
val kotlin_version: String by project
val confluent_version: String by project
val ak_version: String by project

plugins {
    application
    kotlin("jvm")
    id("io.ktor.plugin") version "2.2.4"
    id("com.avast.gradle.docker-compose") version "0.16.12"
    id("com.github.johnrengelman.shadow") version "8.1.1"
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
    }
}

dependencies {
    implementation(project(":plugin"))
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
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-websockets-jvm")
    /*    implementation("io.ktor:ktor-server-webjars")
        implementation("org.webjars.npm:nanoid:4.0.1")*/


    //junit5
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")

    testImplementation("org.assertj:assertj-core:3.22.0")

    testImplementation("org.apache.kafka:kafka-streams-test-utils:$ak_version")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}
