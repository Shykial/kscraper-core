package com.shykial.kScraperCore.configuration.rabbitmq

import org.apache.avro.Schema
import org.apache.avro.io.DecoderFactory
import org.apache.avro.io.EncoderFactory
import org.apache.avro.specific.SpecificDatumReader
import org.apache.avro.specific.SpecificDatumWriter
import org.apache.avro.specific.SpecificRecord
import java.io.ByteArrayOutputStream

interface AvroSerializer<T : SpecificRecord> {
    fun serialize(input: T): ByteArray
}

interface AvroDeserializer<T : SpecificRecord> {
    fun deserialize(input: ByteArray): T
}

inline fun <reified T : SpecificRecord> createSerializer() = object : AvroSerializer<T> {
    private val writer = SpecificDatumWriter(T::class.java)

    override fun serialize(input: T): ByteArray {
        val outputStream = ByteArrayOutputStream()
        EncoderFactory.get().jsonEncoder(input.schema, outputStream)
            .apply { writer.write(input, this) }
            .flush()
        return outputStream.toByteArray().also { outputStream.reset() }
    }
}

inline fun <reified T : SpecificRecord> createDeserializer(schema: Schema) = object : AvroDeserializer<T> {
    private val reader = SpecificDatumReader(T::class.java)

    override fun deserialize(input: ByteArray): T {
        val decoder = DecoderFactory.get().jsonDecoder(schema, input.inputStream())
        return reader.read(null, decoder)
    }
}
