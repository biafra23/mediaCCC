package com.jaeckel.mediaccc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaeckel.mediaccc.MediaRepository
import com.jaeckel.mediaccc.api.model.Event
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeScreenUiState(
    val promotedEvents: List<Event> = emptyList(),
    val recentEvents: List<Event> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class HomeViewModel(
    private val repository: MediaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeScreenUiState())
    val uiState: StateFlow<HomeScreenUiState> = _uiState.asStateFlow()

    init {
        loadContent()
    }

    fun loadContent() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

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
        }
    }
}

