package com.jaeckel.mediaccc.tv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.jaeckel.mediaccc.data.db.PlaybackHistoryEntity
import com.jaeckel.mediaccc.viewmodel.HistoryViewModel
import kotlin.time.Clock
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = koinViewModel(),
    onEventClick: (String) -> Unit = {}
) {
    val history by viewModel.history.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A2E))
    ) {
        if (history.isEmpty()) {
            Text(
                text = "No watch history yet",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Watch History",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                modifier = Modifier.padding(start = 32.dp, top = 32.dp, bottom = 8.dp)
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(history, key = { it.eventGuid }) { entry ->
                    TvHistoryCard(
                        entry = entry,
                        onClick = { onEventClick(entry.eventGuid) }
                    )
                }
            }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun TvHistoryCard(
    entry: PlaybackHistoryEntity,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .scale(if (isFocused) 1.02f else 1f)
            .onFocusChanged { isFocused = it.isFocused },
        colors = CardDefaults.colors(
            containerColor = Color(0xFF2A2A3E),
            focusedContainerColor = Color(0xFF3A3A5E)
        ),
        shape = CardDefaults.shape(RoundedCornerShape(12.dp))
    ) {
        Column {
            // Thumbnail with gradient and progress
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            ) {
                AsyncImage(
                    model = entry.thumbUrl,
                    contentDescription = entry.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                                startY = 100f
                            )
                        )
                )
                Text(
                    text = formatRelativeDate(entry.lastPlayedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                )
            }

            // Progress bar
            val progress = (entry.sliderPos / 1000f).coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF6650A4),
                trackColor = Color.White.copy(alpha = 0.2f)
            )

            // Info
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                entry.conferenceTitle?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                entry.duration?.let { duration ->
                    val remainingSeconds = duration - (progress * duration).toLong()
                    if (remainingSeconds > 60) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${remainingSeconds / 60} min remaining",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF9A80D8)
                        )
                    }
                }
            }
        }
    }
}

private fun formatRelativeDate(epochMillis: Long): String {
    val now = Clock.System.now().toEpochMilliseconds()
    val diffHours = (now - epochMillis) / (1000 * 60 * 60)
    return when {
        diffHours < 1L -> "Just now"
        diffHours < 24L -> "${diffHours}h ago"
        diffHours < 48L -> "Yesterday"
        else -> "${diffHours / 24}d ago"
    }
}
