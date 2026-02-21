package com.jaeckel.mediaccc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaeckel.mediaccc.data.db.FavoriteEventEntity
import com.jaeckel.mediaccc.data.repository.FavoritesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val repository: FavoritesRepository
) : ViewModel() {

    val favorites: StateFlow<List<FavoriteEventEntity>> = repository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleFavorite(
        eventGuid: String,
        isFavorite: Boolean,
        title: String,
        thumbUrl: String?,
        posterUrl: String?,
        conferenceTitle: String?,
        persons: String?,
        duration: Long?
    ) {
        viewModelScope.launch {
            repository.toggleFavorite(
                eventGuid = eventGuid,
                isFavorite = isFavorite,
                title = title,
                thumbUrl = thumbUrl,
                posterUrl = posterUrl,
                conferenceTitle = conferenceTitle,
                persons = persons,
                duration = duration
            )
        }
    }
}
