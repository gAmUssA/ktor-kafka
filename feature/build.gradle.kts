import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    kotlin("jvm")
    `maven-publish`
}

val ktor_version: String by project

group = "io.confluent.developer"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://kotlin.bintray.com/ktor")
}

dependencies {
    implementation("io.ktor:ktor-server-core:$ktor_version")
    //region Kafka and Confluent
    api("org.apache.kafka:kafka-clients:2.7.0")
    //endregion

    testImplementation(kotlin("test-junit"))
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = "1.8"
}
