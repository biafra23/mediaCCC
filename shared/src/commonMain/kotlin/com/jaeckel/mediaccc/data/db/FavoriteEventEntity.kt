package com.jaeckel.mediaccc.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_events")
data class FavoriteEventEntity(
    @PrimaryKey val eventGuid: String,
    val title: String,
    val thumbUrl: String?,
    val posterUrl: String?,
    val conferenceTitle: String?,
    val persons: String?,
    val duration: Long?,
    val starredAt: Long
)
