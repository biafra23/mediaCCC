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
    var currentTime by remember { mutableFloatStateOf(0f) }

    playerHost.onEvent = { event ->
        when (event) {
            is MediaPlayerEvent.MuteChange -> println("Mute status changed: ${event.isMuted}")
            is MediaPlayerEvent.PauseChange -> println("Pause status changed: ${event.isPaused}")
            is MediaPlayerEvent.BufferChange -> println("Buffering status: ${event.isBuffering}")
            is MediaPlayerEvent.CurrentTimeChange -> {
                println("Current time: ${event.currentTime}s")
                currentTime = event.currentTime
            }
            is MediaPlayerEvent.TotalTimeChange -> println("Total duration: ${event.totalTime}s")
            is MediaPlayerEvent.FullScreenChange -> println("FullScreen status: ${event.isFullScreen}")
            MediaPlayerEvent.MediaEnd -> println("Playback ended")
            else -> {
                println("Unhandled event: $event")
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusable() // Crucial for receiving key events on Android TV
            .onKeyEvent { keyEvent ->
                onKeyEvent(keyEvent, playerHost, currentTime)
            }
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
//                playIconResource = ComposeResourceDrawable(Res.drawable.icn_play),
//                pauseIconResource = ComposeResourceDrawable(Res.drawable.icn_pause),
            )
        )
    }
}

fun onKeyEvent(
    keyEvent: androidx.compose.ui.input.key.KeyEvent,
    playerHost: MediaPlayerHost,
    currentTime: Float
): Boolean {
    if (keyEvent.type == KeyEventType.KeyDown) {
        when (keyEvent.nativeKeyEvent.keyCode) {
            KeyEvent.KEYCODE_DPAD_CENTER,
            KeyEvent.KEYCODE_ENTER,
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                println("KEYCODE_DPAD_CENTER")
                playerHost.togglePlayPause()
                return true
            }

            KeyEvent.KEYCODE_MEDIA_PLAY -> {
                println("KEYCODE_MEDIA_PLAY")
                playerHost.play()
                return true
            }

            KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                println("KEYCODE_MEDIA_PAUSE")
                playerHost.pause()
                return true
            }

            KeyEvent.KEYCODE_DPAD_UP -> {
                println("KEYCODE_DPAD_UP")
                return true
            }

            KeyEvent.KEYCODE_DPAD_DOWN -> {
                println("KEYCODE_DPAD_DOWN")
                return true
            }

            KeyEvent.KEYCODE_DPAD_LEFT -> {
                println("KEYCODE_DPAD_LEFT")
                playerHost.seekTo(currentTime - 10.0f)
                return true
            }

            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                println("KEYCODE_DPAD_RIGHT")
                playerHost.seekTo(currentTime + 10.0f)
                return true
            }

            else -> {
                println(keyEvent.nativeKeyEvent.keyCode)
                return true
            }
        }
    }
    return false
}

