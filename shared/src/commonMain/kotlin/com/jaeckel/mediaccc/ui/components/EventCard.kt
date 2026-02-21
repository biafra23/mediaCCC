package com.jaeckel.mediaccc.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.jaeckel.mediaccc.api.model.Event
import com.jaeckel.mediaccc.data.repository.FavoritesRepository
import kotlinx.coroutines.launch
import mediaccc.shared.generated.resources.Res
import mediaccc.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@OptIn(ExperimentalFoundationApi::class)
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
            modifier = modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { showMenu = true }
                ),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column {
                // Thumbnail
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                ) {
                    AsyncImage(
                        model = event.thumbUrl ?: event.posterUrl,
                        contentDescription = event.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Duration badge
                    event.duration?.let { duration ->
                        val minutes = duration / 60
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                                .background(
                                    Color.Black.copy(alpha = 0.7f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${minutes} min",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                    }

                    // Favorite star badge
                    if (isFavorite) {
                        Text(
                            text = "★",
                            color = Color(0xFFFFD700),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                        )
                    }
                }

                // Event info
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    event.conferenceTitle?.let { conference ->
                        Text(
                            text = conference,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    event.persons?.let { persons ->
                        if (persons.isNotEmpty()) {
                            Text(
                                text = persons.joinToString(", "),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
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
                        if (isFavorite) stringResource(Res.string.remove_from_favorites)
                        else stringResource(Res.string.add_to_favorites)
                    )
                },
                leadingIcon = {
                    Text(
                        text = if (isFavorite) "★" else "☆",
                        color = if (isFavorite) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurface
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EventCardCompact(
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
            modifier = modifier
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { showMenu = true }
                ),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            ) {
                AsyncImage(
                    model = event.thumbUrl ?: event.posterUrl,
                    contentDescription = event.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.8f)
                                ),
                                startY = 50f
                            )
                        )
                )

                // Favorite star badge
                if (isFavorite) {
                    Text(
                        text = "★",
                        color = Color(0xFFFFD700),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    )
                }

                // Event info
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                ) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
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
                        if (isFavorite) stringResource(Res.string.remove_from_favorites)
                        else stringResource(Res.string.add_to_favorites)
                    )
                },
                leadingIcon = {
                    Text(
                        text = if (isFavorite) "★" else "☆",
                        color = if (isFavorite) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurface
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

