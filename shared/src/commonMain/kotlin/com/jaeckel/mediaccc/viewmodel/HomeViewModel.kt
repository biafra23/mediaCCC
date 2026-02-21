package com.jaeckel.mediaccc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaeckel.mediaccc.MediaRepository
import com.jaeckel.mediaccc.StreamingRepository
import com.jaeckel.mediaccc.api.model.Conference
import com.jaeckel.mediaccc.api.model.Event
import com.jaeckel.mediaccc.api.model.streaming.StreamRoom
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LiveStreamItem(
    val conferenceName: String,
    val roomName: String,
    val thumbUrl: String?,
    val currentTalkTitle: String?,
    val currentTalkSpeaker: String?,
    val nextTalkTitle: String?,
    val hlsUrl: String?
)

data class HomeScreenUiState(
    val liveStreams: List<LiveStreamItem> = emptyList(),
    val promotedEvents: List<Event> = emptyList(),
    val recentEvents: List<Event> = emptyList(),
    val conferences: List<Conference> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class HomeViewModel(
    private val repository: MediaRepository,
    private val streamingRepository: StreamingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeScreenUiState())
    val uiState: StateFlow<HomeScreenUiState> = _uiState.asStateFlow()

    init {
        loadContent()
    }

    fun loadContent() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            // Load live streams (non-fatal — don't set error if this fails)
            launch {
                streamingRepository.getStreams().collect { result ->
                    result.fold(
                        onSuccess = { conferences ->
                            val items = conferences.flatMap { conf ->
                                conf.groups.flatMap { group ->
                                    group.rooms.mapNotNull { room ->
                                        val hlsUrl = room.streams
                                            .filter { it.type == "video" || it.type == "hls" }
                                            .flatMap { it.urls.values }
                                            .firstOrNull { it.url.endsWith(".m3u8") }
                                            ?.url
                                            ?: return@mapNotNull null
                                        LiveStreamItem(
                                            conferenceName = conf.conference,
                                            roomName = room.display,
                                            thumbUrl = room.thumb,
                                            currentTalkTitle = room.talks?.current?.title,
                                            currentTalkSpeaker = room.talks?.current?.speaker,
                                            nextTalkTitle = room.talks?.next?.title,
                                            hlsUrl = hlsUrl
                                        )
                                    }
                                }
                            }
                            _uiState.value = _uiState.value.copy(liveStreams = items)
                        },
                        onFailure = { /* silently ignore streaming errors */ }
                    )
                }
            }

            // Load promoted events
            launch {
                repository.getPromotedEvents().collect { result ->
                    result.fold(
                        onSuccess = { response ->
                            _uiState.value = _uiState.value.copy(
                                promotedEvents = response.events,
                                isLoading = false
                            )
                        },
                        onFailure = { error ->
                            _uiState.value = _uiState.value.copy(
                                errorMessage = error.message,
                                isLoading = false
                            )
                        }
                    )
                }
            }

            // Load recent events
            launch {
                repository.getRecentEvents().collect { result ->
                    result.fold(
                        onSuccess = { response ->
                            _uiState.value = _uiState.value.copy(
                                recentEvents = response.events,
                                isLoading = false
                            )
                        },
                        onFailure = { error ->
                            _uiState.value = _uiState.value.copy(
                                errorMessage = error.message,
                                isLoading = false
                            )
                        }
                    )
                }
            }

            // Load conferences (sorted by most recent first)
            launch {
                repository.getConferences().collect { result ->
                    result.fold(
                        onSuccess = { response ->
                            val sortedConferences = response.conferences
                                .sortedByDescending { it.eventLastReleasedAt }
                            _uiState.value = _uiState.value.copy(
                                conferences = sortedConferences,
                                isLoading = false
                            )
                        },
                        onFailure = { error ->
                            _uiState.value = _uiState.value.copy(
                                errorMessage = error.message,
                                isLoading = false
                            )
                        }
                    )
                }
            }
        }
    }
}

