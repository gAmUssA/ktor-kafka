package io.confluent.developer

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory.parseFile
import io.confluent.developer.ktor.*
import io.ktor.application.*
import io.ktor.html.*
import io.ktor.http.cio.websocket.*
import io.ktor.http.content.*
import io.ktor.routing.*
import io.ktor.server.netty.*
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

    val kafkaConfigPath = "src/main/resources/kafka.conf"
    val topicName = "myTopic"

    
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    //region Kafka
    install(Kafka) {
        configurationPath = kafkaConfigPath
        topics = listOf(
            newTopic(topicName) {
                partitions = 3
                replicas = 1 // for docker
                //replicas = 3 // for cloud
            }
        )
    }
    //endregion
    val config: Config = parseFile(File(kafkaConfigPath))
    val producer: KafkaProducer<String, Question> = buildProducer(config)
    val consumer: KafkaConsumer<String, Question> = createKafkaConsumer(config, topicName)

    routing {
        //region static assets location
        static("assets") {
            resources("META-INF/resources/assets")
        }
        //endregion
        // TODO: parse post body
        post("/kafka") {
            val question = Question(
                "http://gamov.dev/rel",
                "just test", 42,
                1024,
                listOf("kafka", "streams", "java"),
                "\uD83D\uDCAA"
            )
            producer.send(topicName, question.url, question)
        }

        get("/") {
            call.respondHtml {
                head {
                    title("hello to ktor kafka")
                    js("/assets/index.js")
                }
                body {
                    h1 { +"Ktor Kafka example" }
                    p {
                        +"POST (http POST :8080/kafka) to"
                        a("/kafka")
                        +"to test"
                    }
                }
            }
        }

        webSocket("/kafka") {
            try {
                while (true) {
                    
                    consumer.poll(Duration.ofMillis(100))
                        .forEach {
                            val value: Question = it.value()
                                    
                            outgoing.send(Frame.Text(value.toString()))
                        }
                }
            } finally {
                consumer.apply {
                    unsubscribe()
                    close()
                }
                log.info("consumer for ${consumer.groupMetadata().groupId()} unsubscribed and closed...")
            }
        }
    }
}

private fun HEAD.js(source: String) {
    script(ScriptType.textJavaScript) {
        src = source
    }
}
