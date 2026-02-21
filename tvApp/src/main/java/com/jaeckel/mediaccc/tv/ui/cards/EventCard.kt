package com.jaeckel.mediaccc.tv.ui.cards

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import coil3.compose.AsyncImage
import com.jaeckel.mediaccc.api.model.Event
import com.jaeckel.mediaccc.data.repository.FavoritesRepository
import com.jaeckel.mediaccc.tv.R
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun EventCard(
    event: Event,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val favoritesRepository: FavoritesRepository = koinInject()
    val isFavorite by favoritesRepository.isFavorite(event.guid).collectAsState(initial = false)
    var showMenu by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box {
        Card(
            onClick = onClick,
            onLongClick = { showMenu = true },
            modifier = modifier
                .width(210.dp)
                .height(180.dp),
            colors = CardDefaults.colors(
                containerColor = Color(0xFF2A2A4E)
            )
        ) {
            Column {
                // Thumbnail
                Box {
                    AsyncImage(
                        model = event.thumbUrl ?: event.posterUrl,
                        contentDescription = event.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    )

                    if (isFavorite) {
                        Text(
                            text = "★",
                            color = Color(0xFFFFD700),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(6.dp)
                        )
                    }
                }

                // Event info
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    event.conferenceTitle?.let { conference ->
                        Text(
                            text = conference,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        if (isFavorite) stringResource(R.string.remove_from_favorites)
                        else stringResource(R.string.add_to_favorites)
                    )
                },
                leadingIcon = {
                    Text(
                        text = if (isFavorite) "★" else "☆",
                        color = if (isFavorite) Color(0xFFFFD700) else Color.Unspecified
                    )
                },
                onClick = {
                    showMenu = false
                    scope.launch {
                        favoritesRepository.toggleFavorite(
                            eventGuid = event.guid,
                            isFavorite = isFavorite,
                            title = event.title,
                            thumbUrl = event.thumbUrl,
                            posterUrl = event.posterUrl,
                            conferenceTitle = event.conferenceTitle,
                            persons = event.persons?.joinToString(", "),
                            duration = event.duration
                        )
                    }
                }
            )
        }
    }
}

