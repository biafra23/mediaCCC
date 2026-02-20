package com.jaeckel.mediaccc.tv

import android.util.Log
import android.view.KeyEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import chaintech.videoplayer.host.MediaPlayerEvent
import chaintech.videoplayer.host.MediaPlayerHost
import chaintech.videoplayer.model.VideoPlayerConfig
import chaintech.videoplayer.ui.video.VideoPlayerComposable
import kotlinx.coroutines.delay

import androidx.compose.runtime.collectAsState
import com.jaeckel.mediaccc.viewmodel.EventDetailViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun AndroidTVPlayer(
    eventGuid: String,
    videoUrl: String,
    title: String = "",
    speakers: String = "",
    date: String = "",
    conference: String = "",
    durationFromEvent: Long = 0
) {
    Log.i("AndroidTVPlayer", "videoUrl: $videoUrl")
    Log.i("AndroidTVPlayer", "title: $title")

    val viewModel: EventDetailViewModel = koinViewModel(
        key = eventGuid,
        parameters = { parametersOf(eventGuid) }
    )
    val uiState by viewModel.uiState.collectAsState()

    val playerHost = remember { MediaPlayerHost(videoUrl) }

    var currentTime by remember { mutableFloatStateOf(0f) }
    var duration by remember { mutableFloatStateOf(durationFromEvent.toFloat()) }
    var isPaused by remember { mutableStateOf(false) }
    var controlsVisible by remember { mutableStateOf(false) }
    var selectedControlIndex by remember { mutableIntStateOf(1) } // 0=rewind, 1=play/pause, 2=forward
    var hasRestoredPosition by remember { mutableStateOf(false) }

    // Periodically save progress
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            if (duration > 0) {
                val progress = (currentTime / duration) * 1000f
                viewModel.saveProgress(progress)
            }
        }
    }

    // Seek to saved position once duration is known
    LaunchedEffect(duration) {
        if (duration > 0 && !hasRestoredPosition && uiState.savedSliderPos > 5f) {
            val seekTarget = (uiState.savedSliderPos / 1000f) * duration
            Log.i("AndroidTVPlayer", "Restoring position to $seekTarget (sliderPos: ${uiState.savedSliderPos})")
            playerHost.seekTo(seekTarget)
            hasRestoredPosition = true
        }
    }

    // Auto-hide controls after 5 seconds
    LaunchedEffect(controlsVisible) {
        if (controlsVisible) {
            delay(5000)
            controlsVisible = false
        }
    }

    fun handleKeyEvent(keyEvent: androidx.compose.ui.input.key.KeyEvent): Boolean {
        if (keyEvent.type == KeyEventType.KeyDown) {
            when (keyEvent.nativeKeyEvent.keyCode) {
                KeyEvent.KEYCODE_DPAD_UP,
                KeyEvent.KEYCODE_DPAD_DOWN -> {
                    // Toggle controls visibility
                    controlsVisible = !controlsVisible
                    return true
                }

                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    if (controlsVisible) {
                        // Navigate control selection left
                        selectedControlIndex = (selectedControlIndex - 1).coerceAtLeast(0)
                    } else {
                        // Seek backward 10 seconds
                        playerHost.seekTo((currentTime - 10f).coerceAtLeast(0f))
                    }
                    return true
                }

                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    if (controlsVisible) {
                        // Navigate control selection right
                        selectedControlIndex = (selectedControlIndex + 1).coerceAtMost(2)
                    } else {
                        // Seek forward 10 seconds
                        playerHost.seekTo(currentTime + 10f)
                    }
                    return true
                }

                KeyEvent.KEYCODE_DPAD_CENTER,
                KeyEvent.KEYCODE_ENTER -> {
                    if (controlsVisible) {
                        // Execute selected control action
                        when (selectedControlIndex) {
                            0 -> playerHost.seekTo((currentTime - 10f).coerceAtLeast(0f))
                            1 -> playerHost.togglePlayPause()
                            2 -> playerHost.seekTo(currentTime + 10f)
                        }
                    } else {
                        // Show controls first, then toggle play/pause
                        controlsVisible = true
                        playerHost.togglePlayPause()
                    }
                    return true
                }

                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                    playerHost.togglePlayPause()
                    controlsVisible = true
                    return true
                }

                KeyEvent.KEYCODE_MEDIA_PLAY -> {
                    playerHost.play()
                    controlsVisible = true
                    return true
                }

                KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                    playerHost.pause()
                    controlsVisible = true
                    return true
                }

                KeyEvent.KEYCODE_BACK -> {
                    if (controlsVisible) {
                        controlsVisible = false
                        return true
                    }
                    return false
                }
            }
        }
        return false
    }

    playerHost.onEvent = { event ->
        when (event) {
            is MediaPlayerEvent.CurrentTimeChange -> {
                currentTime = event.currentTime
                // Update duration estimate - currentTime keeps increasing until video ends
                if (event.currentTime > duration) {
                    duration = event.currentTime
                }
            }
            is MediaPlayerEvent.PauseChange -> {
                isPaused = event.isPaused
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusable()
            .onKeyEvent(::handleKeyEvent)
    ) {
        // Video Player
        VideoPlayerComposable(
            modifier = Modifier.fillMaxSize(),
            playerHost = playerHost,
            playerConfig = VideoPlayerConfig(
                showControls = false,
                isPauseResumeEnabled = false,
                isSeekBarVisible = false,
                isDurationVisible = false,
                isAutoHideControlEnabled = false,
                isFastForwardBackwardEnabled = false,
            )
        )

        // Apple TV+ style overlay
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it }
        ) {
            PlayerControlsOverlay(
                title = title,
                speakers = speakers,
                date = date,
                conference = conference,
                currentTime = currentTime,
                duration = duration,
                isPaused = isPaused,
                selectedIndex = selectedControlIndex
            )
        }
    }
}

@Composable
private fun PlayerControlsOverlay(
    title: String,
    speakers: String,
    date: String,
    conference: String,
    currentTime: Float,
    duration: Float,
    isPaused: Boolean,
    selectedIndex: Int
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.7f),
                        Color.Transparent,
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.7f)
                    ),
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY
                )
            )
    ) {
        // Top section - Title, speakers, conference, date
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .padding(horizontal = 48.dp, vertical = 32.dp)
        ) {
            if (title.isNotBlank()) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (speakers.isNotBlank()) {
                Text(
                    text = speakers,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (conference.isNotBlank()) {
                    Text(
                        text = conference,
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 14.sp
                    )
                }

                if (conference.isNotBlank() && date.isNotBlank()) {
                    Text(
                        text = " • ",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 14.sp
                    )
                }

                if (date.isNotBlank()) {
                    Text(
                        text = date,
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Bottom section - Controls and progress
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 48.dp, vertical = 32.dp)
        ) {
            // Progress bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatTime(currentTime),
                    color = Color.White,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.width(16.dp))

                LinearProgressIndicator(
                    progress = { if (duration > 0) currentTime / duration else 0f },
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.3f)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = formatTime(duration),
                    color = Color.White,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Control buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ControlButton(
                    icon = Icons.Default.FastRewind,
                    label = "-10s",
                    isSelected = selectedIndex == 0
                )

                Spacer(modifier = Modifier.width(48.dp))

                ControlButton(
                    icon = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    label = if (isPaused) "Play" else "Pause",
                    isSelected = selectedIndex == 1,
                    isLarge = true
                )

                Spacer(modifier = Modifier.width(48.dp))

                ControlButton(
                    icon = Icons.Default.FastForward,
                    label = "+10s",
                    isSelected = selectedIndex == 2
                )
            }
        }
    }
}

@Composable
private fun ControlButton(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    isLarge: Boolean = false
) {
    val size = if (isLarge) 72.dp else 56.dp
    val iconSize = if (isLarge) 40.dp else 28.dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(
                    if (isSelected) Color.White else Color.White.copy(alpha = 0.2f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) Color.Black else Color.White,
                modifier = Modifier.size(iconSize)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = label,
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

private fun formatTime(seconds: Float): String {
    val totalSeconds = seconds.toInt()
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val secs = totalSeconds % 60

    return if (hours > 0) {
        String.format(java.util.Locale.US, "%d:%02d:%02d", hours, minutes, secs)
    } else {
        String.format(java.util.Locale.US, "%d:%02d", minutes, secs)
    }
}
