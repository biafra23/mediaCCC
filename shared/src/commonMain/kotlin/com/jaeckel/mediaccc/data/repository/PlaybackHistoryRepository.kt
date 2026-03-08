package com.jaeckel.mediaccc.data.repository

import com.jaeckel.mediaccc.data.db.PlaybackHistoryDao
import com.jaeckel.mediaccc.data.db.PlaybackHistoryEntity
import kotlinx.coroutines.flow.Flow
import kotlin.time.Clock

class PlaybackHistoryRepository(private val dao: PlaybackHistoryDao) {

    fun getHistory(): Flow<List<PlaybackHistoryEntity>> = dao.getAll()

    fun getContinueWatching(): Flow<List<PlaybackHistoryEntity>> = dao.getContinueWatching()

    suspend fun getEntry(guid: String): PlaybackHistoryEntity? = dao.getByGuid(guid)

    fun getEntryFlow(guid: String): Flow<PlaybackHistoryEntity?> = dao.getByGuidFlow(guid)

    suspend fun clearHistory() = dao.deleteAll()

    suspend fun saveProgress(
        eventGuid: String,
        title: String,
        thumbUrl: String?,
        conferenceTitle: String?,
        persons: String?,
        duration: Long?,
        sliderPos: Float
    ) {
        dao.upsert(
            PlaybackHistoryEntity(
                eventGuid = eventGuid,
                title = title,
                thumbUrl = thumbUrl,
                conferenceTitle = conferenceTitle,
                persons = persons,
                duration = duration,
                lastPlayedAt = Clock.System.now().toEpochMilliseconds(),
                sliderPos = sliderPos
            )
        )
    }
}
