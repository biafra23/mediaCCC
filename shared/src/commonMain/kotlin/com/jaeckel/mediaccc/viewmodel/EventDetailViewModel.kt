package com.jaeckel.mediaccc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaeckel.mediaccc.MediaRepository
import com.jaeckel.mediaccc.api.model.Event
import com.jaeckel.mediaccc.api.model.Recording
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EventDetailUiState(
    val isLoading: Boolean = true,
    val event: Event? = null,
    val bestRecording: Recording? = null,
    val errorMessage: String? = null
)

class EventDetailViewModel(
    private val repository: MediaRepository,
    private val eventGuid: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventDetailUiState())
    val uiState: StateFlow<EventDetailUiState> = _uiState.asStateFlow()

    init {
        loadEvent()
    }

    private fun loadEvent() {
        viewModelScope.launch {
            repository.getEvent(eventGuid).collect { result ->
                result.fold(
                    onSuccess = { event ->
                        val bestRecording = findBestRecording(event.recordings, event.originalLanguage)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                event = event,
                                bestRecording = bestRecording,
                                errorMessage = null
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = error.message
                            )
                        }
                    }
                )
            }
        }
    }

    private fun findBestRecording(recordings: List<Recording>, originalLanguage: String?): Recording? {
        // Filter to video recordings only
        val videoRecordings = recordings
            .filter { it.mimeType?.startsWith("video/") == true }

        // Prefer recordings matching the event's original language
        val preferred = if (originalLanguage != null) {
            videoRecordings.filter { it.language == originalLanguage }
                .ifEmpty { videoRecordings }
        } else {
            videoRecordings
        }

        // Then sort by format preference: mp4 > mpeg > webm > other
        return preferred
            .sortedBy { recording ->
                when (recording.mimeType) {
                    "video/mp4" -> 0
                    "video/mpeg" -> 1
                    "video/webm" -> 2
                    else -> 3
                }
            }
            .firstOrNull()
    }
}

