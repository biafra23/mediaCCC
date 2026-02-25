package com.jaeckel.mediaccc.tv.ui.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import coil3.compose.AsyncImage
import com.jaeckel.mediaccc.api.model.Event
import com.jaeckel.mediaccc.data.repository.FavoritesRepository
import com.jaeckel.mediaccc.data.repository.QueueRepository
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * Event card with full-size thumbnail and gradient overlay.
 * Shows title and speakers on top of the image.
 * Used in grids like ConferenceDetailScreen.
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun EventCardWithOverlay(
    event: Event,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val favoritesRepository: FavoritesRepository = koinInject()
    val queueRepository: QueueRepository = koinInject()
    val isFavorite by favoritesRepository.isFavorite(event.guid).collectAsState(initial = false)
    val isInQueue by queueRepository.isInQueue(event.guid).collectAsState(initial = false)
    var showMenu by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box {
        Card(
            onClick = onClick,
            onLongClick = { showMenu = true },
            modifier = modifier
                .width(210.dp)
                .height(150.dp),
            colors = CardDefaults.colors(
                containerColor = Color(0xFF2A2A4E)
            )
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Thumbnail
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
                            .padding(6.dp)
                    )
                }

                // Event info
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                ) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    event.persons?.let { persons ->
                        if (persons.isNotEmpty()) {
                            Text(
                                text = persons.joinToString(", "),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        if (showMenu) {
            EventActionDialog(
                eventTitle = event.title,
                isFavorite = isFavorite,
                isInQueue = isInQueue,
                onDismiss = { showMenu = false },
                onToggleFavorite = {
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
                },
                onAddToQueueStart = {
                    showMenu = false
                    scope.launch {
                        queueRepository.addToBeginning(
                            eventGuid = event.guid,
                            title = event.title,
                            thumbUrl = event.thumbUrl,
                            posterUrl = event.posterUrl,
                            conferenceTitle = event.conferenceTitle,
                            persons = event.persons?.joinToString(", "),
                            duration = event.duration
                        )
                    }
                },
                onAddToQueueEnd = {
                    showMenu = false
                    scope.launch {
                        queueRepository.addToEnd(
                            eventGuid = event.guid,
                            title = event.title,
                            thumbUrl = event.thumbUrl,
                            posterUrl = event.posterUrl,
                            conferenceTitle = event.conferenceTitle,
                            persons = event.persons?.joinToString(", "),
                            duration = event.duration
                        )
                    }
                },
                onRemoveFromQueue = {
                    showMenu = false
                    scope.launch { queueRepository.removeFromQueue(event.guid) }
                }
            )
        }
    }
}
