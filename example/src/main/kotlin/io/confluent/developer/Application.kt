package io.confluent.developer

import io.confluent.developer.extension.logger
import io.confluent.developer.kstreams.Rating
import io.confluent.developer.kstreams.ratingTopicName
import io.confluent.developer.kstreams.ratingsAvgTopicName
import io.confluent.developer.ktor.buildProducer
import io.confluent.developer.ktor.createKafkaConsumer
import io.confluent.developer.ktor.send
import io.confluent.developer.plugin.configureDefaultHeaders
import io.confluent.developer.plugin.configureRouting
import io.confluent.developer.plugin.configureWebsockets
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import java.time.Duration

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module(testing: Boolean = false) {
    val log = logger<Application>()

    configureDefaultHeaders()
    configureWebsockets()

    //https://youtrack.jetbrains.com/issue/KTOR-2318
    val config = ApplicationConfig("kafka.conf")
    val producer: KafkaProducer<Long, Rating> = buildProducer(config)

    configureRouting()
    routing {
        post("rating") {
            val rating = call.receive<Rating>()

            producer.send(ratingTopicName, rating.movieId, rating)

            data class Status(val message: String)
            call.respond(HttpStatusCode.Accepted, Status("Accepted"))
        }

        webSocket("/kafka") {

            val clientId = call.parameters["clientId"] ?: "¯\\_(ツ)_/¯"
            log.debug("clientId {}", clientId)
            val consumer: KafkaConsumer<Long, Double> =
                createKafkaConsumer(config, ratingsAvgTopicName, "ws-consumer-$clientId")
            try {
                while (true) {
                    poll(consumer)
                }
            } finally {
                consumer.apply {
                    unsubscribe()
                    //close()
                }
                log.info("consumer for ${consumer.groupMetadata().groupId()} unsubscribed and closed...")
            }
        }
    }
}

// https://discuss.kotlinlang.org/t/calling-blocking-code-in-coroutines/2368/5
// https://discuss.kotlinlang.org/t/coroutines-with-blocking-apis-such-as-jdbc/10669/4
// https://stackoverflow.com/questions/57650163/how-to-properly-make-blocking-service-calls-with-kotlin-coroutines
private suspend fun DefaultWebSocketServerSession.poll(consumer: KafkaConsumer<Long, Double>) =
    withContext(Dispatchers.IO) {
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

