package com.jaeckel.mediaccc.api

import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertFailsWith

class MediaCCCApiErrorTest {

    private fun jsonHeaders() = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())

    @Test
    fun `getConferences throws on 500 status`() {
        val api = MediaCCCApi(engine = MockEngine {
            respond(
                content = """{"error": "Internal Server Error"}""",
                status = HttpStatusCode.InternalServerError,
                headers = jsonHeaders()
            )
        })
        assertFailsWith<Exception> {
            runBlocking { api.getConferences() }
        }
    }

    @Test
    fun `getConferences throws on 404 status`() {
        val api = MediaCCCApi(engine = MockEngine {
            respond(
                content = """{"error": "Not Found"}""",
                status = HttpStatusCode.NotFound,
                headers = jsonHeaders()
            )
        })
        assertFailsWith<Exception> {
            runBlocking { api.getConferences() }
        }
    }

    @Test
    fun `getEvent throws on malformed json`() {
        val api = MediaCCCApi(engine = MockEngine {
            respond(
                content = """not valid json""",
                headers = jsonHeaders()
            )
        })
        assertFailsWith<Exception> {
            runBlocking { api.getEvent("some-guid") }
        }
    }

    @Test
    fun `getConference throws on empty response body`() {
        val api = MediaCCCApi(engine = MockEngine {
            respond(
                content = "",
                headers = jsonHeaders()
            )
        })
        assertFailsWith<Exception> {
            runBlocking { api.getConference("39c3") }
        }
    }
}
