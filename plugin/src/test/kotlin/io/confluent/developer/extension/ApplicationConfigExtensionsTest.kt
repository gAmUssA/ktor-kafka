package io.confluent.developer.extension

import com.typesafe.config.ConfigFactory
import io.confluent.developer.ktor.effectiveStreamProperties
import io.ktor.server.config.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class ApplicationConfigExtensionsTest {

    private val config = ApplicationConfig("kafka-config-map.conf")

    @Test
    @DisplayName("should extract config value based on path - config from file")
    fun testPathConfig() {
        val map = config.toMap("ktor.kafka.producer")

        assertThat(map["key.serializer"])
            .isEqualTo("org.apache.kafka.common.serialization.LongSerializer")
    }

    @Test
    @DisplayName("should extract config for streams based on path - config from file")
    fun testStreamsConfig() {
        val map = config.toMap("ktor.kafka.streams")

        assertThat(map["application.id"])
            .isEqualTo("ktor-stream")
    }

    @Test
    @DisplayName("effectiveConfig should produce correct config for streams")
    fun effectiveConfigTest() {
        effectiveStreamProperties(config)

        assertThat(config.toMap("ktor.kafka.streams")["application.id"])
            .isEqualTo("ktor-stream")
        assertThat(config.toMap("ktor.kafka.properties")["schema.registry.url"])
            .isEqualTo("http://localhost:8081")
    }

    @Test
    @DisplayName("should extract configs value based on path - inline config")
    fun testToMapWithPath() {
        val string = """
            ktor {
              kafka {
                producer {
                  value.serializer = KafkaJsonSchemaSerializer
                  key {
                    serializer =  LongSerializer
                  }
                }
              }
            }
        """.trimIndent()

        val config = HoconApplicationConfig(ConfigFactory.parseString(string))
        val path = "ktor.kafka.producer"
        val map = config.toMap(path)
        //var map = configMap(config, path)

        assertEquals("LongSerializer", map["key.serializer"])
        assertEquals("KafkaJsonSchemaSerializer", map["value.serializer"])
    }

}
