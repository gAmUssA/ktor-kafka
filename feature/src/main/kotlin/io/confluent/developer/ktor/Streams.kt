package io.confluent.developer.ktor

import com.typesafe.config.Config
import io.confluent.developer.extension.configMap
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.Serdes.serdeFrom
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.Produced
import java.util.*

fun streams(
    t: Topology,
    config: Config
): KafkaStreams {
    val p: Properties = effectiveStreamProperties(config)
    return KafkaStreams(t, p)
}

fun effectiveStreamProperties(config: Config): Properties {
    val bootstrapServers = config.getList("ktor.kafka.bootstrap.servers")

    // common config
    val commonConfig = configMap(config, "ktor.kafka.properties")
    // kafka streams
    val streamsConfig = configMap(config, "ktor.kafka.streams")

    return Properties().apply {
        putAll(commonConfig)
        putAll(streamsConfig)
        put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers.unwrapped())
    }
}

inline fun <reified K, reified V> producedWith(): Produced<K, V> = Produced.with(serdeFrom(K::class.java), serdeFrom(V::class.java))
