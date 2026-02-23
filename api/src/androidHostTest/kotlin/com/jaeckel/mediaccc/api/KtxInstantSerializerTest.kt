package com.jaeckel.mediaccc.api

import com.jaeckel.mediaccc.api.model.KtxInstantSerializer
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class KtxInstantSerializerTest {

    private val json = Json

    @Test
    fun `serialize Instant to JSON string`() {
        val instant = Instant.parse("2024-01-15T10:30:00Z")
        val serialized = json.encodeToString(KtxInstantSerializer, instant)
        assertEquals("\"2024-01-15T10:30:00Z\"", serialized)
    }

    @Test
    fun `deserialize JSON string to Instant`() {
        val jsonString = "\"2024-01-15T10:30:00Z\""
        val instant = json.decodeFromString(KtxInstantSerializer, jsonString)
        assertEquals(Instant.parse("2024-01-15T10:30:00Z"), instant)
    }

    @Test
    fun `round trip serialization preserves value`() {
        val original = Instant.parse("2023-12-27T18:00:00Z")
        val serialized = json.encodeToString(KtxInstantSerializer, original)
        val deserialized = json.decodeFromString(KtxInstantSerializer, serialized)
        assertEquals(original, deserialized)
    }

    @Test
    fun `deserialize ISO 8601 with fractional seconds`() {
        val jsonString = "\"2024-06-15T12:30:45.123Z\""
        val instant = json.decodeFromString(KtxInstantSerializer, jsonString)
        assertEquals(Instant.parse("2024-06-15T12:30:45.123Z"), instant)
    }

    @Test
    fun `round trip with epoch zero`() {
        val epoch = Instant.fromEpochSeconds(0)
        val serialized = json.encodeToString(KtxInstantSerializer, epoch)
        val deserialized = json.decodeFromString(KtxInstantSerializer, serialized)
        assertEquals(epoch, deserialized)
    }
}
