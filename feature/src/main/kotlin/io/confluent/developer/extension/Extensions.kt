package io.confluent.developer.extension

import com.typesafe.config.Config
import io.ktor.server.config.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

inline fun <reified T> logger(): Logger {
    return LoggerFactory.getLogger(T::class.java)
}

fun configMap(config: Config, path: String): Map<String, Any> =
    config.getConfig(path).entrySet().associateBy({ it.key }, { it.value.unwrapped() })

fun configMap(config: ApplicationConfig, path: String): Map<String, Any?> =
    // if bootstrap.server is key use list
    config.config(path).keys().associateBy({ it }, { config.config(path).property(it).getString() })
