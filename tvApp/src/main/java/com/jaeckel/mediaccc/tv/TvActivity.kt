package com.jaeckel.mediaccc.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import androidx.tv.material3.Icon
import androidx.tv.material3.NavigationDrawer
import androidx.tv.material3.NavigationDrawerItem
import androidx.tv.material3.NavigationDrawerItemDefaults
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import androidx.tv.material3.darkColorScheme
import com.jaeckel.mediaccc.tv.navigation.ConferenceDetailRoute
import com.jaeckel.mediaccc.tv.navigation.EventDetailRoute
import com.jaeckel.mediaccc.tv.navigation.FavoritesRoute
import com.jaeckel.mediaccc.tv.navigation.HistoryRoute
import com.jaeckel.mediaccc.tv.navigation.HomeRoute
import com.jaeckel.mediaccc.tv.navigation.PlayerRoute
import com.jaeckel.mediaccc.tv.navigation.SearchRoute
import com.jaeckel.mediaccc.tv.navigation.SettingsRoute
import com.jaeckel.mediaccc.tv.ui.ConferenceDetailScreen
import com.jaeckel.mediaccc.tv.ui.EventDetailScreen
import com.jaeckel.mediaccc.tv.ui.FavoritesScreen
import com.jaeckel.mediaccc.tv.ui.HistoryScreen
import com.jaeckel.mediaccc.tv.ui.SearchScreen
import com.jaeckel.mediaccc.tv.ui.SettingsScreen
import com.jaeckel.mediaccc.tv.ui.TvHomeScreen
import androidx.tv.material3.MaterialTheme as TvMaterialTheme

import androidx.compose.material.icons.filled.VideoLibrary
import com.jaeckel.mediaccc.tv.navigation.ConferencesRoute
import com.jaeckel.mediaccc.tv.ui.ConferencesScreen
import androidx.compose.ui.res.stringResource

class TvActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TvMaterialTheme(colorScheme = darkColorScheme()) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RectangleShape
                ) {
                    TvNavHost()
                }
            }
        }
    }
}

@Composable
fun TvNavHost() {
    val backStack = remember { mutableStateListOf<NavKey>(HomeRoute) }
    val currentRoute by remember { derivedStateOf { backStack.lastOrNull() } }

    val isTopLevel = when (currentRoute) {
        HomeRoute, SearchRoute, ConferencesRoute, FavoritesRoute, HistoryRoute, SettingsRoute -> true
        is ConferenceDetailRoute -> true
        else -> false
    }

    if (isTopLevel) {
        NavigationDrawer(
            drawerContent = {
                val drawerItemColors = NavigationDrawerItemDefaults.colors(
                    contentColor = TvMaterialTheme.colorScheme.onSurface,
                    inactiveContentColor = TvMaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    selectedContentColor = TvMaterialTheme.colorScheme.onSurface,
                    focusedContentColor = TvMaterialTheme.colorScheme.inverseOnSurface,
                    pressedContentColor = TvMaterialTheme.colorScheme.inverseOnSurface,
                )

                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(12.dp)
                ) {
                    NavigationDrawerItem(
                        selected = currentRoute == HomeRoute,
                        onClick = {
                            if (currentRoute != HomeRoute) {
                                backStack.clear()
                                backStack.add(HomeRoute)
                            }
                        },
                        leadingContent = { Icon(Icons.Default.Home, contentDescription = null) },
                        colors = drawerItemColors
                    ) {
                        Text(stringResource(R.string.home))
                    }
                    NavigationDrawerItem(
                        selected = currentRoute == SearchRoute,
                        onClick = {
                            if (currentRoute != SearchRoute) {
                                backStack.clear()
                                backStack.add(HomeRoute)
                                backStack.add(SearchRoute)
                            }
                        },
                        leadingContent = { Icon(Icons.Default.Search, contentDescription = null) },
                        colors = drawerItemColors
                    ) {
                        Text(stringResource(R.string.search))
                    }
                    NavigationDrawerItem(
                        selected = currentRoute == ConferencesRoute,
                        onClick = {
                            if (currentRoute != ConferencesRoute) {
                                backStack.clear()
                                backStack.add(HomeRoute)
                                backStack.add(ConferencesRoute)
                            }
                        },
                        leadingContent = { Icon(Icons.Default.VideoLibrary, contentDescription = null) },
                        colors = drawerItemColors
                    ) {
                        Text(stringResource(R.string.conferences))
                    }
                    NavigationDrawerItem(
                        selected = currentRoute == FavoritesRoute,
                        onClick = {
                            if (currentRoute != FavoritesRoute) {
                                backStack.clear()
                                backStack.add(HomeRoute)
                                backStack.add(FavoritesRoute)
                            }
                        },
                        leadingContent = { Icon(Icons.Default.Favorite, contentDescription = null) },
                        colors = drawerItemColors
                    ) {
                        Text(stringResource(R.string.favorites))
                    }
                    NavigationDrawerItem(
                        selected = currentRoute == HistoryRoute,
                        onClick = {
                            if (currentRoute != HistoryRoute) {
                                backStack.clear()
                                backStack.add(HomeRoute)
                                backStack.add(HistoryRoute)
                            }
                        },
                        leadingContent = { Icon(Icons.Default.History, contentDescription = null) },
                        colors = drawerItemColors
                    ) {
                        Text(stringResource(R.string.history))
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    NavigationDrawerItem(
                        selected = currentRoute == SettingsRoute,
                        onClick = {
                            if (currentRoute != SettingsRoute) {
                                backStack.clear()
                                backStack.add(HomeRoute)
                                backStack.add(SettingsRoute)
                            }
                        },
                        leadingContent = { Icon(Icons.Default.Settings, contentDescription = null) },
                        colors = drawerItemColors
                    ) {
                        Text(stringResource(R.string.settings))
                    }
                }
            }
        ) {
            TvNavDisplay(backStack)
        }
    } else {
        TvNavDisplay(backStack)
    }
}

@Composable
fun TvNavDisplay(backStack: MutableList<NavKey>) {
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
                    },
                    onHistoryEventClick = { guid ->
                        backStack.add(EventDetailRoute(guid))
                    },
                    onLiveStreamClick = { stream ->
                        stream.hlsUrl?.let { url ->
                            backStack.add(PlayerRoute(
                                eventGuid = "",
                                videoUrl = url,
                                title = stream.roomName,
                                speakers = stream.currentTalkSpeaker ?: "",
                                date = "",
                                conference = stream.conferenceName,
                                duration = 0
                            ))
                        }
                    }
                )
            }

            entry<SearchRoute> {
                SearchScreen(
                    onEventClick = { event ->
                        backStack.add(EventDetailRoute(event.guid))
                    }
                )
            }
            entry<ConferencesRoute> {
                ConferencesScreen(
                    onConferenceClick = { conference ->
                        backStack.add(ConferenceDetailRoute(conference.acronym))
                    }
                )
            }
            entry<FavoritesRoute> { FavoritesScreen() }
            entry<HistoryRoute> {
                HistoryScreen(
                    onEventClick = { guid ->
                        backStack.add(EventDetailRoute(guid))
                    }
                )
            }
            entry<SettingsRoute> { SettingsScreen() }

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
                    onPlayClick = { eventGuid, videoUrl, title, speakers, date, conference, duration ->
                        backStack.add(PlayerRoute(
                            eventGuid = eventGuid,
                            videoUrl = videoUrl,
                            title = title,
                            speakers = speakers,
                            date = date,
                            conference = conference,
                            duration = duration
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
                    eventGuid = route.eventGuid,
                    videoUrl = route.videoUrl,
                    title = route.title,
                    speakers = route.speakers,
                    date = route.date,
                    conference = route.conference,
                    durationFromEvent = route.duration
                )
            }
        }
    )
}
