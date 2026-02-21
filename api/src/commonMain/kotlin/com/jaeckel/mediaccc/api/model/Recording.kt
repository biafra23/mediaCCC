package com.jaeckel.mediaccc.api.model

import kotlin.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Recording(
    val size: Long? = 0,
    val length: Long? = 0,
    @SerialName("mime_type") val mimeType: String? = null,
    val language: String? = null,
    val filename: String? = null,
    val state: String? = null,
    val folder: String? = null,
    @SerialName("high_quality") val highQuality: Boolean? = false,
    val width: Int? = 0,
    val height: Int? = 0,
    @SerialName("updated_at")
    @Serializable(with = KtxInstantSerializer::class)
    val updatedAt: Instant? = null,
    @SerialName("recording_url") val recordingUrl: String? = null,
    val url: String,
    @SerialName("event_url") val eventUrl: String? = null,
    @SerialName("conference_url") val conferenceUrl: String? = null
)

