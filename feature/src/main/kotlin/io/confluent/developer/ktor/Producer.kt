package io.confluent.developer.ktor

import io.confluent.developer.extension.configMap
import io.ktor.server.config.*
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig.*
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import java.util.*
import java.util.concurrent.Future

fun <K, V> buildProducer(config: ApplicationConfig): KafkaProducer<K, V> {
    val bootstrapServers: List<String> = config.property("ktor.kafka.bootstrap.servers").getList()
    // common config
    val commonConfig = configMap(config, "ktor.kafka.properties")
    // get producer config
    val producerConfig = configMap(config, "ktor.kafka.producer")
    // creating properties
    val producerProperties: Properties = Properties().apply {
        putAll(producerConfig)
        putAll(commonConfig)
        put(BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
    }
    return KafkaProducer(producerProperties)
}

fun <K, V> KafkaProducer<K, V>.send(topicName: String, key: K, value: V): Future<RecordMetadata>? {
    return this.send(ProducerRecord(topicName, key, value))
}

