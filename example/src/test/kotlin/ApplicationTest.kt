package io.confluent.developer

import io.confluent.developer.ktor.buildConsumer
import io.confluent.developer.ktor.buildProducer
import io.ktor.server.config.*
import io.ktor.server.testing.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.utility.DockerImageName


class ApplicationTest {

    companion object {
        @JvmStatic
        val kafka = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.3.1")).apply {
            start()
        }

    }

    @Test
    fun testDevEnvironment() = testApplication {
        environment {
            val custom = MapApplicationConfig("ktor.kafka.bootstrap.servers" to kafka.bootstrapServers)
            config = ApplicationConfig("kafka.conf").mergeWith(custom)
        }

        application {
            val applicationConfig = environment.config
            var producer = buildProducer<String, String>(applicationConfig)



            //assertThat(applicationConfig.property("ktor.kafka.bootstrap.servers").getString()).isEqualTo("stuff")
        }

    }

}

