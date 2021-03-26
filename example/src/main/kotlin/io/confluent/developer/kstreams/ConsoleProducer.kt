package io.confluent.developer.kstreams

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import io.confluent.developer.ktor.buildProducer
import io.confluent.developer.ktor.send
import java.io.File

fun main() {
    val kafkaConfigPath = "src/main/resources/kafka.conf"
    val config: Config = ConfigFactory.parseFile(File(kafkaConfigPath))
    val producer = buildProducer<Long, Rating>(config)

    val rating10 = Rating(movieId = 362, rating = 10.0)
    val rating8 = Rating(movieId = 362, rating = 8.0)
    producer.send("ratings", rating10.movieId, rating10)
    producer.send("ratings", rating8.movieId, rating8)
    producer.flush()
    producer.close()
}
