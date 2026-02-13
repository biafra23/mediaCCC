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

    private fun readResource(fileName: String): String {
        return javaClass.classLoader?.getResource(fileName)?.readText()
            ?: throw IllegalArgumentException("Resource not found: $fileName")
    }
}
