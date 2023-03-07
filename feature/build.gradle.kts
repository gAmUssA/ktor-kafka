import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    kotlin("jvm")
    `maven-publish`
}

val ak_version: String by project
val ktor_version: String by project
val testcontainers_version: String by project

group = "io.confluent.developer"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core:$ktor_version")
    //region Kafka and Confluent
    api("org.apache.kafka:kafka-clients:$ak_version")
    api("org.apache.kafka:kafka-streams:$ak_version")
    //endregion

    //junit5
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")

    implementation(platform("org.testcontainers:testcontainers-bom:$testcontainers_version"))
    testImplementation("org.testcontainers:kafka")
    testImplementation("org.awaitility:awaitility:4.1.1")
    testImplementation("org.assertj:assertj-core:3.22.0")

    testImplementation(kotlin("test-junit"))
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}
