package com.jaeckel.mediaccc.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteEventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FavoriteEventEntity)

    @Query("DELETE FROM favorite_events WHERE eventGuid = :eventGuid")
    suspend fun delete(eventGuid: String)

    @Query("SELECT * FROM favorite_events ORDER BY starredAt DESC")
    fun getAll(): Flow<List<FavoriteEventEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_events WHERE eventGuid = :eventGuid)")
    fun isFavorite(eventGuid: String): Flow<Boolean>
}
