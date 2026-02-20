package com.jaeckel.mediaccc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaeckel.mediaccc.MediaRepository
import com.jaeckel.mediaccc.api.model.Event
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SearchScreenUiState(
    val query: String = "",
    val searchResults: List<Event> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedTag: String? = null,
    val tagCounts: List<Pair<String, Int>> = emptyList()
) {
    val filteredResults: List<Event>
        get() = if (selectedTag == null) searchResults
                else searchResults.filter { it.tags?.contains(selectedTag) == true }
}

class SearchViewModel(
    private val repository: MediaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchScreenUiState())
    val uiState: StateFlow<SearchScreenUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun selectTag(tag: String?) {
        _uiState.value = _uiState.value.copy(
            selectedTag = if (_uiState.value.selectedTag == tag) null else tag
        )
    }

    fun onQueryChanged(newQuery: String) {
        _uiState.value = _uiState.value.copy(query = newQuery, selectedTag = null)
        searchJob?.cancel()
        if (newQuery.length >= 3) {
            searchJob = viewModelScope.launch {
                delay(500) // Debounce for 500ms
                performSearch(newQuery)
            }
        } else {
            _uiState.value = _uiState.value.copy(searchResults = emptyList(), errorMessage = null)
        }
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

    private suspend fun performSearch(query: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        repository.searchEvents(query).collect { result ->
            result.fold(
                onSuccess = { response ->
                    _uiState.value = _uiState.value.copy(
                        searchResults = response.events,
                        isLoading = false,
                        tagCounts = computeTagCounts(response.events)
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
