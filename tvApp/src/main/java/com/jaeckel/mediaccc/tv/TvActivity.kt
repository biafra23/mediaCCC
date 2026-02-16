package com.jaeckel.mediaccc.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.jaeckel.mediaccc.tv.navigation.ConferenceDetailRoute
import com.jaeckel.mediaccc.tv.navigation.EventDetailRoute
import com.jaeckel.mediaccc.tv.navigation.HomeRoute
import com.jaeckel.mediaccc.tv.navigation.PlayerRoute
import com.jaeckel.mediaccc.tv.ui.ConferenceDetailScreen
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

    // Note: NavDisplay handles system back button automatically
    NavDisplay(
        backStack = backStack,
        entryProvider = entryProvider {
            entry<HomeRoute> {
                TvHomeScreen(
                    onEventClick = { event ->
                        backStack.add(EventDetailRoute(event.guid))
                    },
                    onConferenceClick = { conference ->
                        backStack.add(ConferenceDetailRoute(conference.acronym))
                    }
                )
            }

            entry<ConferenceDetailRoute> { route ->
                ConferenceDetailScreen(
                    acronym = route.acronym,
                    onEventClick = { event ->
                        backStack.add(EventDetailRoute(event.guid))
                    },
                    onBackClick = {
                        if (backStack.size > 1) {
                            backStack.removeAt(backStack.lastIndex)
                        }
                    }
                )
            }

            entry<EventDetailRoute> { route ->
                EventDetailScreen(
                    eventGuid = route.eventGuid,
                    onPlayClick = { videoUrl, title, speakers, date, conference ->
                        backStack.add(PlayerRoute(
                            videoUrl = videoUrl,
                            title = title,
                            speakers = speakers,
                            date = date,
                            conference = conference
                        ))
                    },
                    onBackClick = {
                        if (backStack.size > 1) {
                            backStack.removeAt(backStack.lastIndex)
                        }
                    }
                )
            }

            entry<PlayerRoute> { route ->
                AndroidTVPlayer(
                    videoUrl = route.videoUrl,
                    title = route.title,
                    speakers = route.speakers,
                    date = route.date,
                    conference = route.conference
                )
            }
        }
    )
}
