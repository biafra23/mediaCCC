package com.jaeckel.mediaccc.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "queue_events")
data class QueueEventEntity(
    @PrimaryKey val eventGuid: String,
    val title: String,
    val thumbUrl: String?,
    val posterUrl: String?,
    val conferenceTitle: String?,
    val persons: String?,
    val duration: Long?,
    val order: Long
)
