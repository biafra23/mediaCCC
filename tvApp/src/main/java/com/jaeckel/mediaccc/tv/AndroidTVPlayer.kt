package com.jaeckel.mediaccc.tv

import android.view.KeyEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import chaintech.videoplayer.host.MediaPlayerEvent
import chaintech.videoplayer.host.MediaPlayerHost
import chaintech.videoplayer.model.VideoPlayerConfig
import chaintech.videoplayer.ui.video.VideoPlayerComposable

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun AndroidTVPlayer(videoUrl: String) {
    val playerHost = remember {
        MediaPlayerHost(videoUrl)
    }
    // We strictly rely on this state variable because the library doesn't expose a getter for time
    var currentTime by remember { mutableFloatStateOf(0f) }
    var currentVolume by remember { mutableFloatStateOf(0f) }

    // Defined locally to capture 'currentTime' and 'playerHost' without passing them as arguments
    fun handleKeyEvent(keyEvent: androidx.compose.ui.input.key.KeyEvent): Boolean {
        if (keyEvent.type == KeyEventType.KeyDown) {
            when (keyEvent.nativeKeyEvent.keyCode) {
                KeyEvent.KEYCODE_DPAD_CENTER,
                KeyEvent.KEYCODE_ENTER,
                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                    playerHost.togglePlayPause()
                    return true
                }

                KeyEvent.KEYCODE_MEDIA_PLAY -> {
                    playerHost.play()
                    playerHost.unmute()
                    return true
                }

                KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                    playerHost.pause()
                    playerHost.mute()
                    return true
                }

                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    // Safe seek preventing negative values
                    playerHost.seekTo((currentTime - 10.0f).coerceAtLeast(0f))
                    return true
                }

                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    playerHost.seekTo(currentTime + 10.0f)
                    return true
                }

                KeyEvent.KEYCODE_DPAD_UP -> {
                    currentVolume += 0.1f
                    currentVolume = currentVolume.coerceAtMost(1f)
                    println("Increase Volume: $currentVolume")
                    playerHost.unmute()
                    playerHost.setVolume(currentVolume)
                    return true
                }

                KeyEvent.KEYCODE_DPAD_DOWN -> {
                    currentVolume -= 0.1f
                    currentVolume = currentVolume.coerceAtLeast(0f)
                    println("Decrease Volume: $currentVolume")
                    playerHost.setVolume(currentVolume)
                    return true
                }

                else -> {
                    // Swallow other D-Pad events if necessary or allow default focus handling
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
            }

            is MediaPlayerEvent.MuteChange -> {
                println("Mute change: ${event.isMuted}")
            }

            is MediaPlayerEvent.PauseChange -> {
                println("Pause change, isPaused: ${event.isPaused}")
            }

            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusable() // Crucial for receiving key events on Android TV
            .onKeyEvent(::handleKeyEvent)
    ) {
        Button(onClick = { /*TODO*/ }) {
            Text("Press me")
        }
        VideoPlayerComposable(
            modifier = Modifier.fillMaxSize(),
            playerHost = playerHost,
            playerConfig = VideoPlayerConfig(
                showControls = false,
                isPauseResumeEnabled = true,
                isSeekBarVisible = true,
                isDurationVisible = true,
                seekBarThumbColor = Color.Red,
                seekBarActiveTrackColor = Color.Red,
                seekBarInactiveTrackColor = Color.White,
                durationTextColor = Color.White,
                seekBarBottomPadding = 10.dp,
                pauseResumeIconSize = 40.dp,
                isAutoHideControlEnabled = true,
                controlHideIntervalSeconds = 5,
                isFastForwardBackwardEnabled = true,
            )
        )
    }
}

