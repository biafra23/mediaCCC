package com.jaeckel.mediaccc.api.model

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Event(
    val guid: String,
    val title: String,
    val subtitle: String? = null,
    val slug: String,
    val link: String? = null,
    val description: String? = null,
    @SerialName("original_language") val originalLanguage: String? = null,
    val persons: List<String>? = emptyList(),
    val tags: List<String>? = emptyList(),
    @SerialName("view_count") val viewCount: Int? = 0,
    val promoted: Boolean? = false,
    @Serializable(with = KtxInstantSerializer::class)
    val date: Instant? = null,
    @SerialName("release_date")
    @Serializable(with = KtxInstantSerializer::class)
    val releaseDate: Instant? = null,
    @SerialName("updated_at")
    @Serializable(with = KtxInstantSerializer::class)
    val updatedAt: Instant? = null,
    val length: Long? = 0,
    val duration: Long? = 0,
    @SerialName("thumb_url") val thumbUrl: String? = null,
    @SerialName("poster_url") val posterUrl: String? = null,
    @SerialName("timeline_url") val timelineUrl: String? = null,
    @SerialName("thumbnails_url") val thumbnailsUrl: String? = null,
    @SerialName("frontend_link") val frontendLink: String? = null,
    val url: String,
    @SerialName("conference_title") val conferenceTitle: String? = null,
    @SerialName("conference_url") val conferenceUrl: String? = null,
    val related: List<RelatedEvent> = emptyList(),
    val recordings: List<Recording> = emptyList()
)

@Serializable
data class RelatedEvent(
    @SerialName("event_id") val eventId: Int,
    @SerialName("event_guid") val eventGuid: String,
    val weight: Int? = null
)
