package com.jaeckel.mediaccc.api

import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StreamingApiTest {

    @Test
    fun `getStreams returns streaming conferences`() = runBlocking {
        val engine = MockEngine { request ->
            assertEquals("/streams/v2.json", request.url.encodedPath)
            respond(
                content = """[{"conference": "38C3", "slug": "38c3", "groups": []}]""",
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        val api = StreamingApi(engine = engine)
        val streams = api.getStreams()
        assertEquals(1, streams.size)
        assertEquals("38C3", streams[0].conference)
        assertEquals("38c3", streams[0].slug)
    }

    @Test
    fun `getStreams returns empty list`() = runBlocking {
        val engine = MockEngine {
            respond(
                content = """[]""",
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        val api = StreamingApi(engine = engine)
        val streams = api.getStreams()
        assertTrue(streams.isEmpty())
    }

    @Test
    fun `getStreams parses nested groups and rooms`() = runBlocking {
        val engine = MockEngine {
            respond(
                content = """[{
                    "conference": "Test",
                    "slug": "test",
                    "groups": [{
                        "group": "Lectures",
                        "rooms": [{
                            "slug": "room1",
                            "display": "Room 1",
                            "streams": [{
                                "slug": "hd",
                                "display": "HD",
                                "type": "video",
                                "urls": {
                                    "hls": {"url": "https://example.com/stream.m3u8"}
                                }
                            }]
                        }]
                    }]
                }]""",
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        val api = StreamingApi(engine = engine)
        val streams = api.getStreams()
        assertEquals(1, streams.size)
        assertEquals(1, streams[0].groups.size)
        assertEquals("Lectures", streams[0].groups[0].group)
        assertEquals("Room 1", streams[0].groups[0].rooms[0].display)
        assertEquals("https://example.com/stream.m3u8",
            streams[0].groups[0].rooms[0].streams[0].urls["hls"]?.url)
    }

    @Test
    fun `getStreams parses talks`() = runBlocking {
        val engine = MockEngine {
            respond(
                content = """[{
                    "conference": "Test",
                    "slug": "test",
                    "isCurrentlyStreaming": true,
                    "groups": [{
                        "group": "Main",
                        "rooms": [{
                            "slug": "saal1",
                            "display": "Saal 1",
                            "talks": {
                                "current": {"title": "Opening", "speaker": "Host"},
                                "next": {"title": "Keynote", "speaker": "Expert"}
                            },
                            "streams": []
                        }]
                    }]
                }]""",
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        val api = StreamingApi(engine = engine)
        val streams = api.getStreams()
        assertTrue(streams[0].isCurrentlyStreaming)
        val room = streams[0].groups[0].rooms[0]
        assertEquals("Opening", room.talks?.current?.title)
        assertEquals("Host", room.talks?.current?.speaker)
        assertEquals("Keynote", room.talks?.next?.title)
    }

    @Test
    fun `getStreams parses multiple conferences`() = runBlocking {
        val engine = MockEngine {
            respond(
                content = """[
                    {"conference": "Conf A", "slug": "conf-a", "groups": []},
                    {"conference": "Conf B", "slug": "conf-b", "groups": []}
                ]""",
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        val api = StreamingApi(engine = engine)
        val streams = api.getStreams()
        assertEquals(2, streams.size)
        assertEquals("Conf A", streams[0].conference)
        assertEquals("Conf B", streams[1].conference)
    }
}
