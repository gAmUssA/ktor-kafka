import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    id("io.ktor.plugin") version "2.2.4"
    kotlin("jvm")
    `maven-publish`
}

val ak_version: String by project
val ktor_version: String by project
val testcontainers_version: String by project
val logback_version: String by project

group = "io.confluent.developer"
version = "0.0.1-SNAPSHOT"

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core")
    //region Kafka and Confluent
    api("org.apache.kafka:kafka-clients:$ak_version")
    api("org.apache.kafka:kafka-streams:$ak_version")
    //endregion

    implementation("ch.qos.logback:logback-classic:$logback_version")
    //junit5
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")

    implementation(platform("org.testcontainers:testcontainers-bom:$testcontainers_version"))
    testImplementation("org.testcontainers:kafka")
    testImplementation("org.awaitility:awaitility:4.2.0")
    testImplementation("org.assertj:assertj-core:3.24.2")

    testImplementation(kotlin("test-junit"))

    testImplementation("io.ktor:ktor-server-tests")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    testLogging {
        outputs.upToDateWhen { false }
        outputs.upToDateWhen { false }
        showStandardStreams = false
        events = setOf(PASSED, SKIPPED, FAILED)
        exceptionFormat = FULL
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
            from(components["java"])
        }
    }
}
