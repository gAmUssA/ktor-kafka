package io.confluent.developer


class ApplicationTest {
/*

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
*/

}

