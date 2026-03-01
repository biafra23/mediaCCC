package com.jaeckel.mediaccc.data.repository

import com.jaeckel.mediaccc.data.db.QueueEventDao
import com.jaeckel.mediaccc.data.db.QueueEventEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class QueueRepositoryTest {

    private class FakeQueueEventDao : QueueEventDao {
        val items = mutableListOf<QueueEventEntity>()
        private val flow = MutableStateFlow<List<QueueEventEntity>>(emptyList())

        private fun emitUpdate() {
            flow.value = items.sortedBy { it.order }.toList()
        }

        override suspend fun insert(entity: QueueEventEntity) {
            items.removeAll { it.eventGuid == entity.eventGuid }
            items.add(entity)
            emitUpdate()
        }

        override suspend fun delete(eventGuid: String) {
            items.removeAll { it.eventGuid == eventGuid }
            emitUpdate()
        }

        override fun getAll(): Flow<List<QueueEventEntity>> = flow

        override fun isInQueue(eventGuid: String): Flow<Boolean> =
            flow.map { list -> list.any { it.eventGuid == eventGuid } }

        override suspend fun getByGuid(eventGuid: String): QueueEventEntity? =
            items.find { it.eventGuid == eventGuid }

        override suspend fun getMinOrder(): Long? =
            items.minByOrNull { it.order }?.order

        override suspend fun getMaxOrder(): Long? =
            items.maxByOrNull { it.order }?.order

        override suspend fun getNext(currentOrder: Long): QueueEventEntity? =
            items.filter { it.order > currentOrder }.minByOrNull { it.order }

        override suspend fun updateOrder(eventGuid: String, order: Long) {
            val index = items.indexOfFirst { it.eventGuid == eventGuid }
            if (index >= 0) {
                items[index] = items[index].copy(order = order)
                emitUpdate()
            }
        }
    }

    @Test
    fun getAllReturnsFlow() = runTest {
        val dao = FakeQueueEventDao()
        val repo = QueueRepository(dao)
        assertTrue(repo.getAll().first().isEmpty())
        dao.insert(
            QueueEventEntity(
                eventGuid = "g1", title = "Talk", thumbUrl = null,
                posterUrl = null, conferenceTitle = null, persons = null,
                duration = null, order = 1
            )
        )
        assertEquals(1, repo.getAll().first().size)
    }

    @Test
    fun isInQueueDelegatesToDao() = runTest {
        val dao = FakeQueueEventDao()
        val repo = QueueRepository(dao)
        assertFalse(repo.isInQueue("g1").first())
        dao.insert(
            QueueEventEntity(
                eventGuid = "g1", title = "Talk", thumbUrl = null,
                posterUrl = null, conferenceTitle = null, persons = null,
                duration = null, order = 1
            )
        )
        assertTrue(repo.isInQueue("g1").first())
    }

    @Test
    fun addToBeginningUsesMinOrderMinus1() = runTest {
        val dao = FakeQueueEventDao()
        val repo = QueueRepository(dao)
        // Insert first item with order 10
        dao.insert(
            QueueEventEntity(
                eventGuid = "existing", title = "Existing", thumbUrl = null,
                posterUrl = null, conferenceTitle = null, persons = null,
                duration = null, order = 10
            )
        )
        repo.addToBeginning(
            eventGuid = "new", title = "New", thumbUrl = null,
            posterUrl = null, conferenceTitle = null, persons = null,
            duration = null
        )
        val newItem = dao.items.find { it.eventGuid == "new" }
        assertNotNull(newItem)
        assertEquals(9, newItem.order)
    }

    @Test
    fun addToBeginningWithEmptyQueueUsesNegative1() = runTest {
        val dao = FakeQueueEventDao()
        val repo = QueueRepository(dao)
        repo.addToBeginning(
            eventGuid = "g1", title = "Talk", thumbUrl = null,
            posterUrl = null, conferenceTitle = null, persons = null,
            duration = null
        )
        val item = dao.items.find { it.eventGuid == "g1" }
        assertNotNull(item)
        assertEquals(-1, item.order) // minOrder null -> 0, minus 1 = -1
    }

    @Test
    fun addToEndUsesMaxOrderPlus1() = runTest {
        val dao = FakeQueueEventDao()
        val repo = QueueRepository(dao)
        dao.insert(
            QueueEventEntity(
                eventGuid = "existing", title = "Existing", thumbUrl = null,
                posterUrl = null, conferenceTitle = null, persons = null,
                duration = null, order = 5
            )
        )
        repo.addToEnd(
            eventGuid = "new", title = "New", thumbUrl = null,
            posterUrl = null, conferenceTitle = null, persons = null,
            duration = null
        )
        val newItem = dao.items.find { it.eventGuid == "new" }
        assertNotNull(newItem)
        assertEquals(6, newItem.order)
    }

    @Test
    fun addToEndWithEmptyQueueUses1() = runTest {
        val dao = FakeQueueEventDao()
        val repo = QueueRepository(dao)
        repo.addToEnd(
            eventGuid = "g1", title = "Talk", thumbUrl = null,
            posterUrl = null, conferenceTitle = null, persons = null,
            duration = null
        )
        val item = dao.items.find { it.eventGuid == "g1" }
        assertNotNull(item)
        assertEquals(1, item.order) // maxOrder null -> 0, plus 1 = 1
    }

    @Test
    fun removeFromQueueCallsDelete() = runTest {
        val dao = FakeQueueEventDao()
        val repo = QueueRepository(dao)
        dao.insert(
            QueueEventEntity(
                eventGuid = "g1", title = "Talk", thumbUrl = null,
                posterUrl = null, conferenceTitle = null, persons = null,
                duration = null, order = 1
            )
        )
        assertEquals(1, dao.items.size)
        repo.removeFromQueue("g1")
        assertEquals(0, dao.items.size)
    }

    @Test
    fun getNextReturnsNextItemInQueue() = runTest {
        val dao = FakeQueueEventDao()
        val repo = QueueRepository(dao)
        dao.insert(
            QueueEventEntity(
                eventGuid = "g1", title = "Talk 1", thumbUrl = null,
                posterUrl = null, conferenceTitle = null, persons = null,
                duration = null, order = 1
            )
        )
        dao.insert(
            QueueEventEntity(
                eventGuid = "g2", title = "Talk 2", thumbUrl = null,
                posterUrl = null, conferenceTitle = null, persons = null,
                duration = null, order = 2
            )
        )
        val next = repo.getNext("g1")
        assertNotNull(next)
        assertEquals("g2", next.eventGuid)
    }

    @Test
    fun getNextReturnsNullWhenNotInQueue() = runTest {
        val dao = FakeQueueEventDao()
        val repo = QueueRepository(dao)
        assertNull(repo.getNext("nonexistent"))
    }

    @Test
    fun getNextReturnsNullWhenNoNextItem() = runTest {
        val dao = FakeQueueEventDao()
        val repo = QueueRepository(dao)
        dao.insert(
            QueueEventEntity(
                eventGuid = "g1", title = "Talk 1", thumbUrl = null,
                posterUrl = null, conferenceTitle = null, persons = null,
                duration = null, order = 1
            )
        )
        assertNull(repo.getNext("g1"))
    }
}
