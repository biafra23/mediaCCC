package com.jaeckel.mediaccc.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.LinearProgressIndicator
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
import com.jaeckel.mediaccc.data.db.PlaybackHistoryEntity
import com.jaeckel.mediaccc.data.repository.FavoritesRepository
import com.jaeckel.mediaccc.data.repository.QueueRepository
import kotlinx.coroutines.launch
import mediaccc.shared.generated.resources.Res
import mediaccc.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryCardCompact(
    entry: PlaybackHistoryEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val favoritesRepository: FavoritesRepository = koinInject()
    val queueRepository: QueueRepository = koinInject()
    val isFavorite by favoritesRepository.isFavorite(entry.eventGuid).collectAsState(initial = false)
    val isInQueue by queueRepository.isInQueue(entry.eventGuid).collectAsState(initial = false)
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
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                ) {
                    AsyncImage(
                        model = entry.thumbUrl,
                        contentDescription = entry.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
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

                    Text(
                        text = entry.title,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                    )
                }

                // Progress bar at the bottom
                LinearProgressIndicator(
                    progress = { (entry.sliderPos / 1000f).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            if (isInQueue) {
                DropdownMenuItem(
                    text = { Text(stringResource(Res.string.remove_from_queue)) },
                    onClick = {
                        showMenu = false
                        scope.launch { queueRepository.removeFromQueue(entry.eventGuid) }
                    }
                )
            } else {
                DropdownMenuItem(
                    text = { Text(stringResource(Res.string.add_to_queue_start)) },
                    onClick = {
                        showMenu = false
                        scope.launch {
                            queueRepository.addToBeginning(
                                eventGuid = entry.eventGuid,
                                title = entry.title,
                                thumbUrl = entry.thumbUrl,
                                posterUrl = null,
                                conferenceTitle = entry.conferenceTitle,
                                persons = entry.persons,
                                duration = entry.duration
                            )
                        }
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(Res.string.add_to_queue_end)) },
                    onClick = {
                        showMenu = false
                        scope.launch {
                            queueRepository.addToEnd(
                                eventGuid = entry.eventGuid,
                                title = entry.title,
                                thumbUrl = entry.thumbUrl,
                                posterUrl = null,
                                conferenceTitle = entry.conferenceTitle,
                                persons = entry.persons,
                                duration = entry.duration
                            )
                        }
                    }
                )
            }

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
                            eventGuid = entry.eventGuid,
                            isFavorite = isFavorite,
                            title = entry.title,
                            thumbUrl = entry.thumbUrl,
                            posterUrl = null,
                            conferenceTitle = entry.conferenceTitle,
                            persons = entry.persons,
                            duration = entry.duration
                        )
                    }
                }
            )
        }
    }
}
