package com.jaeckel.mediaccc.api

import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MediaCCCApiTest {

    private fun jsonHeaders() = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())

    @Test
    fun `getConferences returns conferences`() = runBlocking {
        val api = MediaCCCApi(engine = MockEngine { request ->
            assertEquals("/public/conferences", request.url.encodedPath)
            respond(content = """{"conferences": [{"acronym": "39c3", "title": "39C3", "slug": "39c3", "url": "u"}]}""", headers = jsonHeaders())
        })
        val response = api.getConferences()
        assertEquals(1, response.conferences.size)
        assertEquals("39c3", response.conferences[0].acronym)
        assertEquals("39C3", response.conferences[0].title)
    }

    @Test
    fun `getConference returns single conference`() = runBlocking {
        val api = MediaCCCApi(engine = MockEngine { request ->
            assertTrue(request.url.encodedPath.endsWith("/39c3"))
            respond(content = """{"acronym": "39c3", "title": "39C3", "slug": "39c3", "url": "u", "events": []}""", headers = jsonHeaders())
        })
        val conference = api.getConference("39c3")
        assertEquals("39c3", conference.acronym)
        assertTrue(conference.events.isEmpty())
    }

    @Test
    fun `getEvents returns events`() = runBlocking {
        val api = MediaCCCApi(engine = MockEngine { request ->
            assertEquals("/public/events", request.url.encodedPath)
            respond(content = """{"events": [{"guid": "g1", "title": "Event 1", "slug": "e1", "url": "u"}]}""", headers = jsonHeaders())
        })
        val response = api.getEvents()
        assertEquals(1, response.events.size)
        assertEquals("g1", response.events[0].guid)
    }

    @Test
    fun `getEvent returns single event`() = runBlocking {
        val api = MediaCCCApi(engine = MockEngine { request ->
            assertTrue(request.url.encodedPath.contains("/events/"))
            respond(content = """{"guid": "test-guid", "title": "Test", "slug": "test", "url": "u", "recordings": [], "related": []}""", headers = jsonHeaders())
        })
        val event = api.getEvent("test-guid")
        assertEquals("test-guid", event.guid)
    }

    @Test
    fun `getRecentEvents calls correct endpoint`() = runBlocking {
        val api = MediaCCCApi(engine = MockEngine { request ->
            assertEquals("/public/events/recent", request.url.encodedPath)
            respond(content = """{"events": []}""", headers = jsonHeaders())
        })
        val response = api.getRecentEvents()
        assertTrue(response.events.isEmpty())
    }

    @Test
    fun `getPromotedEvents calls correct endpoint`() = runBlocking {
        val api = MediaCCCApi(engine = MockEngine { request ->
            assertEquals("/public/events/promoted", request.url.encodedPath)
            respond(content = """{"events": []}""", headers = jsonHeaders())
        })
        val response = api.getPromotedEvents()
        assertTrue(response.events.isEmpty())
    }

    @Test
    fun `search passes query parameter`() = runBlocking {
        val api = MediaCCCApi(engine = MockEngine { request ->
            assertEquals("/public/events/search", request.url.encodedPath)
            assertEquals("kotlin", request.url.parameters["q"])
            respond(content = """{"events": [{"guid": "g", "title": "Kotlin Talk", "slug": "s", "url": "u"}]}""", headers = jsonHeaders())
        })
        val response = api.search("kotlin")
        assertEquals(1, response.events.size)
        assertEquals("Kotlin Talk", response.events[0].title)
    }

    @Test
    fun `getPopularEvents passes year parameter`() = runBlocking {
        val api = MediaCCCApi(engine = MockEngine { request ->
            assertEquals("/public/events/popular", request.url.encodedPath)
            assertEquals("2024", request.url.parameters["year"])
            respond(content = """{"events": []}""", headers = jsonHeaders())
        })
        val response = api.getPopularEvents(2024)
        assertTrue(response.events.isEmpty())
    }

    @Test
    fun `getUnPopularEvents passes year parameter`() = runBlocking {
        val api = MediaCCCApi(engine = MockEngine { request ->
            assertEquals("/public/events/unpopular", request.url.encodedPath)
            assertEquals("2023", request.url.parameters["year"])
            respond(content = """{"events": []}""", headers = jsonHeaders())
        })
        val response = api.getUnPopularEvents(2023)
        assertTrue(response.events.isEmpty())
    }

    @Test
    fun `getRecordings returns recordings`() = runBlocking {
        val api = MediaCCCApi(engine = MockEngine { request ->
            assertEquals("/public/recordings", request.url.encodedPath)
            respond(content = """{"recordings": [{"url": "u", "recording_url": "https://example.com/rec.mp4"}]}""", headers = jsonHeaders())
        })
        val response = api.getRecordings()
        assertEquals(1, response.recordings.size)
        assertEquals("https://example.com/rec.mp4", response.recordings[0].recordingUrl)
    }

    @Test
    fun `getRecording returns single recording`() = runBlocking {
        val api = MediaCCCApi(engine = MockEngine { request ->
            assertTrue(request.url.encodedPath.contains("/recordings/"))
            respond(content = """{"url": "u", "recording_url": "https://example.com/rec.mp4", "mime_type": "video/mp4", "high_quality": true, "width": 1920, "height": 1080}""", headers = jsonHeaders())
        })
        val recording = api.getRecording("123")
        assertEquals("video/mp4", recording.mimeType)
        assertEquals(true, recording.highQuality)
        assertEquals(1920, recording.width)
        assertEquals(1080, recording.height)
    }

    @Test
    fun `getConference with events`() = runBlocking {
        val api = MediaCCCApi(engine = MockEngine {
            respond(
                content = """{
                    "acronym": "39c3",
                    "title": "39C3",
                    "slug": "39c3",
                    "url": "u",
                    "events": [
                        {"guid": "e1", "title": "Talk 1", "slug": "s1", "url": "u1"},
                        {"guid": "e2", "title": "Talk 2", "slug": "s2", "url": "u2"}
                    ]
                }""",
                headers = jsonHeaders()
            )
        })
        val conference = api.getConference("39c3")
        assertEquals(2, conference.events.size)
        assertEquals("Talk 1", conference.events[0].title)
        assertEquals("Talk 2", conference.events[1].title)
    }
}
