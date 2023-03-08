package io.confluent.developer.ktor

import io.confluent.developer.extension.configMap
import io.confluent.developer.extension.toMap
import io.ktor.server.config.*
import org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG
import org.apache.kafka.clients.consumer.KafkaConsumer
import java.util.*

fun <K, V> buildConsumer(config: ApplicationConfig): KafkaConsumer<K, V> {
    val bootstrapServers: List<String> = config.property("ktor.kafka.bootstrap.servers").getList()

    // common config
    val commonConfig = config.toMap("ktor.kafka.properties")

    // get consumer config
    val consumerConfig = config.toMap("ktor.kafka.consumer")

    val consumerProperties: Properties = Properties().apply {
        putAll(commonConfig)
        putAll(consumerConfig)
        put(BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
    }

    return KafkaConsumer(consumerProperties)
}

fun <K, V> createKafkaConsumer(config: ApplicationConfig, topic: String): KafkaConsumer<K, V> {
    val consumer = buildConsumer<K, V>(config)
    consumer.subscribe(listOf(topic))
    return consumer
}
