package com.jaeckel.mediaccc.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.jaeckel.mediaccc.ui.util.SystemAppearance
import com.jaeckel.mediaccc.viewmodel.EventDetailViewModel
import io.github.kdroidfilter.composemediaplayer.VideoPlayerSurface
import io.github.kdroidfilter.composemediaplayer.VideoPlayerState
import io.github.kdroidfilter.composemediaplayer.rememberVideoPlayerState
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventGuid: String,
    onPlayClick: (videoUrl: String, title: String, speakers: String, date: String, conference: String) -> Unit,
    onBackClick: () -> Unit
) {
    val viewModel: EventDetailViewModel = koinViewModel(
        key = eventGuid,
        parameters = { parametersOf(eventGuid) }
    )
    val uiState by viewModel.uiState.collectAsState()

    var isPlaying by rememberSaveable { mutableStateOf(false) }
    val playerState = rememberVideoPlayerState()
    
    // Apply system appearance based on fullscreen state
    SystemAppearance(playerState.isFullscreen)

    val recordingUrl = uiState.bestRecording?.recordingUrl
    LaunchedEffect(recordingUrl, isPlaying) {
        if (isPlaying && recordingUrl != null) {
            playerState.openUri(recordingUrl)
        }
    }

    val dateTimeFormat = remember {
        LocalDateTime.Format {
            day()
            char('.')
            monthNumber()
            char('.')
            year()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.event?.title ?: "Event",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
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
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onBackClick) {
                            Text("Go Back")
                        }
                    }
                }

                uiState.event != null -> {
                    val event = uiState.event!!

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Poster/Thumbnail or Video Player
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                        ) {
                            // Always compose the surface so fullscreen can work
                            VideoPlayerSurface(
                                playerState = playerState,
                                modifier = Modifier.fillMaxSize(),
                                overlay = {
                                    if (playerState.isFullscreen) {
                                        val formattedDate = event.date?.let {
                                            dateTimeFormat.format(it.toLocalDateTime(TimeZone.currentSystemDefault()))
                                        } ?: ""
                                        val speakers = event.persons?.joinToString(", ") ?: ""
                                        
                                        PlayerControlsOverlay(
                                            playerState = playerState,
                                            title = event.title,
                                            speakers = speakers,
                                            conference = event.conferenceTitle ?: "",
                                            date = formattedDate,
                                            onExitFullscreen = {
                                                playerState.toggleFullscreen()
                                                isPlaying = false
                                                playerState.pause()
                                            }
                                        )
                                    }
                                }
                            )

                            if (!isPlaying || recordingUrl == null) {
                                // Show thumbnail with play button on top
                                AsyncImage(
                                    model = event.posterUrl ?: event.thumbUrl,
                                    contentDescription = event.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                // Play button overlay
                                if (uiState.bestRecording != null) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .size(64.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.9f))
                                            .clickable {
                                                isPlaying = true
                                                playerState.toggleFullscreen()
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "▶",
                                            style = MaterialTheme.typography.headlineMedium,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // Title
                            Text(
                                text = event.title,
                                style = MaterialTheme.typography.headlineSmall
                            )

                            // Subtitle
                            event.subtitle?.let { subtitle ->
                                if (subtitle.isNotBlank()) {
                                    Text(
                                        text = subtitle,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Meta info
                            event.conferenceTitle?.let { conference ->
                                MetaInfoRow(label = "📅", text = conference)
                            }

                            event.date?.let { date ->
                                val localDateTime =
                                    date.toLocalDateTime(TimeZone.currentSystemDefault())
                                val formattedDate = dateTimeFormat.format(localDateTime)
                                MetaInfoRow(label = "🗓", text = formattedDate)
                            }

                            event.persons?.let { persons ->
                                if (persons.isNotEmpty()) {
                                    MetaInfoRow(label = "👤", text = persons.joinToString(", "))
                                }
                            }

                            event.duration?.let { duration ->
                                val minutes = duration / 60
                                MetaInfoRow(label = "⏱", text = "${minutes} min")
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Play button
                            if (uiState.bestRecording != null && !isPlaying) {
                                Button(
                                    onClick = {
                                        isPlaying = true
                                        playerState.toggleFullscreen()
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("▶ Play")
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Description
                            event.description?.let { description ->
                                if (description.isNotBlank()) {
                                    Text(
                                        text = "Description",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetaInfoRow(
    label: String,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PlayerControlsOverlay(
    playerState: VideoPlayerState,
    title: String,
    speakers: String,
    conference: String,
    date: String,
    onExitFullscreen: () -> Unit
) {
    var showControls by remember { mutableStateOf(true) }
    var interactionCount by remember { mutableStateOf(0) }

    LaunchedEffect(showControls, interactionCount) {
        if (showControls) {
            delay(10_000)
            showControls = false
        }
    }

    fun onInteract() {
        interactionCount++
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { 
                showControls = !showControls
                if (showControls) onInteract()
            }
    ) {
        if (showControls) {
            // Top bar with close button and metadata
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(8.dp)
                    .align(Alignment.TopStart),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "✕",
                    color = Color.White,
                    fontSize = 24.sp,
                    modifier = Modifier
                        .clickable { 
                            onInteract()
                            onExitFullscreen() 
                        }
                        .padding(8.dp)
                )

                Column(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .weight(1f)
                ) {
                    Text(
                        text = title,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    val metaText = buildString {
                        if (speakers.isNotBlank()) append(speakers)
                        val metaParts = listOfNotNull(
                            conference.takeIf { it.isNotBlank() },
                            date.takeIf { it.isNotBlank() }
                        )
                        if (metaParts.isNotEmpty()) {
                            if (isNotEmpty()) append(" • ")
                            append(metaParts.joinToString(" • "))
                        }
                    }
                    
                    if (metaText.isNotBlank()) {
                         Text(
                            text = metaText,
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Center play/pause
            val isPlaying = playerState.isPlaying
            val buttonColor = if (isPlaying) Color.Black.copy(alpha = 0.6f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
            val iconColor = if (isPlaying) Color.White else MaterialTheme.colorScheme.onPrimary
            
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(buttonColor)
                    .clickable {
                        onInteract()
                        if (isPlaying) playerState.pause() else playerState.play()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isPlaying) "⏸" else "▶",
                    style = MaterialTheme.typography.headlineMedium,
                    color = iconColor
                )
            }

            // Bottom bar with seek slider and time
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .align(Alignment.BottomCenter)
            ) {
                Slider(
                    value = playerState.sliderPos,
                    onValueChange = {
                        onInteract()
                        playerState.sliderPos = it
                        playerState.userDragging = true
                    },
                    onValueChangeFinished = {
                        onInteract()
                        playerState.userDragging = false
                        playerState.seekTo(playerState.sliderPos)
                    },
                    valueRange = 0f..1000f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.White,
                        inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = playerState.positionText,
                        color = Color.White,
                        fontSize = 12.sp
                    )
                    Text(
                        text = playerState.durationText,
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
