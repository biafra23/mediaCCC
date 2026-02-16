package com.jaeckel.mediaccc.api

import com.jaeckel.mediaccc.api.model.Conference
import com.jaeckel.mediaccc.api.model.ConferencesResponse
import com.jaeckel.mediaccc.api.model.Event
import com.jaeckel.mediaccc.api.model.EventsResponse
import com.jaeckel.mediaccc.api.model.Recording
import com.jaeckel.mediaccc.api.model.RecordingsResponse
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class MediaCCCApi(
    engine: HttpClientEngine? = null,
    logLevel: LogLevel = LogLevel.INFO,
    config: HttpClientConfig<*>.() -> Unit = {}
) {

    private val client = if (engine != null) {
        HttpClient(engine) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
            install(Logging) {
                logger = Logger.SIMPLE
                level = logLevel
            }
            config()
        }
    } else {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
            install(Logging) {
                logger = Logger.SIMPLE
                level = logLevel
            }
            config()
        }
    }

    private val baseUrl = "https://api.media.ccc.de/public"

    suspend fun getConferences(): ConferencesResponse {
        return client.get("$baseUrl/conferences").body()
    }

    suspend fun getConference(acronym: String): Conference {
        return client.get("$baseUrl/conferences/$acronym").body()
    }

    suspend fun getEvents(): EventsResponse {
        return client.get("$baseUrl/events").body()
    }

    suspend fun getEvent(guid: String): Event {
        return client.get("$baseUrl/events/$guid").body()
    }

    suspend fun getRecentEvents(): EventsResponse {
        return client.get("$baseUrl/events/recent").body()
    }

    suspend fun getPopularEvents(year: Int): EventsResponse {
        return client.get("$baseUrl/events/popular") {
            parameter("year", year)
        }.body()
    }

    suspend fun getUnPopularEvents(year: Int): EventsResponse {
        return client.get("$baseUrl/events/unpopular") {
            parameter("year", year)
        }.body()
    }

    suspend fun getPromotedEvents(): EventsResponse {
        return client.get("$baseUrl/events/promoted").body()
    }

    suspend fun search(query: String): EventsResponse {
        return client.get("$baseUrl/events/search") {
            parameter("q", query)
        }.body()
    }

    suspend fun getRecordings(): RecordingsResponse {
        return client.get("$baseUrl/recordings").body()
    }

    suspend fun getRecording(id: String): Recording {
        return client.get("$baseUrl/recordings/$id").body()
    }
}
