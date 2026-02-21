package com.jaeckel.mediaccc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaeckel.mediaccc.MediaRepository
import com.jaeckel.mediaccc.api.model.Event
import com.jaeckel.mediaccc.api.model.Recording
import com.jaeckel.mediaccc.data.repository.QueueRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlayerUiState(
    val isLoading: Boolean = false,
    val currentEvent: Event? = null,
    val nextEvent: Event? = null,
    val videoUrl: String? = null,
    val title: String = "",
    val errorMessage: String? = null
)

class PlayerViewModel(
    private val repository: MediaRepository,
    private val queueRepository: QueueRepository,
    private val initialVideoUrl: String,
    private val initialTitle: String,
    private val initialEventGuid: String?
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        PlayerUiState(
            videoUrl = initialVideoUrl,
            title = initialTitle
        )
    )
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    init {
        if (initialEventGuid != null) {
            loadEvent(initialEventGuid)
        }
    }

    private fun loadEvent(eventGuid: String) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            repository.getEvent(eventGuid).collect { result ->
                result.fold(
                    onSuccess = { event ->
                        val recording = findBestRecording(event.recordings, event.originalLanguage)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                currentEvent = event,
                                videoUrl = recording?.recordingUrl ?: it.videoUrl,
                                title = event.title,
                                errorMessage = null
                            )
                        }
                        loadNextEvent(eventGuid)
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

    private fun loadNextEvent(currentEventGuid: String) {
        viewModelScope.launch {
            val nextItem = queueRepository.getNext(currentEventGuid)
            if (nextItem != null) {
                repository.getEvent(nextItem.eventGuid).collect { result ->
                    result.onSuccess { nextEvent ->
                        _uiState.update { it.copy(nextEvent = nextEvent) }
                    }
                }
            }
        }
    }

    fun playNext() {
        val nextEvent = _uiState.value.nextEvent ?: return
        loadEvent(nextEvent.guid)
    }

    private fun findBestRecording(recordings: List<Recording>, originalLanguage: String?): Recording? {
        val videoRecordings = recordings
            .filter { it.mimeType?.startsWith("video/") == true }

        val preferred = if (originalLanguage != null) {
            videoRecordings.filter { it.language == originalLanguage }
                .ifEmpty { videoRecordings }
        } else {
            videoRecordings
        }

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
