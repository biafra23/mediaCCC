package com.jaeckel.mediaccc.viewmodel

import com.jaeckel.mediaccc.api.model.Recording
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EventDetailUiStateTest {

    @Test
    fun defaultValues() {
        val state = EventDetailUiState()
        assertTrue(state.isLoading)
        assertNull(state.event)
        assertNull(state.bestRecording)
        assertTrue(state.availableLanguages.isEmpty())
        assertNull(state.selectedLanguage)
        assertNull(state.errorMessage)
        assertEquals(0f, state.savedSliderPos)
        assertFalse(state.isFavorite)
    }

    @Test
    fun copySemantics() {
        val state = EventDetailUiState()
        val updated = state.copy(isLoading = false, isFavorite = true, savedSliderPos = 500f)
        assertFalse(updated.isLoading)
        assertTrue(updated.isFavorite)
        assertEquals(500f, updated.savedSliderPos)
        assertNull(updated.event)
    }

    @Test
    fun allNullableFields() {
        val state = EventDetailUiState(
            event = null,
            bestRecording = null,
            selectedLanguage = null,
            errorMessage = null
        )
        assertNull(state.event)
        assertNull(state.bestRecording)
        assertNull(state.selectedLanguage)
        assertNull(state.errorMessage)
    }

    @Test
    fun withBestRecording() {
        val recording = Recording(url = "u", recordingUrl = "https://example.com/video.mp4", mimeType = "video/mp4")
        val state = EventDetailUiState(bestRecording = recording, isLoading = false)
        assertEquals("video/mp4", state.bestRecording?.mimeType)
        assertEquals("https://example.com/video.mp4", state.bestRecording?.recordingUrl)
    }

    @Test
    fun withAvailableLanguages() {
        val state = EventDetailUiState(
            availableLanguages = listOf("en", "de", "es"),
            selectedLanguage = "en"
        )
        assertEquals(3, state.availableLanguages.size)
        assertEquals("en", state.selectedLanguage)
    }

    @Test
    fun equality() {
        val state1 = EventDetailUiState()
        val state2 = EventDetailUiState()
        assertEquals(state1, state2)
    }
}
