package com.jaeckel.mediaccc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaeckel.mediaccc.data.db.QueueEventEntity
import com.jaeckel.mediaccc.data.repository.QueueRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class QueueViewModel(
    private val repository: QueueRepository
) : ViewModel() {

    val queueItems: StateFlow<List<QueueEventEntity>> = repository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addToBeginning(
        eventGuid: String,
        title: String,
        thumbUrl: String?,
        posterUrl: String?,
        conferenceTitle: String?,
        persons: String?,
        duration: Long?
    ) {
        viewModelScope.launch {
            repository.addToBeginning(
                eventGuid = eventGuid,
                title = title,
                thumbUrl = thumbUrl,
                posterUrl = posterUrl,
                conferenceTitle = conferenceTitle,
                persons = persons,
                duration = duration
            )
        }
    }

    fun addToEnd(
        eventGuid: String,
        title: String,
        thumbUrl: String?,
        posterUrl: String?,
        conferenceTitle: String?,
        persons: String?,
        duration: Long?
    ) {
        viewModelScope.launch {
            repository.addToEnd(
                eventGuid = eventGuid,
                title = title,
                thumbUrl = thumbUrl,
                posterUrl = posterUrl,
                conferenceTitle = conferenceTitle,
                persons = persons,
                duration = duration
            )
        }
    }

    fun removeFromQueue(eventGuid: String) {
        viewModelScope.launch {
            repository.removeFromQueue(eventGuid)
        }
    }
}
