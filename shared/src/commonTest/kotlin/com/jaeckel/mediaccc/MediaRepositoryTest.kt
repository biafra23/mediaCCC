package com.jaeckel.mediaccc

import com.jaeckel.mediaccc.api.MediaCCCApi
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MediaRepositoryTest {

    private fun successEngine(responseBody: String): MockEngine {
        return MockEngine {
            respond(
                content = responseBody,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
    }

    private fun errorEngine(): MockEngine {
        return MockEngine {
            throw RuntimeException("Network error")
        }
    }

    @Test
    fun getConferencesEmitsSuccess() = runTest {
        val engine = successEngine("""{"conferences": [{"acronym": "test", "title": "Test", "slug": "s", "url": "u"}]}""")
        val repo = MediaRepository(MediaCCCApi(engine = engine))
        val result = repo.getConferences().toList().first()
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.conferences?.size)
        assertEquals("test", result.getOrNull()?.conferences?.first()?.acronym)
    }

    @Test
    fun getConferencesEmitsFailureOnError() = runTest {
        val repo = MediaRepository(MediaCCCApi(engine = errorEngine()))
        val result = repo.getConferences().toList().first()
        assertTrue(result.isFailure)
    }

    @Test
    fun getConferenceEmitsSuccess() = runTest {
        val engine = successEngine("""{"acronym": "39c3", "title": "39C3", "slug": "s", "url": "u", "events": []}""")
        val repo = MediaRepository(MediaCCCApi(engine = engine))
        val result = repo.getConference("39c3").toList().first()
        assertTrue(result.isSuccess)
        assertEquals("39c3", result.getOrNull()?.acronym)
    }

    @Test
    fun getConferenceEmitsFailureOnError() = runTest {
        val repo = MediaRepository(MediaCCCApi(engine = errorEngine()))
        val result = repo.getConference("39c3").toList().first()
        assertTrue(result.isFailure)
    }

    @Test
    fun getEventEmitsSuccess() = runTest {
        val engine = successEngine("""{"guid": "g1", "title": "Event", "slug": "s", "url": "u", "recordings": [], "related": []}""")
        val repo = MediaRepository(MediaCCCApi(engine = engine))
        val result = repo.getEvent("g1").toList().first()
        assertTrue(result.isSuccess)
        assertEquals("g1", result.getOrNull()?.guid)
    }

    @Test
    fun getEventEmitsFailureOnError() = runTest {
        val repo = MediaRepository(MediaCCCApi(engine = errorEngine()))
        val result = repo.getEvent("g1").toList().first()
        assertTrue(result.isFailure)
    }

    @Test
    fun searchEventsEmitsSuccess() = runTest {
        val engine = successEngine("""{"events": [{"guid": "g1", "title": "Kotlin Talk", "slug": "s", "url": "u"}]}""")
        val repo = MediaRepository(MediaCCCApi(engine = engine))
        val result = repo.searchEvents("kotlin").toList().first()
        assertTrue(result.isSuccess)
        assertEquals("Kotlin Talk", result.getOrNull()?.events?.first()?.title)
    }

    @Test
    fun searchEventsEmitsFailureOnError() = runTest {
        val repo = MediaRepository(MediaCCCApi(engine = errorEngine()))
        val result = repo.searchEvents("kotlin").toList().first()
        assertTrue(result.isFailure)
    }

    @Test
    fun getRecentEventsEmitsSuccess() = runTest {
        val engine = successEngine("""{"events": []}""")
        val repo = MediaRepository(MediaCCCApi(engine = engine))
        val result = repo.getRecentEvents().toList().first()
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.events?.isEmpty() == true)
    }

    @Test
    fun getPromotedEventsEmitsSuccess() = runTest {
        val engine = successEngine("""{"events": [{"guid": "g", "title": "Promoted", "slug": "s", "url": "u"}]}""")
        val repo = MediaRepository(MediaCCCApi(engine = engine))
        val result = repo.getPromotedEvents().toList().first()
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.events?.size)
    }

    @Test
    fun getPopularEventsEmitsSuccess() = runTest {
        val engine = successEngine("""{"events": []}""")
        val repo = MediaRepository(MediaCCCApi(engine = engine))
        val result = repo.getPopularEvents(2024).toList().first()
        assertTrue(result.isSuccess)
    }

    @Test
    fun getRecordingsEmitsSuccess() = runTest {
        val engine = successEngine("""{"recordings": [{"url": "u", "recording_url": "https://example.com/rec.mp4"}]}""")
        val repo = MediaRepository(MediaCCCApi(engine = engine))
        val result = repo.getRecordings().toList().first()
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.recordings?.size)
    }

    @Test
    fun getRecordingEmitsFailureOnError() = runTest {
        val repo = MediaRepository(MediaCCCApi(engine = errorEngine()))
        val result = repo.getRecording("123").toList().first()
        assertTrue(result.isFailure)
    }
}
