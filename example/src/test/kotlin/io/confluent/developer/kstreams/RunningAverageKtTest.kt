package io.confluent.developer.kstreams

import io.confluent.kafka.streams.serdes.json.KafkaJsonSchemaSerde
import org.apache.kafka.common.serialization.DoubleDeserializer
import org.apache.kafka.common.serialization.LongDeserializer
import org.apache.kafka.common.serialization.LongSerializer
import org.apache.kafka.streams.*
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.state.KeyValueStore
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.*


class RunningAverageTest {
    private lateinit var testDriver: TopologyTestDriver
    private var ratingSpecificAvroSerde: KafkaJsonSchemaSerde<Rating>? = null

    @Before
    fun setUp() {
        val mockProps = Properties()
        mockProps["application.id"] = "kafka-movies-test"
        mockProps["bootstrap.servers"] = "DUMMY_KAFKA_CONFLUENT_CLOUD_9092"
        mockProps["schema.registry.url"] = "mock://DUMMY_SR_CONFLUENT_CLOUD_8080"

        val builder = StreamsBuilder()
        val countAndSumSerde: KafkaJsonSchemaSerde<CountAndSum> = jsonSchemaSerde(mockProps, false)
        ratingSpecificAvroSerde = jsonSchemaSerde(mockProps, false)

        val ratingStream: KStream<Long, Rating> = ratingsStream(builder, mockProps)

        getRatingAverageTable(
            ratingStream,
            AVERAGE_RATINGS_TOPIC_NAME,
            countAndSumSerde
        )
        val topology = builder.build()
        testDriver = TopologyTestDriver(topology, mockProps)
    }

    @Test
    fun validateIfTestDriverCreated() {
        Assert.assertNotNull(testDriver)
    }

    @Test
    fun validateAverageRating() {
        val inputTopic: TestInputTopic<Long, Rating> = testDriver.createInputTopic(
            RATINGS_TOPIC_NAME,
            LongSerializer(),
            ratingSpecificAvroSerde?.serializer()
        )
        inputTopic.pipeKeyValueList(
            listOf(
                KeyValue(LETHAL_WEAPON_RATING_8.movieId, LETHAL_WEAPON_RATING_8),
                KeyValue(LETHAL_WEAPON_RATING_10.movieId, LETHAL_WEAPON_RATING_10)
            )
        )
        val outputTopic: TestOutputTopic<Long, Double> = testDriver.createOutputTopic(
            AVERAGE_RATINGS_TOPIC_NAME,
            LongDeserializer(),
            DoubleDeserializer()
        )
        val keyValues: List<KeyValue<Long, Double>> = outputTopic.readKeyValuesToList()
        // I sent two records to input topic
        // I expect second record in topic will contain correct result
        val longDoubleKeyValue = keyValues[1]
        println("longDoubleKeyValue = $longDoubleKeyValue")
        MatcherAssert.assertThat(
            longDoubleKeyValue,
            CoreMatchers.equalTo(KeyValue(362L, 9.0))
        )
        val keyValueStore: KeyValueStore<Long, Double> = testDriver.getKeyValueStore("average-ratings")
        val expected = keyValueStore[362L]
        Assert.assertEquals("Message", expected, 9.0, 0.0)
    }

    @After
    fun tearDown() {
        testDriver.close()
    }

    companion object {
        private const val RATINGS_TOPIC_NAME = "ratings"
        private const val AVERAGE_RATINGS_TOPIC_NAME = "average-ratings"
        private val LETHAL_WEAPON_RATING_10 = Rating(362L, 10.0)
        private val LETHAL_WEAPON_RATING_8 = Rating(362L, 8.0)
    }
}
