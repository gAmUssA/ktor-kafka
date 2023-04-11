package io.confluent.developer.plugin

import io.confluent.developer.html.Html
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    install(ContentNegotiation) {
        jackson()
    }
    routing {
        //region static assets location
        static("/assets") {
            resources("META-INF/resources/assets")
        }

        get("/") {
            call.respondHtml(
                HttpStatusCode.OK,
                Html.indexHTML
            )
        }
    }
}
