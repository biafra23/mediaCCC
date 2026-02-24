package com.jaeckel.mediaccc.data.repository

import com.jaeckel.mediaccc.data.db.PlaybackHistoryDao
import com.jaeckel.mediaccc.data.db.PlaybackHistoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PlaybackHistoryRepositoryTest {

    private class FakePlaybackHistoryDao : PlaybackHistoryDao {
        val items = mutableListOf<PlaybackHistoryEntity>()
        private val flow = MutableStateFlow<List<PlaybackHistoryEntity>>(emptyList())

        override suspend fun upsert(entry: PlaybackHistoryEntity) {
            items.removeAll { it.eventGuid == entry.eventGuid }
            items.add(entry)
            flow.value = items.toList()
        }

        override fun getAll(): Flow<List<PlaybackHistoryEntity>> = flow

        override fun getContinueWatching(): Flow<List<PlaybackHistoryEntity>> =
            flow.map { list -> list.filter { it.sliderPos > 5 && it.sliderPos < 975 } }

        override suspend fun getByGuid(guid: String): PlaybackHistoryEntity? =
            items.find { it.eventGuid == guid }

        override fun getByGuidFlow(guid: String): Flow<PlaybackHistoryEntity?> =
            flow.map { list -> list.find { it.eventGuid == guid } }
    }

    @Test
    fun getHistoryReturnsFlow() = runTest {
        val dao = FakePlaybackHistoryDao()
        val repo = PlaybackHistoryRepository(dao)
        assertTrue(repo.getHistory().first().isEmpty())
        dao.upsert(
            PlaybackHistoryEntity(
                eventGuid = "g1", title = "Talk", thumbUrl = null,
                conferenceTitle = null, persons = null, duration = null,
                lastPlayedAt = 1000L, sliderPos = 500f
            )
        )
        assertEquals(1, repo.getHistory().first().size)
    }

    @Test
    fun getContinueWatchingReturnsFlow() = runTest {
        val dao = FakePlaybackHistoryDao()
        val repo = PlaybackHistoryRepository(dao)
        // sliderPos of 500 should be in continue watching
        dao.upsert(
            PlaybackHistoryEntity(
                eventGuid = "g1", title = "Talk 1", thumbUrl = null,
                conferenceTitle = null, persons = null, duration = null,
                lastPlayedAt = 1000L, sliderPos = 500f
            )
        )
        // sliderPos of 2 should NOT be in continue watching (< 5)
        dao.upsert(
            PlaybackHistoryEntity(
                eventGuid = "g2", title = "Talk 2", thumbUrl = null,
                conferenceTitle = null, persons = null, duration = null,
                lastPlayedAt = 2000L, sliderPos = 2f
            )
        )
        val result = repo.getContinueWatching().first()
        assertEquals(1, result.size)
        assertEquals("g1", result[0].eventGuid)
    }

    @Test
    fun getEntryReturnsEntityOrNull() = runTest {
        val dao = FakePlaybackHistoryDao()
        val repo = PlaybackHistoryRepository(dao)
        assertNull(repo.getEntry("g1"))
        dao.upsert(
            PlaybackHistoryEntity(
                eventGuid = "g1", title = "Talk", thumbUrl = null,
                conferenceTitle = null, persons = null, duration = null,
                lastPlayedAt = 1000L, sliderPos = 100f
            )
        )
        val entry = repo.getEntry("g1")
        assertNotNull(entry)
        assertEquals("g1", entry.eventGuid)
    }

    @Test
    fun getEntryFlowReturnsFlow() = runTest {
        val dao = FakePlaybackHistoryDao()
        val repo = PlaybackHistoryRepository(dao)
        assertNull(repo.getEntryFlow("g1").first())
        dao.upsert(
            PlaybackHistoryEntity(
                eventGuid = "g1", title = "Talk", thumbUrl = null,
                conferenceTitle = null, persons = null, duration = null,
                lastPlayedAt = 1000L, sliderPos = 100f
            )
        )
        val entry = repo.getEntryFlow("g1").first()
        assertNotNull(entry)
        assertEquals("g1", entry.eventGuid)
    }

    @Test
    fun saveProgressCallsUpsertWithCorrectEntity() = runTest {
        val dao = FakePlaybackHistoryDao()
        val repo = PlaybackHistoryRepository(dao)
        repo.saveProgress(
            eventGuid = "g1", title = "My Talk", thumbUrl = "thumb",
            conferenceTitle = "Conf", persons = "Speaker A",
            duration = 3600L, sliderPos = 250f
        )
        assertEquals(1, dao.items.size)
        val saved = dao.items[0]
        assertEquals("g1", saved.eventGuid)
        assertEquals("My Talk", saved.title)
        assertEquals("thumb", saved.thumbUrl)
        assertEquals("Conf", saved.conferenceTitle)
        assertEquals("Speaker A", saved.persons)
        assertEquals(3600L, saved.duration)
        assertEquals(250f, saved.sliderPos)
        assertTrue(saved.lastPlayedAt > 0)
    }
}
