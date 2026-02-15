package com.jaeckel.mediaccc.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import chaintech.videoplayer.host.MediaPlayerHost
import chaintech.videoplayer.ui.video.VideoPlayerComposable
import com.jaeckel.mediaccc.tv.navigation.EventDetailRoute
import com.jaeckel.mediaccc.tv.navigation.HomeRoute
import com.jaeckel.mediaccc.tv.navigation.PlayerRoute
import com.jaeckel.mediaccc.tv.ui.EventDetailScreen
import com.jaeckel.mediaccc.tv.ui.TvHomeScreen

class TvActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                TvNavHost()
            }
        }
    }
}

@Composable
fun TvNavHost() {
    val backStack = remember { mutableStateListOf<NavKey>(HomeRoute) }

    NavDisplay(
        backStack = backStack,
        entryProvider = entryProvider {
            entry<HomeRoute> {
                TvHomeScreen(
                    onEventClick = { event ->
                        backStack.add(EventDetailRoute(event.guid))
                    }
                )
            }

            entry<EventDetailRoute> { route ->
                EventDetailScreen(
                    eventGuid = route.eventGuid,
                    onPlayClick = { videoUrl ->
                        backStack.add(PlayerRoute(videoUrl))
                    },
                    onBackClick = {
                        if (backStack.size > 1) {
                            backStack.removeLast()
                        }
                    }
                )
            }

            entry<PlayerRoute> { route ->
                TvPlayerScreen(
                    videoUrl = route.videoUrl,
                    onBackClick = {
                        if (backStack.size > 1) {
                            backStack.removeLast()
                        }
                    }
                )
            }
        }
    )
}

@Composable
fun TvPlayerScreen(
    videoUrl: String,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (videoUrl.isNotBlank()) {
            val playerHost = remember(videoUrl) {
                MediaPlayerHost(mediaUrl = videoUrl)
            }
            VideoPlayerComposable(
                modifier = Modifier.fillMaxSize(),
                playerHost = playerHost
            )
        } else {
            Text(
                text = "No video URL available",
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}
