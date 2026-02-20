package com.jaeckel.mediaccc.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playback_history")
data class PlaybackHistoryEntity(
    @PrimaryKey val eventGuid: String,
    val title: String,
    val thumbUrl: String?,
    val conferenceTitle: String?,
    val persons: String?,
    val duration: Long?,
    val lastPlayedAt: Long,
    val sliderPos: Float
)
