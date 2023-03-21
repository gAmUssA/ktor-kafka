package io.confluent.developer.kstreams

import io.confluent.developer.ktor.buildProducer
import io.confluent.developer.ktor.send
import io.ktor.server.config.*

fun main() {

    val applicationConfig = ApplicationConfig("kafka.conf")
    val producer = buildProducer<Long, Rating>(applicationConfig)

    val rating10 = Rating(movieId = 362, rating = 10.0)
    val rating8 = Rating(movieId = 362, rating = 8.0)
    producer.send("ratings", rating10.movieId, rating10)
    producer.send("ratings", rating8.movieId, rating8)
    producer.flush()
    producer.close()
}
