package com.jaeckel.mediaccc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaeckel.mediaccc.data.db.PlaybackHistoryEntity
import com.jaeckel.mediaccc.data.repository.PlaybackHistoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class HistoryViewModel(
    private val historyRepository: PlaybackHistoryRepository
) : ViewModel() {

    val history: StateFlow<List<PlaybackHistoryEntity>> = historyRepository
        .getHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val continueWatching: StateFlow<List<PlaybackHistoryEntity>> = historyRepository
        .getContinueWatching()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
