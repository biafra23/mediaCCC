package com.jaeckel.mediaccc.tv.ui

import android.util.Log
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
import com.jaeckel.mediaccc.api.model.Event
import com.jaeckel.mediaccc.api.model.Recording
import com.jaeckel.mediaccc.viewmodel.EventDetailViewModel
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import org.koin.core.parameter.parametersOf
import androidx.compose.runtime.collectAsState
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventGuid: String,
    onPlayClick: (videoUrl: String, title: String, speakers: String, date: String, conference: String) -> Unit,
    onBackClick: () -> Unit
) {
    // Use eventGuid as key to ensure we get a fresh ViewModel for each event
    val viewModel: EventDetailViewModel = koinViewModel(
        key = eventGuid,
        parameters = { parametersOf(eventGuid) }
    )
    val uiState by viewModel.uiState.collectAsState()

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A2E))
    ) {
        when {
            uiState.isLoading -> {
                Text(
                    text = "Loading...",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            uiState.errorMessage != null -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Error: ${uiState.errorMessage}",
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onBackClick) {
                        Text("Go Back")
                    }
                }
            }
            uiState.event != null -> {
                EventDetailContent(
                    event = uiState.event!!,
                    bestRecording = uiState.bestRecording,
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
    bestRecording: Recording?,
    dateTimeFormat: DateTimeFormat<LocalDateTime>,
    onPlayClick: (videoUrl: String, title: String, speakers: String, date: String, conference: String) -> Unit,
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

            ) {
                // Fixed header section (title, metadata, buttons)
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
                    if (bestRecording != null) {
                        Button(
                            onClick = {
                                onPlayClick(
                                    bestRecording.recordingUrl ?: "",
                                    event.title,
                                    event.persons?.joinToString(", ") ?: "",
                                    event.date?.toString() ?: "",
                                    event.conferenceTitle ?: ""
                                )
                            },
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

                // Scrollable description section
                event.description?.let { description ->
                    if (description.isNotBlank()) {
                        Text(
                            text = "Description",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
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





