package com.jaeckel.mediaccc.api.model

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

    @SerialName("original_language")
    val originalLanguage: String? = null,

    val persons: List<String>? = emptyList(),
    val tags: List<String>? = emptyList(),

    @SerialName("view_count")
    val viewCount: Int? = 0,

    val promoted: Boolean? = false,
    val date: String? = null,

    @SerialName("release_date")
    val releaseDate: String? = null,

    @SerialName("updated_at")
    val updatedAt: String? = null,

    val length: Long? = 0,
    val duration: Long? = 0,

    @SerialName("thumb_url")
    val thumbUrl: String? = null,

    @SerialName("poster_url")
    val posterUrl: String? = null,

    @SerialName("timeline_url")
    val timelineUrl: String? = null,

    @SerialName("thumbnails_url")
    val thumbnailsUrl: String? = null,

    @SerialName("frontend_link")
    val frontendLink: String? = null,

    val url: String,

    @SerialName("conference_title")
    val conferenceTitle: String? = null,

    @SerialName("conference_url")
    val conferenceUrl: String? = null
)

