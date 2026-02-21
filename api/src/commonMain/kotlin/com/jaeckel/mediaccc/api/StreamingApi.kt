package com.jaeckel.mediaccc.api

import com.jaeckel.mediaccc.api.model.streaming.StreamingConference
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
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class StreamingApi(
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

    private val baseUrl = "https://streaming.media.ccc.de"

    suspend fun getStreams(): List<StreamingConference> {
        return client.get("$baseUrl/streams/v2.json").body()
    }
}
