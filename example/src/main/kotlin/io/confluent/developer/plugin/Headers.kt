package io.confluent.developer.plugin

import io.ktor.server.application.*
import io.ktor.server.plugins.defaultheaders.*

fun Application.configureDefaultHeaders() {
    install(DefaultHeaders) {
        header("X-Engine", "Ktor")
        header("X-Ktor-Version", "2.2.4")
    }
}
