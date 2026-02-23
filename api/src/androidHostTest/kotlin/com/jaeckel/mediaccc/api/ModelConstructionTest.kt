package com.jaeckel.mediaccc.api

import com.jaeckel.mediaccc.api.model.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ModelConstructionTest {

    @Test
    fun `Conference default values`() {
        val conference = Conference(
            acronym = "39c3",
            title = "39th Chaos Communication Congress",
            slug = "39c3",
            url = "https://api.media.ccc.de/public/conferences/39c3"
        )
        assertEquals("39c3", conference.acronym)
        assertEquals("39th Chaos Communication Congress", conference.title)
        assertNull(conference.aspectRatio)
        assertNull(conference.updatedAt)
        assertNull(conference.scheduleUrl)
        assertNull(conference.link)
        assertNull(conference.description)
        assertNull(conference.logoUrl)
        assertTrue(conference.events.isEmpty())
    }

    @Test
    fun `Conference equality`() {
        val conf1 = Conference(acronym = "a", title = "t", slug = "s", url = "u")
        val conf2 = Conference(acronym = "a", title = "t", slug = "s", url = "u")
        assertEquals(conf1, conf2)
    }

    @Test
    fun `Conference copy changes only specified fields`() {
        val conf = Conference(acronym = "a", title = "Original", slug = "s", url = "u")
        val copy = conf.copy(title = "Modified")
        assertEquals("a", copy.acronym)
        assertEquals("Modified", copy.title)
    }

    @Test
    fun `Event default values`() {
        val event = Event(
            guid = "test-guid",
            title = "Test Event",
            slug = "test-event",
            url = "https://api.media.ccc.de/public/events/test-guid"
        )
        assertEquals("test-guid", event.guid)
        assertEquals("Test Event", event.title)
        assertNull(event.subtitle)
        assertEquals(emptyList(), event.persons)
        assertEquals(emptyList(), event.tags)
        assertEquals(0, event.viewCount)
        assertEquals(false, event.promoted)
        assertEquals(0L, event.length)
        assertEquals(0L, event.duration)
        assertTrue(event.recordings.isEmpty())
        assertTrue(event.related.isEmpty())
    }

    @Test
    fun `Event equality`() {
        val event1 = Event(guid = "guid1", title = "Title", slug = "slug", url = "url")
        val event2 = Event(guid = "guid1", title = "Title", slug = "slug", url = "url")
        assertEquals(event1, event2)
    }

    @Test
    fun `Event copy changes only specified fields`() {
        val event = Event(guid = "guid1", title = "Original", slug = "slug", url = "url")
        val copy = event.copy(title = "Modified")
        assertEquals("guid1", copy.guid)
        assertEquals("Modified", copy.title)
    }

    @Test
    fun `Event with recordings and related events`() {
        val recording = Recording(url = "u", mimeType = "video/mp4", recordingUrl = "https://example.com/rec.mp4")
        val related = RelatedEvent(eventId = 1, eventGuid = "related-guid", weight = 10)
        val event = Event(
            guid = "g", title = "t", slug = "s", url = "u",
            recordings = listOf(recording),
            related = listOf(related)
        )
        assertEquals(1, event.recordings.size)
        assertEquals("video/mp4", event.recordings[0].mimeType)
        assertEquals(1, event.related.size)
        assertEquals("related-guid", event.related[0].eventGuid)
        assertEquals(10, event.related[0].weight)
    }

    @Test
    fun `Recording default values`() {
        val recording = Recording(url = "https://example.com/recording")
        assertEquals(0L, recording.size)
        assertEquals(0L, recording.length)
        assertNull(recording.mimeType)
        assertNull(recording.language)
        assertNull(recording.filename)
        assertEquals(false, recording.highQuality)
        assertEquals(0, recording.width)
        assertEquals(0, recording.height)
    }

    @Test
    fun `RelatedEvent construction`() {
        val related = RelatedEvent(eventId = 123, eventGuid = "abc-def")
        assertEquals(123, related.eventId)
        assertEquals("abc-def", related.eventGuid)
        assertNull(related.weight)
    }

    @Test
    fun `ConferencesResponse holds conferences`() {
        val conf = Conference(acronym = "a", title = "t", slug = "s", url = "u")
        val response = ConferencesResponse(conferences = listOf(conf))
        assertEquals(1, response.conferences.size)
        assertEquals("a", response.conferences[0].acronym)
    }

    @Test
    fun `EventsResponse holds events`() {
        val event = Event(guid = "g", title = "t", slug = "s", url = "u")
        val response = EventsResponse(events = listOf(event))
        assertEquals(1, response.events.size)
    }

    @Test
    fun `RecordingsResponse holds recordings`() {
        val rec = Recording(url = "u")
        val response = RecordingsResponse(recordings = listOf(rec))
        assertEquals(1, response.recordings.size)
    }

    @Test
    fun `empty response collections`() {
        val confs = ConferencesResponse(conferences = emptyList())
        val events = EventsResponse(events = emptyList())
        val recs = RecordingsResponse(recordings = emptyList())
        assertTrue(confs.conferences.isEmpty())
        assertTrue(events.events.isEmpty())
        assertTrue(recs.recordings.isEmpty())
    }
}
