package io.confluent.developer

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory.parseFile
import io.confluent.developer.html.Html.indexHTML
import io.confluent.developer.kstreams.Rating
import io.confluent.developer.kstreams.ratingTopicName
import io.confluent.developer.kstreams.ratingsAvgTopicName
import io.confluent.developer.ktor.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.html.*
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import java.io.File
import java.time.Duration

fun main(args: Array<String>): Unit = EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@JvmOverloads
fun Application.module(testing: Boolean = false) {

    //https://youtrack.jetbrains.com/issue/KTOR-2318
    val kafkaConfigPath = "src/main/resources/kafka.conf"

    install(ContentNegotiation) {
        jackson()
    }

    val config: Config = parseFile(File(kafkaConfigPath))
    val producer: KafkaProducer<Long, Rating> = buildProducer(config)

    install(WebSockets)
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
                log.info("consumer for ${consumer.groupMetadata().groupId()} unsubscribed and closed...")
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
