package com.jaeckel.mediaccc.tv.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import com.jaeckel.mediaccc.api.model.Event
import com.jaeckel.mediaccc.data.db.FavoriteEventEntity
import com.jaeckel.mediaccc.tv.R
import com.jaeckel.mediaccc.tv.ui.cards.EventCard
import com.jaeckel.mediaccc.viewmodel.FavoritesViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel = koinViewModel(),
    onEventClick: (String) -> Unit = {}
) {
    val favorites by viewModel.favorites.collectAsState()

    if (favorites.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = stringResource(R.string.no_favorites_yet))
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 220.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(favorites, key = { it.eventGuid }) { fav ->
                EventCard(
                    event = fav.toEvent(),
                    onClick = { onEventClick(fav.eventGuid) }
                )
            }
        }
    }
}

private fun FavoriteEventEntity.toEvent() = Event(
    guid = eventGuid,
    title = title,
    slug = "",
    url = "",
    thumbUrl = thumbUrl,
    posterUrl = posterUrl,
    conferenceTitle = conferenceTitle,
    persons = persons?.split(", ")?.filter { it.isNotBlank() },
    duration = duration
)
