package io.confluent.developer

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import org.json.JSONArray
import java.io.IOException

// JSON-Schema will generated from POKO in SR by Serializer
data class Question(
    val url: String,
    val title: String,
    val favoriteCount: Int,
    val viewCount: Int,
    val tags: List<String>,
    val body: String
) {
    constructor() : this("", "", 0, 0, emptyList(), "")

    override fun toString(): String {
        
        return """{"url": "$url", "title":"$title", "favoriteCount":$favoriteCount, "viewCount":$viewCount, "tags":${JSONArray(tags)}, "body":"$body"}"""
    }

}
