package com.jaeckel.mediaccc.data.repository

import com.jaeckel.mediaccc.data.db.FavoriteEventDao
import com.jaeckel.mediaccc.data.db.FavoriteEventEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FavoritesRepositoryTest {

    private class FakeFavoriteEventDao : FavoriteEventDao {
        val items = mutableListOf<FavoriteEventEntity>()
        private val flow = MutableStateFlow<List<FavoriteEventEntity>>(emptyList())

        override suspend fun insert(entity: FavoriteEventEntity) {
            items.add(entity)
            flow.value = items.toList()
        }

        override suspend fun delete(eventGuid: String) {
            items.removeAll { it.eventGuid == eventGuid }
            flow.value = items.toList()
        }

        override fun getAll(): Flow<List<FavoriteEventEntity>> = flow

        override fun isFavorite(eventGuid: String): Flow<Boolean> =
            flow.map { list -> list.any { it.eventGuid == eventGuid } }
    }

    @Test
    fun getAllReturnsFlowFromDao() = runTest {
        val dao = FakeFavoriteEventDao()
        val repo = FavoritesRepository(dao)
        val entity = FavoriteEventEntity(
            eventGuid = "g1", title = "Talk", thumbUrl = null,
            posterUrl = null, conferenceTitle = null, persons = null,
            duration = null, starredAt = 1000L
        )
        dao.insert(entity)
        val result = repo.getAll().first()
        assertEquals(1, result.size)
        assertEquals("g1", result[0].eventGuid)
    }

    @Test
    fun isFavoriteDelegatesToDao() = runTest {
        val dao = FakeFavoriteEventDao()
        val repo = FavoritesRepository(dao)
        assertFalse(repo.isFavorite("g1").first())
        dao.insert(
            FavoriteEventEntity(
                eventGuid = "g1", title = "Talk", thumbUrl = null,
                posterUrl = null, conferenceTitle = null, persons = null,
                duration = null, starredAt = 1000L
            )
        )
        assertTrue(repo.isFavorite("g1").first())
    }

    @Test
    fun toggleFavoriteInsertsWhenNotFavorite() = runTest {
        val dao = FakeFavoriteEventDao()
        val repo = FavoritesRepository(dao)
        repo.toggleFavorite(
            eventGuid = "g1", isFavorite = false, title = "Talk",
            thumbUrl = "thumb", posterUrl = "poster",
            conferenceTitle = "Conf", persons = "Speaker",
            duration = 3600L
        )
        assertEquals(1, dao.items.size)
        assertEquals("g1", dao.items[0].eventGuid)
        assertEquals("Talk", dao.items[0].title)
        assertEquals("thumb", dao.items[0].thumbUrl)
        assertEquals("poster", dao.items[0].posterUrl)
        assertEquals("Conf", dao.items[0].conferenceTitle)
        assertEquals("Speaker", dao.items[0].persons)
        assertEquals(3600L, dao.items[0].duration)
    }

    @Test
    fun toggleFavoriteDeletesWhenAlreadyFavorite() = runTest {
        val dao = FakeFavoriteEventDao()
        val repo = FavoritesRepository(dao)
        dao.insert(
            FavoriteEventEntity(
                eventGuid = "g1", title = "Talk", thumbUrl = null,
                posterUrl = null, conferenceTitle = null, persons = null,
                duration = null, starredAt = 1000L
            )
        )
        assertEquals(1, dao.items.size)
        repo.toggleFavorite(
            eventGuid = "g1", isFavorite = true, title = "Talk",
            thumbUrl = null, posterUrl = null,
            conferenceTitle = null, persons = null,
            duration = null
        )
        assertEquals(0, dao.items.size)
    }
}
