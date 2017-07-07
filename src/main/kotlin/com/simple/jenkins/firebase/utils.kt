package com.simple.jenkins.firebase

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class NullIfBlank : ReadWriteProperty<Any?, String?> {
    private var value: String? = null

    override fun getValue(thisRef: Any?, property: KProperty<*>): String? = value

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: String?) {
        this.value = if (value.isNullOrBlank()) null else value
    }
}

class StringToArray : JsonSerializer<String>() {
    override fun serialize(value: String, gen: JsonGenerator, serializers: SerializerProvider) {
        with (gen) {
            writeStartArray()
            for (v in value.split(",")) {
                writeString(v.trim())
            }
            writeEndArray()
        }
    }
}

class StringToObject(val fieldDelimiter: String = ",", val keyDelimiter: String = "=") : JsonSerializer<String>() {
    override fun serialize(value: String, gen: JsonGenerator, serializers: SerializerProvider) {
        with (gen) {
            writeStartObject()
            for (entry in value.split(fieldDelimiter)) {
                val (k, v) = entry.split(keyDelimiter)
                writeObjectField(k.trim(), v.trim())
            }
            writeEndObject()
        }
    }
}
