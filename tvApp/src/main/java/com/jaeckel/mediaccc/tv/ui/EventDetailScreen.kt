package com.jaeckel.mediaccc.tv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import coil.compose.AsyncImage
import com.jaeckel.mediaccc.MediaRepository
import com.jaeckel.mediaccc.api.model.Event
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventGuid: String,
    onPlayClick: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val repository: MediaRepository = koinInject()
    var event by remember { mutableStateOf<Event?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val dateTimeFormat = remember {
        LocalDateTime.Format {
            day()
            char('.')
            monthNumber()
            char('.')
            year()
            char(' ')
            hour()
            char(':')
            minute()
        }
    }

    LaunchedEffect(eventGuid) {
        repository.getEvent(eventGuid).collect { result ->
            result.fold(
                onSuccess = {
                    event = it
                    isLoading = false
                },
                onFailure = {
                    errorMessage = it.message
                    isLoading = false
                }
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A2E))
    ) {
        when {
            isLoading -> {
                Text(
                    text = "Loading...",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            errorMessage != null -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Error: $errorMessage",
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onBackClick) {
                        Text("Go Back")
                    }
                }
            }
            event != null -> {
                EventDetailContent(
                    event = event!!,
                    dateTimeFormat = dateTimeFormat,
                    onPlayClick = onPlayClick,
                    onBackClick = onBackClick
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun EventDetailContent(
    event: Event,
    dateTimeFormat: DateTimeFormat<LocalDateTime>,
    onPlayClick: (String) -> Unit,
    onBackClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Background image
        AsyncImage(
            model = event.posterUrl ?: event.thumbUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            alpha = 0.3f
        )

        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF1A1A2E).copy(alpha = 0.95f),
                            Color(0xFF1A1A2E).copy(alpha = 0.7f),
                            Color.Transparent
                        )
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(48.dp)
        ) {
            // Left side - Event details
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.displaySmall,
                    color = Color.White,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                event.subtitle?.let { subtitle ->
                    if (subtitle.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                event.conferenceTitle?.let { conference ->
                    Text(
                        text = conference,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                event.date?.let { date ->
                    val localDateTime = date.toLocalDateTime(TimeZone.currentSystemDefault())
                    val formattedDate = dateTimeFormat.format(localDateTime)
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }

                event.persons?.let { persons ->
                    if (persons.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Speakers: ${persons.joinToString(", ")}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                event.duration?.let { duration ->
                    val minutes = duration / 60
                    Text(
                        text = "Duration: ${minutes} min",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Find the best recording to play
                    val videoRecording = event.recordings.firstOrNull {
                        it.mimeType?.contains("video") == true
                    }

                    if (videoRecording != null) {
                        Button(
                            onClick = { onPlayClick(videoRecording.recordingUrl ?: "") },
                            colors = ButtonDefaults.colors(
                                containerColor = Color(0xFF6366F1)
                            )
                        ) {
                            Text("Play")
                        }
                    }

                    Button(
                        onClick = onBackClick,
                        colors = ButtonDefaults.colors(
                            containerColor = Color(0xFF4A4A6E)
                        )
                    ) {
                        Text("Back")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Description
                event.description?.let { description ->
                    if (description.isNotBlank()) {
                        Text(
                            text = "Description",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(48.dp))

            // Right side - Poster
            AsyncImage(
                model = event.posterUrl ?: event.thumbUrl,
                contentDescription = event.title,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .weight(0.6f)
                    .padding(vertical = 24.dp)
            )
        }
    }
}





