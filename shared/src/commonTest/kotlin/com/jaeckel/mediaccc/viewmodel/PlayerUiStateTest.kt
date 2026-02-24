package com.jaeckel.mediaccc.viewmodel

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class PlayerUiStateTest {

    @Test
    fun defaultValues() {
        val state = PlayerUiState()
        assertFalse(state.isLoading)
        assertNull(state.currentEvent)
        assertNull(state.nextEvent)
        assertNull(state.videoUrl)
        assertEquals("", state.title)
        assertNull(state.errorMessage)
    }

    @Test
    fun copySemantics() {
        val state = PlayerUiState()
        val updated = state.copy(
            isLoading = true,
            videoUrl = "https://example.com/video.mp4",
            title = "Test Talk"
        )
        assertEquals(true, updated.isLoading)
        assertEquals("https://example.com/video.mp4", updated.videoUrl)
        assertEquals("Test Talk", updated.title)
        assertNull(updated.currentEvent)
        assertNull(updated.nextEvent)
    }

    @Test
    fun withErrorMessage() {
        val state = PlayerUiState(errorMessage = "Network error", isLoading = false)
        assertEquals("Network error", state.errorMessage)
        assertFalse(state.isLoading)
    }

    @Test
    fun equality() {
        val state1 = PlayerUiState()
        val state2 = PlayerUiState()
        assertEquals(state1, state2)
    }
}
