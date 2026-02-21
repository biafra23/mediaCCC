package com.jaeckel.mediaccc.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jaeckel.mediaccc.api.model.Event
import com.jaeckel.mediaccc.data.db.FavoriteEventEntity
import com.jaeckel.mediaccc.ui.components.EventCard
import com.jaeckel.mediaccc.viewmodel.FavoritesViewModel
import mediaccc.shared.generated.resources.Res
import mediaccc.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel = koinViewModel(),
    onEventClick: (String) -> Unit = {},
    onBackClick: () -> Unit
) {
    val favorites by viewModel.favorites.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.favorites)) },
                navigationIcon = {
                    Text(
                        text = "←",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .clickable { onBackClick() }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        if (favorites.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(Res.string.no_favorites_yet),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 280.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
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

@Preview
@Composable
private fun FavoritesScreenPreview() {
    MaterialTheme {
        FavoritesScreen(onBackClick = {})
    }
}
