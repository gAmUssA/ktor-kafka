package io.confluent.developer.ktor

import com.typesafe.config.ConfigFactory
import io.ktor.server.config.*
import io.ktor.server.testing.*
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.awaitility.Awaitility.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.utility.DockerImageName.parse
import java.time.Duration
import java.util.concurrent.*

class ProducerConsumerIntegrationTest {

    companion object {
        lateinit var kafka: KafkaContainer
        //private val log = logger<ProducerKtTest>()

        @JvmStatic
        @BeforeAll
        fun setUp() {
            kafka = KafkaContainer(parse("confluentinc/cp-kafka:7.3.1")).apply {
                //only for test
                //withLogConsumer(Slf4jLogConsumer(log))
                withEnv("KAFKA_AUTO_CREATE_TOPIC_ENABLE", "true")
                start()
            }
        }
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    @DisplayName("should start KafkaProducer send to topic and KafkaConsumer consume from topic")
    fun testDevEnvironment() = testApplication {
        val stringSerializerName = StringSerializer::class.java.name
        val stringDeserializerName = StringDeserializer::class.java.name

        environment {
            val string = """
            ktor {
              kafka {
                bootstrap.servers = ["${kafka.bootstrapServers}"]
                 properties {
                    schema.registry.url = "http://localhost:8081"
                }
                producer {
                  client.id = "ktor-producer"
                  key.serializer = $stringSerializerName
                  value.serializer = $stringSerializerName
                }
                consumer {
                  group.id = "ktor-test"
                  key.deserializer = $stringDeserializerName
                  value.deserializer = $stringDeserializerName
                  ${ConsumerConfig.AUTO_OFFSET_RESET_CONFIG} = "earliest"
                }
              }
            }
        """.trimIndent()

            val custom = HoconApplicationConfig(ConfigFactory.parseString(string));
            //var applicationConfig = ApplicationConfig("kafka-config-map.conf")
            config = custom
            //config = custom.mergeWith(applicationConfig)
        }

        val topic = "test1"
        application {
            val applicationConfig = environment.config

            // create test1 topic
            kafkaAdmin(applicationConfig) {
                createTopics(listOf(newTopic(topic) {
                    partitions = 1
                    replicas = 1
                }))
            }

            var producer = buildProducer<String, String>(applicationConfig)
            var consumer = createKafkaConsumer<String, String>(applicationConfig, topic)

            /*Stream.of("A", "B", "B", "A")
                .map { e -> ProducerRecord(topic, e, e) }
                .forEach(producer::send)
            producer.flush()*/
            producer.send(topic, "testKey", "testValue")
            producer.flush()

            //We are using thread-safe data structure here, since it's shared between consumer and verifier
            val actual: MutableList<String> = CopyOnWriteArrayList()
            val service: ExecutorService = Executors.newSingleThreadExecutor()
            val consumingTask: Future<*> = service.submit {
                while (!Thread.currentThread().isInterrupted) {
                    val records: ConsumerRecords<String, String> =
                        consumer.poll(Duration.ofMillis(100))
                    for (rec in records) {
                        actual.add(rec.value())
                    }
                }
            }

            try {
                await()
                    .atMost(5, TimeUnit.SECONDS)
                    .until { listOf("testValue") == actual }
            } finally {
                consumingTask.cancel(true)
                service.awaitTermination(200, TimeUnit.MILLISECONDS)
            }
        }
    }
}
