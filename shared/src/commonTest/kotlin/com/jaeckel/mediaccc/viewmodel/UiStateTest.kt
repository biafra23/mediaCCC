package com.jaeckel.mediaccc.viewmodel

import com.jaeckel.mediaccc.api.model.Conference
import com.jaeckel.mediaccc.api.model.Event
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UiStateTest {

    @Test
    fun homeScreenUiStateDefaultValues() {
        val state = HomeScreenUiState()
        assertTrue(state.liveStreams.isEmpty())
        assertTrue(state.promotedEvents.isEmpty())
        assertTrue(state.recentEvents.isEmpty())
        assertTrue(state.conferences.isEmpty())
        assertTrue(state.isLoading)
        assertNull(state.errorMessage)
    }

    @Test
    fun homeScreenUiStateCopyUpdatesSpecificFields() {
        val state = HomeScreenUiState()
        val updated = state.copy(isLoading = false, errorMessage = "Error occurred")
        assertEquals(false, updated.isLoading)
        assertEquals("Error occurred", updated.errorMessage)
        assertTrue(updated.liveStreams.isEmpty())
        assertTrue(updated.promotedEvents.isEmpty())
    }

    @Test
    fun homeScreenUiStateWithEvents() {
        val events = listOf(
            Event(guid = "g1", title = "Event 1", slug = "s1", url = "u1"),
            Event(guid = "g2", title = "Event 2", slug = "s2", url = "u2")
        )
        val state = HomeScreenUiState(promotedEvents = events, isLoading = false)
        assertEquals(2, state.promotedEvents.size)
        assertEquals("Event 1", state.promotedEvents[0].title)
        assertEquals("Event 2", state.promotedEvents[1].title)
    }

    @Test
    fun homeScreenUiStateWithRecentEvents() {
        val events = listOf(
            Event(guid = "g1", title = "Recent 1", slug = "s1", url = "u1")
        )
        val state = HomeScreenUiState(recentEvents = events)
        assertEquals(1, state.recentEvents.size)
        assertEquals("Recent 1", state.recentEvents[0].title)
    }

    @Test
    fun homeScreenUiStateWithConferences() {
        val conferences = listOf(
            Conference(acronym = "39c3", title = "39C3", slug = "39c3", url = "u"),
            Conference(acronym = "38c3", title = "38C3", slug = "38c3", url = "u2")
        )
        val state = HomeScreenUiState(conferences = conferences, isLoading = false)
        assertEquals(2, state.conferences.size)
        assertEquals("39c3", state.conferences[0].acronym)
    }

    @Test
    fun homeScreenUiStateEquality() {
        val state1 = HomeScreenUiState()
        val state2 = HomeScreenUiState()
        assertEquals(state1, state2)
    }

    @Test
    fun liveStreamItemConstruction() {
        val item = LiveStreamItem(
            conferenceName = "38C3",
            roomName = "Saal 1",
            thumbUrl = "https://example.com/thumb.jpg",
            currentTalkTitle = "Opening",
            currentTalkSpeaker = "Host",
            nextTalkTitle = "Keynote",
            hlsUrl = "https://example.com/stream.m3u8"
        )
        assertEquals("38C3", item.conferenceName)
        assertEquals("Saal 1", item.roomName)
        assertEquals("https://example.com/thumb.jpg", item.thumbUrl)
        assertEquals("Opening", item.currentTalkTitle)
        assertEquals("Host", item.currentTalkSpeaker)
        assertEquals("Keynote", item.nextTalkTitle)
        assertEquals("https://example.com/stream.m3u8", item.hlsUrl)
    }

    @Test
    fun liveStreamItemWithNullValues() {
        val item = LiveStreamItem(
            conferenceName = "Test",
            roomName = "Room",
            thumbUrl = null,
            currentTalkTitle = null,
            currentTalkSpeaker = null,
            nextTalkTitle = null,
            hlsUrl = null
        )
        assertNull(item.thumbUrl)
        assertNull(item.currentTalkTitle)
        assertNull(item.currentTalkSpeaker)
        assertNull(item.nextTalkTitle)
        assertNull(item.hlsUrl)
    }

    @Test
    fun liveStreamItemEquality() {
        val item1 = LiveStreamItem("c", "r", null, null, null, null, null)
        val item2 = LiveStreamItem("c", "r", null, null, null, null, null)
        assertEquals(item1, item2)
    }

    @Test
    fun homeScreenUiStateWithLiveStreams() {
        val streams = listOf(
            LiveStreamItem("Conf", "Room 1", null, "Talk 1", "Speaker", null, "url1"),
            LiveStreamItem("Conf", "Room 2", null, "Talk 2", "Speaker 2", null, "url2")
        )
        val state = HomeScreenUiState(liveStreams = streams, isLoading = false)
        assertEquals(2, state.liveStreams.size)
        assertEquals("Room 1", state.liveStreams[0].roomName)
        assertEquals("Room 2", state.liveStreams[1].roomName)
    }

    @Test
    fun homeScreenUiStateClearError() {
        val stateWithError = HomeScreenUiState(errorMessage = "Error", isLoading = false)
        val cleared = stateWithError.copy(errorMessage = null, isLoading = true)
        assertNull(cleared.errorMessage)
        assertTrue(cleared.isLoading)
    }
}
