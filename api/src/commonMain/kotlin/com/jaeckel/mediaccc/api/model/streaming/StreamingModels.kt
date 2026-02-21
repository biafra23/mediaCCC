package com.jaeckel.mediaccc.api.model.streaming

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Top-level object representing a streaming conference.
 * The API returns a JSON array of these at /streams/v2.json
 */
@Serializable
data class StreamingConference(
    val conference: String,
    val slug: String,
    val author: String? = null,
    val description: String? = null,
    val keywords: String? = null,
    val schedule: String? = null,
    val startsAt: String? = null,
    val endsAt: String? = null,
    val isCurrentlyStreaming: Boolean = false,
    val groups: List<StreamGroup> = emptyList()
)

/**
 * A group of rooms, e.g. "Lecture Rooms", "Live Music".
 */
@Serializable
data class StreamGroup(
    val group: String,
    val rooms: List<StreamRoom> = emptyList()
)

/**
 * A room within a group, containing metadata and available streams.
 */
@Serializable
data class StreamRoom(
    val guid: String? = null,
    val slug: String,
    val schedulename: String? = null,
    val thumb: String? = null,
    val poster: String? = null,
    val link: String? = null,
    val display: String,
    val stream: String? = null,
    val talks: RoomTalks? = null,
    val streams: List<RoomStream> = emptyList()
)

/**
 * Current and next talk info for a room.
 */
@Serializable
data class RoomTalks(
    val current: Talk? = null,
    val next: Talk? = null
)

/**
 * A single talk/session entry in the schedule.
 * May represent a real talk (with guid, code, speaker) or a special entry
 * like "daychange" (with special field set).
 */
@Serializable
data class Talk(
    val fstart: String? = null,
    val fend: String? = null,
    val tstart: String? = null,
    val tend: String? = null,
    val start: Long? = null,
    val end: Long? = null,
    val offset: Long? = null,
    val duration: Long? = null,
    val special: String? = null,
    val guid: String? = null,
    val code: String? = null,
    val track: String? = null,
    val title: String? = null,
    val speaker: String? = null,
    @SerialName("room_known")
    val roomKnown: Boolean? = null,
    val optout: Boolean? = null,
    val url: String? = null
)

/**
 * A single stream variant for a room.
 * Known type values: "video", "audio", "slides", "music", "hls"
 */
@Serializable
data class RoomStream(
    val slug: String,
    val display: String,
    val type: String,
    val isTranslated: Boolean = false,
    val videoSize: List<Int>? = null,
    val urls: Map<String, StreamUrl> = emptyMap()
)

/**
 * A concrete stream URL in a specific format.
 * Known format keys: "hls", "webm", "mp3", "opus"
 */
@Serializable
data class StreamUrl(
    val display: String? = null,
    val tech: String? = null,
    val url: String
)
