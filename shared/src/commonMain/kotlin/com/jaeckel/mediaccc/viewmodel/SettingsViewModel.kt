package com.jaeckel.mediaccc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaeckel.mediaccc.data.repository.PlaybackHistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val historyRepository: PlaybackHistoryRepository
) : ViewModel() {

    private val _historyClearedEvent = MutableSharedFlow<Unit>()
    val historyClearedEvent = _historyClearedEvent.asSharedFlow()

    fun clearWatchHistory() {
        viewModelScope.launch {
            historyRepository.clearHistory()
            _historyClearedEvent.emit(Unit)
        }
    }
}
