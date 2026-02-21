package com.jaeckel.mediaccc.data.repository

import com.jaeckel.mediaccc.data.db.FavoriteEventDao
import com.jaeckel.mediaccc.data.db.FavoriteEventEntity
import kotlinx.coroutines.flow.Flow
import kotlin.time.Clock

class FavoritesRepository(private val dao: FavoriteEventDao) {

    fun getAll(): Flow<List<FavoriteEventEntity>> = dao.getAll()

    fun isFavorite(eventGuid: String): Flow<Boolean> = dao.isFavorite(eventGuid)

    suspend fun toggleFavorite(
        eventGuid: String,
        isFavorite: Boolean,
        title: String,
        thumbUrl: String?,
        posterUrl: String?,
        conferenceTitle: String?,
        persons: String?,
        duration: Long?
    ) {
        if (isFavorite) {
            dao.delete(eventGuid)
        } else {
            dao.insert(
                FavoriteEventEntity(
                    eventGuid = eventGuid,
                    title = title,
                    thumbUrl = thumbUrl,
                    posterUrl = posterUrl,
                    conferenceTitle = conferenceTitle,
                    persons = persons,
                    duration = duration,
                    starredAt = Clock.System.now().toEpochMilliseconds()
                )
            )
        }
    }
}
