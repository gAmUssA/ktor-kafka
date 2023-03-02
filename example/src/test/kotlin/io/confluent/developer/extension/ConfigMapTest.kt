package io.confluent.developer.extension

import io.ktor.server.config.*
import io.ktor.server.testing.*
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.*
import org.junit.Test

class ConfigMapTest {

    @Test
    fun configMapTestBlah() {
        val config = ApplicationConfig("kafka-config-map.conf")
        val fixture = "org.apache.kafka.common.serialization.LongSerializer"
        val map = configMap(config, "ktor.kafka.producer")
        assertThat(map["key.serializer"]).isEqualTo(fixture)
    }
}
