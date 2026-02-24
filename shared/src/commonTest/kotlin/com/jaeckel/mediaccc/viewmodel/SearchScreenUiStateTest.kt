package com.jaeckel.mediaccc.viewmodel

import com.jaeckel.mediaccc.api.model.Event
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SearchScreenUiStateTest {

    @Test
    fun defaultValues() {
        val state = SearchScreenUiState()
        assertEquals("", state.query)
        assertTrue(state.searchResults.isEmpty())
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertNull(state.selectedTag)
        assertTrue(state.tagCounts.isEmpty())
    }

    @Test
    fun filteredResultsWithNoTagReturnsAll() {
        val events = listOf(
            Event(guid = "g1", title = "Talk 1", slug = "s1", url = "u1", tags = listOf("security")),
            Event(guid = "g2", title = "Talk 2", slug = "s2", url = "u2", tags = listOf("network"))
        )
        val state = SearchScreenUiState(searchResults = events, selectedTag = null)
        assertEquals(2, state.filteredResults.size)
    }

    @Test
    fun filteredResultsWithTagFiltersCorrectly() {
        val events = listOf(
            Event(guid = "g1", title = "Talk 1", slug = "s1", url = "u1", tags = listOf("security")),
            Event(guid = "g2", title = "Talk 2", slug = "s2", url = "u2", tags = listOf("network")),
            Event(guid = "g3", title = "Talk 3", slug = "s3", url = "u3", tags = listOf("security", "network"))
        )
        val state = SearchScreenUiState(searchResults = events, selectedTag = "network")
        assertEquals(2, state.filteredResults.size)
        assertTrue(state.filteredResults.all { it.tags?.contains("network") == true })
    }

    @Test
    fun filteredResultsWithNullTags() {
        val events = listOf(
            Event(guid = "g1", title = "Talk 1", slug = "s1", url = "u1", tags = listOf("security")),
            Event(guid = "g2", title = "Talk 2", slug = "s2", url = "u2", tags = null)
        )
        val state = SearchScreenUiState(searchResults = events, selectedTag = "security")
        assertEquals(1, state.filteredResults.size)
    }

    @Test
    fun copySemantics() {
        val state = SearchScreenUiState()
        val updated = state.copy(query = "kotlin", isLoading = true)
        assertEquals("kotlin", updated.query)
        assertTrue(updated.isLoading)
        assertTrue(updated.searchResults.isEmpty())
    }

    @Test
    fun equality() {
        val state1 = SearchScreenUiState()
        val state2 = SearchScreenUiState()
        assertEquals(state1, state2)
    }
}
