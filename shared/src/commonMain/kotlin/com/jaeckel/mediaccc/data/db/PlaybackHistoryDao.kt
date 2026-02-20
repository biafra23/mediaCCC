package com.jaeckel.mediaccc.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaybackHistoryDao {

    @Upsert
    suspend fun upsert(entry: PlaybackHistoryEntity)

    @Query("SELECT * FROM playback_history ORDER BY lastPlayedAt DESC")
    fun getAll(): Flow<List<PlaybackHistoryEntity>>

    @Query("SELECT * FROM playback_history WHERE eventGuid = :guid LIMIT 1")
    suspend fun getByGuid(guid: String): PlaybackHistoryEntity?
}
