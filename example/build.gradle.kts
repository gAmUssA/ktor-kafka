plugins {
    application
    kotlin("jvm")  
    id("com.avast.gradle.docker-compose") version "0.14.1"
}

val logback_version: String by project
val ktor_version: String by project
val kotlin_version: String by project

group = "io.confluent.developer"
version = "0.0.1-SNAPSHOT"

application {
    // TODO: mainClass.set doesn't work with shadowJar ???
    // ref https://ktor.io/docs/fatjar.html#fat-jar-gradle 
    mainClassName = "io.ktor.server.netty.EngineMain"
}

dockerCompose.isRequiredBy(project.tasks.named("run"))

tasks.withType<Jar> {
    manifest {
        attributes(
            mapOf(
                "Main-Class" to application.mainClassName
            )
        )
    }
}

repositories {
    mavenCentral()
    jcenter()
    maven { url = uri("https://kotlin.bintray.com/ktor") }
}

dependencies {
    implementation(project(":feature"))
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-html-builder:$ktor_version")
    implementation("io.ktor:ktor-websockets:$ktor_version")
    testImplementation("io.ktor:ktor-server-tests:$ktor_version")
}
