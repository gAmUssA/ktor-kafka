package io.confluent.developer.ktor

import com.typesafe.config.Config
import io.confluent.developer.extension.configMap
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.ProducerConfig
import java.util.*

fun <K, V> buildConsumer(config: Config): KafkaConsumer<K, V> {
    val bootstrapServers = config.getList("ktor.kafka.bootstrap.servers")

    // common config
    val commonConfig = configMap(config, "ktor.kafka.properties")

    // get consumer config
    val consumerConfig = configMap(config, "ktor.kafka.consumer")

    val consumerProperties: Properties = Properties().apply {
        putAll(commonConfig)
        putAll(consumerConfig)
        put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers.unwrapped())
        put("group.id", "ktor-consumer-"+Random().nextInt())
    }

    return KafkaConsumer(consumerProperties)
}

fun <K, V> createKafkaConsumer(config: Config, topic: String): KafkaConsumer<K, V> {
    val consumer = buildConsumer<K, V>(config)
    consumer.subscribe(listOf(topic))
    return consumer
}
