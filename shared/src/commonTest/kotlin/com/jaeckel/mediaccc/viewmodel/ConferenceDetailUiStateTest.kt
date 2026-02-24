package com.jaeckel.mediaccc.viewmodel

import com.jaeckel.mediaccc.api.model.Event
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ConferenceDetailUiStateTest {

    @Test
    fun defaultValues() {
        val state = ConferenceDetailUiState()
        assertTrue(state.isLoading)
        assertNull(state.conference)
        assertTrue(state.events.isEmpty())
        assertNull(state.errorMessage)
        assertNull(state.selectedTag)
        assertTrue(state.tagCounts.isEmpty())
    }

    @Test
    fun filteredEventsWithNoTagSelectedReturnsAll() {
        val events = listOf(
            Event(guid = "g1", title = "Talk 1", slug = "s1", url = "u1", tags = listOf("security")),
            Event(guid = "g2", title = "Talk 2", slug = "s2", url = "u2", tags = listOf("network"))
        )
        val state = ConferenceDetailUiState(events = events, selectedTag = null)
        assertEquals(2, state.filteredEvents.size)
    }

    @Test
    fun filteredEventsWithTagSelectedFiltersCorrectly() {
        val events = listOf(
            Event(guid = "g1", title = "Talk 1", slug = "s1", url = "u1", tags = listOf("security")),
            Event(guid = "g2", title = "Talk 2", slug = "s2", url = "u2", tags = listOf("network")),
            Event(guid = "g3", title = "Talk 3", slug = "s3", url = "u3", tags = listOf("security", "network"))
        )
        val state = ConferenceDetailUiState(events = events, selectedTag = "security")
        assertEquals(2, state.filteredEvents.size)
        assertTrue(state.filteredEvents.all { it.tags?.contains("security") == true })
    }

    @Test
    fun filteredEventsWithTagAndNullTagsExcludesNullTagEvents() {
        val events = listOf(
            Event(guid = "g1", title = "Talk 1", slug = "s1", url = "u1", tags = listOf("security")),
            Event(guid = "g2", title = "Talk 2", slug = "s2", url = "u2", tags = null)
        )
        val state = ConferenceDetailUiState(events = events, selectedTag = "security")
        assertEquals(1, state.filteredEvents.size)
        assertEquals("g1", state.filteredEvents[0].guid)
    }

    @Test
    fun copySemantics() {
        val state = ConferenceDetailUiState()
        val updated = state.copy(isLoading = false, errorMessage = "Error")
        assertEquals(false, updated.isLoading)
        assertEquals("Error", updated.errorMessage)
        assertNull(updated.conference)
    }

    @Test
    fun tagTogglingBehavior() {
        val state = ConferenceDetailUiState(selectedTag = null)
        val withTag = state.copy(selectedTag = "security")
        assertEquals("security", withTag.selectedTag)
        val cleared = withTag.copy(selectedTag = null)
        assertNull(cleared.selectedTag)
    }
}
