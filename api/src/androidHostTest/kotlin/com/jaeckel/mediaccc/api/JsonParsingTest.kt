package com.jaeckel.mediaccc.api

import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertNotNull
import kotlin.test.fail

class JsonParsingTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Test
    fun testDeserialization() {
        val jsonString = readResource("public.conferences.json")
        val response = json.decodeFromString<com.jaeckel.mediaccc.api.model.ConferencesResponse>(jsonString)

        assertNotNull(response)
        assertNotNull(response.conferences)
        println("Found ${response.conferences.size} conferences")

        if (response.conferences.isNotEmpty()) {
            val first = response.conferences.first()
            println("First conference: ${first.title}")
            assertNotNull(first.acronym)
            assertNotNull(first.url)
        }
    }

    @Test
    fun testConferenceDetailDeserialization() {
        val jsonString = readResource("public.conferences.39c3.json")
        val conference = json.decodeFromString<com.jaeckel.mediaccc.api.model.Conference>(jsonString)

        assertNotNull(conference)
        println("Conference: ${conference.title}")
        assertNotNull(conference.events)
        println("Found ${conference.events?.size} events")

        if (!conference.events.isNullOrEmpty()) {
            val firstEvent = conference.events!!.first()
            println("First Event: ${firstEvent.title}")
            assertNotNull(firstEvent.guid)
        }
    }

    @Test
    fun testEventsDeserialization() {
        val jsonString = readResource("public.events.json")
        val response = json.decodeFromString<com.jaeckel.mediaccc.api.model.EventsResponse>(jsonString)

        assertNotNull(response)
        assertNotNull(response.events)
        println("Found ${response.events.size} events")

        if (response.events.isNotEmpty()) {
            val first = response.events.first()
            println("First event: ${first.title}")
            assertNotNull(first.guid)

            // Check for related events
            if (!first.related.isNullOrEmpty()) {
                val firstRelated = first.related!!.first()
                println("First related event guid: ${firstRelated.eventGuid}")
                assertNotNull(firstRelated.eventGuid)
            }
        }
    }

    @Test
    fun testEventDetailDeserialization() {
        val jsonString = readResource("public.events.821.json")
        val event = json.decodeFromString<com.jaeckel.mediaccc.api.model.Event>(jsonString)

        assertNotNull(event)
        println("Event: ${event.title}")
        assertNotNull(event.guid)

        // Check recordings
        assertNotNull(event.recordings)
        println("Found ${event.recordings?.size} recordings")
        if (!event.recordings.isNullOrEmpty()) {
            val recording = event.recordings!!.first()
            println("First recording URL: ${recording.recordingUrl}")
            assertNotNull(recording.recordingUrl)
            assertNotNull(recording.mimeType)
        }
    }

    @Test
    fun testRecordingsDeserialization() {
        val jsonString = readResource("public.recordings.json")
        val response = json.decodeFromString<com.jaeckel.mediaccc.api.model.RecordingsResponse>(jsonString)

        assertNotNull(response)
        assertNotNull(response.recordings)
        println("Found ${response.recordings.size} recordings")

        if (response.recordings.isNotEmpty()) {
            val first = response.recordings.first()
            println("First recording filename: ${first.filename}")
            assertNotNull(first.recordingUrl)
        }
    }

    private fun readResource(fileName: String): String {
        return javaClass.classLoader?.getResource(fileName)?.readText()
            ?: throw IllegalArgumentException("Resource not found: $fileName")
    }
}
