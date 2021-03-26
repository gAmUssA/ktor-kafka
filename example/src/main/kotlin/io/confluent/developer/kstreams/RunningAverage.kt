package io.confluent.developer.kstreams

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import io.confluent.developer.ktor.*
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig.BASIC_AUTH_CREDENTIALS_SOURCE
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig.USER_INFO_CONFIG
import io.confluent.kafka.streams.serdes.json.KafkaJsonSchemaSerde
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.serialization.Serdes.*
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.*
import org.apache.kafka.streams.kstream.Grouped.with
import org.apache.kafka.streams.state.KeyValueStore
import java.io.File
import java.time.Duration
import java.util.*
import java.util.concurrent.CountDownLatch
import kotlin.system.exitProcess

fun main() {
    // load properties
    val kafkaConfigPath = "src/main/resources/kafka.conf"
    val config: Config = ConfigFactory.parseFile(File(kafkaConfigPath))
    val properties = effectiveStreamProperties(config)

    createTopics(properties)

    val streamsBuilder = StreamsBuilder()
    val topology = buildTopology(streamsBuilder, properties);
    println(topology.describe().toString())
    val streams = streams(topology, config)
    val latch = CountDownLatch(1)
    // Attach shutdown handler to catch Control-C.
    Runtime.getRuntime().addShutdownHook(object : Thread("streams-shutdown-hook") {
        override fun run() {
            streams.close(Duration.ofSeconds(5))
            latch.countDown()
        }
    })
    try {
        streams.cleanUp()
        streams.start()
        latch.await()
    } catch (e: Throwable) {
        exitProcess(1)
    }
    exitProcess(0)
}

fun buildTopology(
    bldr: StreamsBuilder,
    properties: Properties
): Topology {

    val ratingTopicName = "ratings"

    val ratingStream: KStream<Long, Rating> = bldr.stream(
        ratingTopicName,
        Consumed.with(Long(), jsonSchemaSerde<Rating>(properties, false))
    )

    val avgRatingsTopicName = "rating-averages"
    getRatingAverageTable(
        ratingStream,
        avgRatingsTopicName,
        jsonSchemaSerde(properties, false)
    )
    // finish the topology
    return bldr.build()
}

fun getRatingAverageTable(
    ratings: KStream<Long, Rating>,
    avgRatingsTopicName: String,
    countAndSumSerde: KafkaJsonSchemaSerde<CountAndSum>
): KTable<Long, Double> {

    // Grouping Ratings
    val ratingsById: KGroupedStream<Long, Double> = ratings
        .map { _, rating -> KeyValue(rating.movieId, rating.rating) }
        .groupByKey(with(Long(), Double()))
    val ratingCountAndSum: KTable<Long, CountAndSum> = ratingsById.aggregate(
        { CountAndSum(0L, 0.0) },
        { _, value, aggregate ->
            aggregate.count = aggregate.count++
            aggregate.sum = aggregate.sum + value
            aggregate
        },
        Materialized.with(Long(), countAndSumSerde)
    )
    val materialized = Materialized.`as`<Long, Double, KeyValueStore<Bytes, ByteArray>>("average-ratings")
        .withKeySerde(LongSerde())
        .withValueSerde(DoubleSerde())
    val ratingAverage: KTable<Long, Double> = ratingCountAndSum.mapValues(
        { value -> value.sum / value.count },
        materialized
    )

    // persist the result in topic
    val stream = ratingAverage.toStream()
    stream.peek { key, value -> println("$key:$value") }
    stream.to(avgRatingsTopicName, producedWith<Long, Double>())
    return ratingAverage
}

inline fun <reified V> jsonSchemaSerde(
    properties: Properties,
    isKeySerde: Boolean
): KafkaJsonSchemaSerde<V> {
    val schemaSerde = KafkaJsonSchemaSerde<V>(V::class.java)
    val crSource = properties[BASIC_AUTH_CREDENTIALS_SOURCE]
    val uiConfig = properties[USER_INFO_CONFIG]

    val map = mutableMapOf(
        "schema.registry.url" to properties["schema.registry.url"]
    )
    crSource?.let {
        map[BASIC_AUTH_CREDENTIALS_SOURCE] = crSource
    }
    uiConfig?.let {
        map[USER_INFO_CONFIG] = uiConfig
    }
    schemaSerde.configure(map, isKeySerde)
    return schemaSerde;
}

//region createTopics
/**
 * Create topics using AdminClient API
 */
private fun createTopics(props: Properties) {
    val topics = listOf("ratings", "rating-averages")
    kafkaAdmin(props) {
        createTopics(topics.map {
            newTopic(it) {
                partitions = 3
                replicas = 1
            }
        })
    }
}
//endregion
