package com.jaeckel.mediaccc.data.repository

import com.jaeckel.mediaccc.data.db.QueueEventDao
import com.jaeckel.mediaccc.data.db.QueueEventEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class QueueRepository(private val dao: QueueEventDao) {

    private val _currentEventGuid = MutableStateFlow<String?>(null)
    val currentEventGuid: StateFlow<String?> = _currentEventGuid.asStateFlow()

    fun setCurrentEventGuid(guid: String) {
        _currentEventGuid.value = guid
    }

    fun getAll(): Flow<List<QueueEventEntity>> = dao.getAll()

    fun isInQueue(eventGuid: String): Flow<Boolean> = dao.isInQueue(eventGuid)

    suspend fun addToBeginning(
        eventGuid: String,
        title: String,
        thumbUrl: String?,
        posterUrl: String?,
        conferenceTitle: String?,
        persons: String?,
        duration: Long?
    ) {
        val minOrder = dao.getMinOrder() ?: 0L
        dao.insert(
            QueueEventEntity(
                eventGuid = eventGuid,
                title = title,
                thumbUrl = thumbUrl,
                posterUrl = posterUrl,
                conferenceTitle = conferenceTitle,
                persons = persons,
                duration = duration,
                order = minOrder - 1
            )
        )
    }

    suspend fun addToEnd(
        eventGuid: String,
        title: String,
        thumbUrl: String?,
        posterUrl: String?,
        conferenceTitle: String?,
        persons: String?,
        duration: Long?
    ) {
        val maxOrder = dao.getMaxOrder() ?: 0L
        dao.insert(
            QueueEventEntity(
                eventGuid = eventGuid,
                title = title,
                thumbUrl = thumbUrl,
                posterUrl = posterUrl,
                conferenceTitle = conferenceTitle,
                persons = persons,
                duration = duration,
                order = maxOrder + 1
            )
        )
    }

    suspend fun removeFromQueue(eventGuid: String) {
        dao.delete(eventGuid)
    }

    suspend fun getNext(currentEventGuid: String): QueueEventEntity? {
        val current = dao.getByGuid(currentEventGuid) ?: return null
        return dao.getNext(current.order)
    }

    suspend fun reorder(items: List<QueueEventEntity>) {
        items.forEachIndexed { index, item ->
            dao.updateOrder(item.eventGuid, index.toLong())
        }
    }
}
