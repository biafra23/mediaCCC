package com.jaeckel.mediaccc.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ConferencesResponse(
    val conferences: List<Conference>
)

@Serializable
data class Conference(
    val acronym: String,
    @SerialName("aspect_ratio") val aspectRatio: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    val title: String,
    @SerialName("schedule_url") val scheduleUrl: String? = null,
    val slug: String,
    @SerialName("event_last_released_at") val eventLastReleasedAt: String? = null,
    val link: String? = null,
    val description: String? = null,
    @SerialName("webgen_location") val webgenLocation: String? = null,
    @SerialName("logo_url") val logoUrl: String? = null,
    @SerialName("images_url") val imagesUrl: String? = null,
    @SerialName("recordings_url") val recordingsUrl: String? = null,
    val url: String,

    val events: List<Event>? = emptyList()
)

