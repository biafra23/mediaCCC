package com.jaeckel.mediaccc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaeckel.mediaccc.MediaRepository
import com.jaeckel.mediaccc.api.model.Conference
import com.jaeckel.mediaccc.api.model.Event
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ConferenceDetailUiState(
    val isLoading: Boolean = true,
    val conference: Conference? = null,
    val events: List<Event> = emptyList(),
    val errorMessage: String? = null,
    val selectedTag: String? = null,
    val tagCounts: List<Pair<String, Int>> = emptyList()
) {
    val filteredEvents: List<Event>
        get() = if (selectedTag == null) events
                else events.filter { it.tags?.contains(selectedTag) == true }
}

class ConferenceDetailViewModel(
    private val repository: MediaRepository,
    private val acronym: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConferenceDetailUiState())
    val uiState: StateFlow<ConferenceDetailUiState> = _uiState.asStateFlow()

    init {
        loadConference()
    }

    fun selectTag(tag: String?) {
        _uiState.update { it.copy(selectedTag = if (it.selectedTag == tag) null else tag) }
    }

    private fun computeTagCounts(events: List<Event>): List<Pair<String, Int>> {
        val counts = mutableMapOf<String, Int>()
        for (event in events) {
            event.tags?.forEach { tag ->
                if (tag.isNotBlank()) counts[tag] = (counts[tag] ?: 0) + 1
            }
        }
        return counts.entries.sortedByDescending { it.value }.map { it.key to it.value }
    }

    private fun loadConference() {
        viewModelScope.launch {
            repository.getConference(acronym).collect { result ->
                result.fold(
                    onSuccess = { conference ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                conference = conference,
                                events = conference.events,
                                errorMessage = null,
                                tagCounts = computeTagCounts(conference.events)
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
}

