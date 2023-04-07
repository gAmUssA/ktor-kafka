package io.confluent.developer.kstreams

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import io.confluent.developer.ktor.*
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig.BASIC_AUTH_CREDENTIALS_SOURCE
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig.USER_INFO_CONFIG
import io.confluent.kafka.streams.serdes.json.KafkaJsonSchemaSerde
import io.ktor.server.application.*
import io.ktor.server.netty.*
import org.apache.kafka.common.serialization.Serdes.*
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.*
import org.apache.kafka.streams.kstream.Grouped.with
import org.apache.kafka.streams.state.KeyValueStore
import java.io.File
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.util.*

fun main(args: Array<String>): Unit = EngineMain.main(args)

const val ratingTopicName = "ratings"
const val ratingsAvgTopicName = "rating-averages"

fun Application.module(testing: Boolean = false) {
    lateinit var streams: KafkaStreams

    // load properties
    val kafkaConfigPath = "src/main/resources/kafka.conf"
    val config: Config = ConfigFactory.parseFile(File(kafkaConfigPath))
    val properties = effectiveStreamProperties(config)

    //region Kafka
    install(Kafka) {
        configurationPath = kafkaConfigPath
        topics = listOf(
            newTopic(ratingTopicName) {
                partitions = 3
                replicas = 1    // for docker
                //replicas = 3  // for cloud
            },
            newTopic(ratingsAvgTopicName) {
                partitions = 3
                replicas = 1    // for docker
                //replicas = 3  // for cloud
            }
        )
    }
    //endregion

    val streamsBuilder = StreamsBuilder()
    val topology = buildTopology(streamsBuilder, properties)
    // if you want to see a visual representation of the Kafka Stream Topology uncomment below
    //
    //var encodeToString = Base64.getEncoder().encodeToString(topology.describe().toString().toByteArray(StandardCharsets.UTF_8))
    //log.debug("https://gaetancollaud.github.io/kafka-streams-visualization/#${encodeToString}")

    streams = streams(topology, config)

    environment.monitor.subscribe(ApplicationStarted) {
        streams.cleanUp()
        streams.start()
        log.info("Kafka Streams app is ready to roll...")
    }

    environment.monitor.subscribe(ApplicationStopped) {
        log.info("Time to clean up...")
        streams.close(Duration.ofSeconds(5))
    }
}

fun buildTopology(
    builder: StreamsBuilder,
    properties: Properties
): Topology {

    val ratingStream: KStream<Long, Rating> = ratingsStream(builder, properties)

    getRatingAverageTable(
        ratingStream,
        ratingsAvgTopicName,
        jsonSchemaSerde(properties, false)
    )
    return builder.build()
}

fun ratingsStream(builder: StreamsBuilder, properties: Properties): KStream<Long, Rating> {
    return builder.stream(
        ratingTopicName,
        Consumed.with(Long(), jsonSchemaSerde(properties, false))
    )
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
            aggregate.count = aggregate.count + 1
            aggregate.sum = aggregate.sum + value
            aggregate
        },
        Materialized.with(Long(), countAndSumSerde)
    )

    val ratingAverage: KTable<Long, Double> = ratingCountAndSum.mapValues(
        { value -> value.sum.div(value.count) },
        Materialized.`as`<Long, Double, KeyValueStore<Bytes, ByteArray>>("average-ratings")
            .withKeySerde(LongSerde())
            .withValueSerde(DoubleSerde())
    )

    // persist the result in topic
    val stream = ratingAverage.toStream()
    //stream.peek { key, value -> println("$key:$value") }
    stream.to(avgRatingsTopicName, producedWith<Long, Double>())
    return ratingAverage
}

inline fun <reified V> jsonSchemaSerde(
    properties: Properties,
    isKeySerde: Boolean
): KafkaJsonSchemaSerde<V> {
    val schemaSerde = KafkaJsonSchemaSerde(V::class.java)
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
