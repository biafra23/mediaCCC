package com.jaeckel.mediaccc

import com.jaeckel.mediaccc.api.StreamingApi
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StreamingRepositoryTest {

    @Test
    fun getStreamsEmitsSuccess() = runTest {
        val engine = MockEngine {
            respond(
                content = """[{"conference": "Test", "slug": "test", "groups": []}]""",
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        val repo = StreamingRepository(StreamingApi(engine = engine))
        val result = repo.getStreams().toList().first()
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("Test", result.getOrNull()?.first()?.conference)
    }

    @Test
    fun getStreamsEmitsFailureOnError() = runTest {
        val engine = MockEngine {
            throw RuntimeException("Network error")
        }
        val repo = StreamingRepository(StreamingApi(engine = engine))
        val result = repo.getStreams().toList().first()
        assertTrue(result.isFailure)
    }

    @Test
    fun getStreamsEmitsEmptyList() = runTest {
        val engine = MockEngine {
            respond(
                content = """[]""",
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        val repo = StreamingRepository(StreamingApi(engine = engine))
        val result = repo.getStreams().toList().first()
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.isEmpty() == true)
    }

    @Test
    fun getStreamsEmitsFullStreamingHierarchy() = runTest {
        val engine = MockEngine {
            respond(
                content = """[{
                    "conference": "38C3",
                    "slug": "38c3",
                    "isCurrentlyStreaming": true,
                    "groups": [{
                        "group": "Lectures",
                        "rooms": [{
                            "slug": "saal1",
                            "display": "Saal 1",
                            "talks": {
                                "current": {"title": "Opening", "speaker": "Host"},
                                "next": null
                            },
                            "streams": [{
                                "slug": "hd",
                                "display": "HD",
                                "type": "video",
                                "urls": {"hls": {"url": "https://example.com/stream.m3u8"}}
                            }]
                        }]
                    }]
                }]""",
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        val repo = StreamingRepository(StreamingApi(engine = engine))
        val result = repo.getStreams().toList().first()
        assertTrue(result.isSuccess)
        val conferences = result.getOrNull()!!
        assertEquals("38C3", conferences[0].conference)
        assertTrue(conferences[0].isCurrentlyStreaming)
        assertEquals("Saal 1", conferences[0].groups[0].rooms[0].display)
        assertEquals("Opening", conferences[0].groups[0].rooms[0].talks?.current?.title)
    }
}
