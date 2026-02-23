package com.jaeckel.mediaccc.data.db

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EntityTest {

    @Test
    fun favoriteEventEntityConstruction() {
        val entity = FavoriteEventEntity(
            eventGuid = "guid1",
            title = "Test Event",
            thumbUrl = "https://example.com/thumb.jpg",
            posterUrl = null,
            conferenceTitle = "39C3",
            persons = "Speaker A, Speaker B",
            duration = 3600L,
            starredAt = 1000L
        )
        assertEquals("guid1", entity.eventGuid)
        assertEquals("Test Event", entity.title)
        assertEquals("https://example.com/thumb.jpg", entity.thumbUrl)
        assertNull(entity.posterUrl)
        assertEquals("39C3", entity.conferenceTitle)
        assertEquals("Speaker A, Speaker B", entity.persons)
        assertEquals(3600L, entity.duration)
        assertEquals(1000L, entity.starredAt)
    }

    @Test
    fun favoriteEventEntityEquality() {
        val entity1 = FavoriteEventEntity("g", "t", null, null, null, null, null, 0L)
        val entity2 = FavoriteEventEntity("g", "t", null, null, null, null, null, 0L)
        assertEquals(entity1, entity2)
    }

    @Test
    fun favoriteEventEntityCopy() {
        val entity = FavoriteEventEntity("g", "t", null, null, null, null, null, 0L)
        val copy = entity.copy(title = "Updated", starredAt = 999L)
        assertEquals("g", copy.eventGuid)
        assertEquals("Updated", copy.title)
        assertEquals(999L, copy.starredAt)
    }

    @Test
    fun playbackHistoryEntityConstruction() {
        val entity = PlaybackHistoryEntity(
            eventGuid = "guid1",
            title = "Test",
            thumbUrl = null,
            conferenceTitle = "Conference",
            persons = "Speaker",
            duration = 1800L,
            lastPlayedAt = 2000L,
            sliderPos = 0.5f
        )
        assertEquals("guid1", entity.eventGuid)
        assertEquals("Test", entity.title)
        assertEquals(0.5f, entity.sliderPos)
        assertEquals(2000L, entity.lastPlayedAt)
        assertEquals(1800L, entity.duration)
    }

    @Test
    fun playbackHistoryEntityCopy() {
        val entity = PlaybackHistoryEntity("g", "t", null, null, null, null, 0L, 0.0f)
        val updated = entity.copy(sliderPos = 0.75f, lastPlayedAt = 5000L)
        assertEquals(0.75f, updated.sliderPos)
        assertEquals(5000L, updated.lastPlayedAt)
        assertEquals("g", updated.eventGuid)
    }

    @Test
    fun playbackHistorySliderBoundaries() {
        val atStart = PlaybackHistoryEntity("g", "t", null, null, null, null, 0L, 0.0f)
        val atEnd = PlaybackHistoryEntity("g", "t", null, null, null, null, 0L, 1.0f)
        assertEquals(0.0f, atStart.sliderPos)
        assertEquals(1.0f, atEnd.sliderPos)
    }

    @Test
    fun queueEventEntityConstruction() {
        val entity = QueueEventEntity(
            eventGuid = "guid1",
            title = "Queued Event",
            thumbUrl = "https://example.com/thumb.jpg",
            posterUrl = "https://example.com/poster.jpg",
            conferenceTitle = "Congress",
            persons = "Speaker",
            duration = 2400L,
            order = 5L
        )
        assertEquals("guid1", entity.eventGuid)
        assertEquals("Queued Event", entity.title)
        assertEquals("https://example.com/thumb.jpg", entity.thumbUrl)
        assertEquals("https://example.com/poster.jpg", entity.posterUrl)
        assertEquals(5L, entity.order)
    }

    @Test
    fun queueEventEntityOrdering() {
        val first = QueueEventEntity("g1", "First", null, null, null, null, null, 1L)
        val second = QueueEventEntity("g2", "Second", null, null, null, null, null, 2L)
        val third = QueueEventEntity("g3", "Third", null, null, null, null, null, 3L)
        assertTrue(first.order < second.order)
        assertTrue(second.order < third.order)
    }

    @Test
    fun queueEventEntityNegativeOrder() {
        val entity = QueueEventEntity("g", "t", null, null, null, null, null, -1L)
        assertEquals(-1L, entity.order)
    }

    @Test
    fun entityNullableFields() {
        val fav = FavoriteEventEntity("g", "t", null, null, null, null, null, 0L)
        val hist = PlaybackHistoryEntity("g", "t", null, null, null, null, 0L, 0f)
        val queue = QueueEventEntity("g", "t", null, null, null, null, null, 0L)
        assertNull(fav.thumbUrl)
        assertNull(fav.posterUrl)
        assertNull(fav.conferenceTitle)
        assertNull(fav.persons)
        assertNull(fav.duration)
        assertNull(hist.thumbUrl)
        assertNull(hist.conferenceTitle)
        assertNull(hist.persons)
        assertNull(hist.duration)
        assertNull(queue.thumbUrl)
        assertNull(queue.posterUrl)
        assertNull(queue.conferenceTitle)
        assertNull(queue.persons)
        assertNull(queue.duration)
    }
}
