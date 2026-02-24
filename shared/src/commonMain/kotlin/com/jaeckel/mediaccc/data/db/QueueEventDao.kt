package com.jaeckel.mediaccc.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface QueueEventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: QueueEventEntity)

    @Query("DELETE FROM queue_events WHERE eventGuid = :eventGuid")
    suspend fun delete(eventGuid: String)

    @Query("SELECT * FROM queue_events ORDER BY `order` ASC")
    fun getAll(): Flow<List<QueueEventEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM queue_events WHERE eventGuid = :eventGuid)")
    fun isInQueue(eventGuid: String): Flow<Boolean>

    @Query("SELECT * FROM queue_events WHERE eventGuid = :eventGuid")
    suspend fun getByGuid(eventGuid: String): QueueEventEntity?

    @Query("SELECT MIN(`order`) FROM queue_events")
    suspend fun getMinOrder(): Long?

    @Query("SELECT MAX(`order`) FROM queue_events")
    suspend fun getMaxOrder(): Long?

    @Query("SELECT * FROM queue_events WHERE `order` > :currentOrder ORDER BY `order` ASC LIMIT 1")
    suspend fun getNext(currentOrder: Long): QueueEventEntity?

    @Query("UPDATE queue_events SET `order` = :order WHERE eventGuid = :eventGuid")
    suspend fun updateOrder(eventGuid: String, order: Long)
}
