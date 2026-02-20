package com.jaeckel.mediaccc.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.jaeckel.mediaccc.ui.screens.ConferenceDetailScreen
import com.jaeckel.mediaccc.ui.screens.EventDetailScreen
import com.jaeckel.mediaccc.ui.screens.HistoryScreen
import com.jaeckel.mediaccc.ui.screens.HomeScreen
import com.jaeckel.mediaccc.ui.screens.PlayerScreen

@Composable
fun AppNavHost() {
    val backStack = remember { mutableStateListOf<NavKey>(HomeRoute) }

    NavDisplay(
        backStack = backStack,
        entryProvider = entryProvider {
            entry<HomeRoute> {
                HomeScreen(
                    onEventClick = { event ->
                        backStack.add(EventDetailRoute(event.guid))
                    },
                    onConferenceClick = { conference ->
                        backStack.add(ConferenceDetailRoute(conference.acronym))
                    },
                    onHistoryEventClick = { guid ->
                        backStack.add(EventDetailRoute(guid))
                    },
                    onHistoryClick = {
                        backStack.add(HistoryRoute)
                    }
                )
            }

            entry<HistoryRoute> {
                HistoryScreen(
                    onEventClick = { guid ->
                        backStack.add(EventDetailRoute(guid))
                    },
                    onBackClick = {
                        if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
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
                        backStack.add(
                            PlayerRoute(
                                videoUrl = videoUrl,
                                title = title,
                                speakers = speakers,
                                date = date,
                                conference = conference
                            )
                        )
                    },
                    onBackClick = {
                        if (backStack.size > 1) {
                            backStack.removeAt(backStack.lastIndex)
                        }
                    }
                )
            }

            entry<PlayerRoute> { route ->
                PlayerScreen(
                    videoUrl = route.videoUrl,
                    title = route.title,
                    onBackClick = {
                        if (backStack.size > 1) {
                            backStack.removeAt(backStack.lastIndex)
                        }
                    }
                )
            }
        }
    )
}
