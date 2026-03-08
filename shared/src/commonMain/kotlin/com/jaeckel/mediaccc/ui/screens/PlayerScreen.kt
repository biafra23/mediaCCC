package com.jaeckel.mediaccc.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jaeckel.mediaccc.ui.util.SystemAppearance
import com.jaeckel.mediaccc.viewmodel.PlayerViewModel
import io.github.kdroidfilter.composemediaplayer.VideoPlayerSurface
import io.github.kdroidfilter.composemediaplayer.VideoPlayerState
import io.github.kdroidfilter.composemediaplayer.rememberVideoPlayerState
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun PlayerScreen(
    videoUrl: String,
    title: String,
    eventGuid: String? = null,
    onBackClick: () -> Unit
) {
    val viewModel: PlayerViewModel = koinViewModel(
        key = eventGuid?.takeIf { it.isNotBlank() } ?: videoUrl,
        parameters = { parametersOf(videoUrl, title, eventGuid) }
    )
    val uiState by viewModel.uiState.collectAsState()

    SystemAppearance(true)
    val playerState = rememberVideoPlayerState()
    var isBuffering by remember { mutableStateOf(true) }

    LaunchedEffect(uiState.videoUrl) {
        uiState.videoUrl?.let { url ->
            playerState.openUri(url)
        }
    }

    LaunchedEffect(playerState.isPlaying) {
        if (playerState.isPlaying) {
            isBuffering = false
        }
    }

    // Detect end of playback to play next
    LaunchedEffect(playerState.positionText, playerState.durationText) {
        val duration = parseTimeToSeconds(playerState.durationText)
        val position = parseTimeToSeconds(playerState.positionText)
        if (duration > 0 && position >= duration - 1) {
            viewModel.playNext()
        }
    }

    LaunchedEffect(Unit) {
        delay(10_000)
        isBuffering = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        VideoPlayerSurface(
            playerState = playerState,
            modifier = Modifier.fillMaxSize(),
            overlay = {
                LivePlayerOverlay(
                    playerState = playerState,
                    title = uiState.title,
                    onBackClick = onBackClick
                )
            }
        )
        if (isBuffering || uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = Color.White
            )
        }
    }
}

@Composable
private fun LivePlayerOverlay(
    playerState: VideoPlayerState,
    title: String,
    onBackClick: () -> Unit
) {
    var showControls by remember { mutableStateOf(true) }
    var interactionCount by remember { mutableStateOf(0) }

    LaunchedEffect(showControls, interactionCount) {
        if (showControls) {
            delay(5_000)
            showControls = false
        }
    }

    fun onInteract() { interactionCount++ }

    // Determine if at live edge (slider near max or duration is 0/unknown)
    val durationSecs = parseTimeToSeconds(playerState.durationText)
    val positionSecs = parseTimeToSeconds(playerState.positionText)
    val isAtLiveEdge = durationSecs == 0 || (durationSecs - positionSecs) < 5

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
            // Top bar: back button, title, live badge
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
                            onBackClick()
                        }
                        .padding(8.dp)
                )

                Text(
                    text = title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                )

                // LIVE indicator
                Box(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .background(
                            if (isAtLiveEdge) Color.Red else Color.Gray,
                            MaterialTheme.shapes.small
                        )
                        .clickable {
                            onInteract()
                            // Jump to live edge
                            playerState.seekTo(1000f)
                        }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isAtLiveEdge) "🔴 LIVE" else "● LIVE",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            // Center play/pause
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.9f))
                    .clickable {
                        onInteract()
                        if (playerState.isPlaying) playerState.pause() else playerState.play()
                    },
                contentAlignment = Alignment.Center
            ) {
                if (playerState.isPlaying) {
                    // Two white bars — avoids tint issues with Icons.Filled.Pause
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Box(
                            modifier = Modifier
                                .width(7.dp)
                                .height(26.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(MaterialTheme.colorScheme.onPrimary)
                        )
                        Box(
                            modifier = Modifier
                                .width(7.dp)
                                .height(26.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(MaterialTheme.colorScheme.onPrimary)
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Play",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            // Bottom: seek bar
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
                        activeTrackColor = if (isAtLiveEdge) Color.Red else Color.White,
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
                    if (!isAtLiveEdge && durationSecs > 0) {
                        Text(
                            text = "-${formatSeconds((durationSecs - positionSecs).coerceAtLeast(0))}",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

private fun parseTimeToSeconds(text: String): Int {
    val parts = text.split(":")
    return when (parts.size) {
        3 -> (parts[0].toIntOrNull() ?: 0) * 3600 + (parts[1].toIntOrNull() ?: 0) * 60 + (parts[2].toIntOrNull() ?: 0)
        2 -> (parts[0].toIntOrNull() ?: 0) * 60 + (parts[1].toIntOrNull() ?: 0)
        else -> 0
    }
}

private fun formatSeconds(totalSeconds: Int): String {
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    val pad = { v: Int -> v.toString().padStart(2, '0') }
    return if (h > 0) "$h:${pad(m)}:${pad(s)}" else "$m:${pad(s)}"
}

@Preview
@Composable
private fun PlayerScreenPreview() {
    MaterialTheme {
        PlayerScreen(
            videoUrl = "https://example.com/stream.m3u8",
            title = "Aktionshalle — Winterkongress 2026",
            onBackClick = {}
        )
    }
}
