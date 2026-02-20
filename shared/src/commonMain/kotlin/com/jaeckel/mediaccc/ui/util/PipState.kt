package com.jaeckel.mediaccc.ui.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Shared state for Picture-in-Picture mode.
 * Updated by EventDetailScreen when video playback starts/stops.
 * Read by the platform Activity to decide whether to enter PiP.
 */
object PipState {
    private val _isVideoPlaying = MutableStateFlow(false)
    val isVideoPlaying: StateFlow<Boolean> = _isVideoPlaying.asStateFlow()

    fun setPlaying(playing: Boolean) {
        _isVideoPlaying.value = playing
    }
}
