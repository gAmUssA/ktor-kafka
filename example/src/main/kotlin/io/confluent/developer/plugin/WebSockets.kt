package io.confluent.developer.plugin

import io.ktor.server.application.*
import io.ktor.server.websocket.*
import java.time.Duration

fun Application.configureWebsockets() {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
}
