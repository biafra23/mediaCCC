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
    val errorMessage: String? = null
)

class ConferenceDetailViewModel(
    private val repository: MediaRepository,
    private val acronym: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConferenceDetailUiState())
    val uiState: StateFlow<ConferenceDetailUiState> = _uiState.asStateFlow()

    init {
        loadConference()
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
}

