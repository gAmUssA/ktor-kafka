package io.confluent.developer

import io.confluent.developer.html.Html.indexHTML
import io.confluent.developer.kstreams.Rating
import io.confluent.developer.kstreams.ratingTopicName
import io.confluent.developer.kstreams.ratingsAvgTopicName
import io.confluent.developer.ktor.buildProducer
import io.confluent.developer.ktor.createKafkaConsumer
import io.confluent.developer.ktor.send
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import java.time.Duration

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module(testing: Boolean = false) {

    install(WebSockets)
    install(ContentNegotiation) {
        jackson()
    }

    //https://youtrack.jetbrains.com/issue/KTOR-2318
    val config = ApplicationConfig("kafka.conf")
    val producer: KafkaProducer<Long, Rating> = buildProducer(config)

    routing {
        //region static assets location
        static("/assets") {
            resources("META-INF/resources/assets")
        }
        //endregion

        post("rating") {
            val rating = call.receive<Rating>()

            producer.send(ratingTopicName, rating.movieId, rating)

            data class Status(val message: String)
            call.respond(HttpStatusCode.Accepted, Status("Accepted"))
        }

        webSocket("/kafka") {
            val consumer: KafkaConsumer<Long, Double> = createKafkaConsumer(config, ratingsAvgTopicName)
            try {
                while (true) {
                    consumer.poll(Duration.ofMillis(100))
                        .forEach {
                            outgoing.send(
                                Frame.Text(
                                    """{
                                "movieId":${it.key()},
                                "rating":${it.value()}
                                }
                            """.trimIndent()
                                )
                            )
                        }
                }
            } finally {
                consumer.apply {
                    unsubscribe()
                    //close()
                }
                //log.info("consumer for ${consumer.groupMetadata().groupId()} unsubscribed and closed...")
            }
        }
        get("/") {
            call.respondHtml(
                HttpStatusCode.OK,
                indexHTML
            )
        }
    }
}
