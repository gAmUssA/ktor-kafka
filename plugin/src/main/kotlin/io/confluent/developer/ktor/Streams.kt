package io.confluent.developer.ktor

import io.confluent.developer.extension.toMap
import io.ktor.server.config.*
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.Serdes.serdeFrom
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.Produced
import java.util.*

fun streams(
    t: Topology,
    config: ApplicationConfig,
): KafkaStreams {
    val p: Properties = effectiveStreamProperties(config)
    return KafkaStreams(t, p)
}

fun effectiveStreamProperties(config: ApplicationConfig): Properties {
    val bootstrapServers: List<String> = config.property("ktor.kafka.bootstrap.servers").getList()

    // common config
    val commonConfig = config.toMap("ktor.kafka.properties")
    // kafka streams
    val streamsConfig = config.toMap("ktor.kafka.streams")

    return Properties().apply {
        putAll(commonConfig)
        putAll(streamsConfig)
        put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
    }
}

inline fun <reified K, reified V> producedWith(): Produced<K, V> =
    Produced.with(serdeFrom(K::class.java), serdeFrom(V::class.java))
