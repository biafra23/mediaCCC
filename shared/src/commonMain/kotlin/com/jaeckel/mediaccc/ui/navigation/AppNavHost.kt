package com.jaeckel.mediaccc.ui.navigation

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import androidx.compose.material.icons.filled.List
import com.jaeckel.mediaccc.ui.screens.ConferenceDetailScreen
import com.jaeckel.mediaccc.ui.screens.ConferencesScreen
import com.jaeckel.mediaccc.ui.screens.EventDetailScreen
import com.jaeckel.mediaccc.ui.screens.FavoritesScreen
import com.jaeckel.mediaccc.ui.screens.HistoryScreen
import com.jaeckel.mediaccc.ui.screens.HomeScreen
import com.jaeckel.mediaccc.ui.screens.PlayerScreen
import com.jaeckel.mediaccc.ui.screens.QueueScreen
import com.jaeckel.mediaccc.ui.screens.SearchScreen
import com.jaeckel.mediaccc.ui.screens.SettingsScreen
import kotlinx.coroutines.launch
import mediaccc.shared.generated.resources.Res
import mediaccc.shared.generated.resources.*
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private data class DrawerItem(
    val labelRes: StringResource,
    val icon: ImageVector,
    val route: NavKey
)

private val topDrawerItems = listOf(
    DrawerItem(Res.string.home, Icons.Default.Home, HomeRoute),
    DrawerItem(Res.string.search, Icons.Default.Search, SearchRoute),
    DrawerItem(Res.string.conferences, Icons.Default.VideoLibrary, ConferencesRoute),
    DrawerItem(Res.string.favorites, Icons.Default.Favorite, FavoritesRoute),
    DrawerItem(Res.string.history, Icons.Default.History, HistoryRoute),
    DrawerItem(Res.string.queue, Icons.Default.List, QueueRoute),
)

private val bottomDrawerItems = listOf(
    DrawerItem(Res.string.settings, Icons.Default.Settings, SettingsRoute),
)

@Composable
fun AppNavHost() {
    val backStack = remember { mutableStateListOf<NavKey>(HomeRoute) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentRoute by remember { derivedStateOf { backStack.lastOrNull() } }

    val isTopLevel = currentRoute is HomeRoute ||
            currentRoute is SearchRoute ||
            currentRoute is ConferencesRoute ||
            currentRoute is FavoritesRoute ||
            currentRoute is HistoryRoute ||
            currentRoute is QueueRoute ||
            currentRoute is SettingsRoute

    fun navigateToDrawerRoute(route: NavKey) {
        if (currentRoute != route) {
            backStack.clear()
            backStack.add(HomeRoute)
            if (route != HomeRoute) backStack.add(route)
        }
        scope.launch { drawerState.close() }
    }

    val isVideoScreen = currentRoute is EventDetailRoute || currentRoute is PlayerRoute

    BoxWithConstraints {
        val isWideScreen = maxWidth >= 840.dp

        if (isWideScreen && !isVideoScreen) {
            PermanentNavigationDrawer(
                drawerContent = {
                    PermanentDrawerSheet(modifier = Modifier.width(280.dp)) {
                        DrawerSheetContent(
                            currentRoute = currentRoute,
                            onNavigate = { navigateToDrawerRoute(it) }
                        )
                    }
                }
            ) {
                AppNavDisplay(
                    backStack = backStack,
                    onOpenDrawer = {},
                    showMenuButton = false
                )
            }
        } else if (isWideScreen) {
            AppNavDisplay(
                backStack = backStack,
                onOpenDrawer = {},
                showMenuButton = false
            )
        } else {
            ModalNavigationDrawer(
                drawerState = drawerState,
                gesturesEnabled = isTopLevel,
                drawerContent = {
                    ModalDrawerSheet(modifier = Modifier.width(280.dp)) {
                        DrawerSheetContent(
                            currentRoute = currentRoute,
                            onNavigate = { navigateToDrawerRoute(it) }
                        )
                    }
                }
            ) {
                AppNavDisplay(
                    backStack = backStack,
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    showMenuButton = true
                )
            }
        }
    }
}

@Composable
private fun DrawerSheetContent(
    currentRoute: NavKey?,
    onNavigate: (NavKey) -> Unit
) {
    Text(
        text = stringResource(Res.string.app_name),
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.padding(horizontal = 28.dp, vertical = 24.dp)
    )
    HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(horizontal = 12.dp)
    ) {
        topDrawerItems.forEach { item ->
            NavigationDrawerItem(
                label = { Text(stringResource(item.labelRes)) },
                icon = { Icon(item.icon, contentDescription = null) },
                selected = currentRoute == item.route,
                onClick = { onNavigate(item.route) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        HorizontalDivider()
        Spacer(modifier = Modifier.height(8.dp))

        bottomDrawerItems.forEach { item ->
            NavigationDrawerItem(
                label = { Text(stringResource(item.labelRes)) },
                icon = { Icon(item.icon, contentDescription = null) },
                selected = currentRoute == item.route,
                onClick = { onNavigate(item.route) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun AppNavDisplay(
    backStack: MutableList<NavKey>,
    onOpenDrawer: () -> Unit,
    showMenuButton: Boolean = true
) {
    fun popBack() {
        if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
    }

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
                    onLiveStreamClick = { stream ->
                        stream.hlsUrl?.let { url ->
                            backStack.add(
                                PlayerRoute(
                                    videoUrl = url,
                                    title = stream.roomName,
                                    speakers = stream.currentTalkSpeaker ?: "",
                                    date = "",
                                    conference = stream.conferenceName
                                )
                            )
                        }
                    },
                    onOpenDrawer = onOpenDrawer,
                    showMenuButton = showMenuButton
                )
            }

            entry<SearchRoute> {
                SearchScreen(
                    onEventClick = { event ->
                        backStack.add(EventDetailRoute(event.guid))
                    },
                    onBackClick = ::popBack
                )
            }

            entry<ConferencesRoute> {
                ConferencesScreen(
                    onConferenceClick = { conference ->
                        backStack.add(ConferenceDetailRoute(conference.acronym))
                    },
                    onBackClick = ::popBack
                )
            }

            entry<FavoritesRoute> {
                FavoritesScreen(
                    onEventClick = { guid ->
                        backStack.add(EventDetailRoute(guid))
                    },
                    onBackClick = ::popBack
                )
            }

            entry<HistoryRoute> {
                HistoryScreen(
                    onEventClick = { guid ->
                        backStack.add(EventDetailRoute(guid))
                    },
                    onBackClick = ::popBack
                )
            }

            entry<QueueRoute> {
                QueueScreen(
                    onEventClick = { videoUrl, title, speakers, date, conference, eventGuid ->
                        backStack.add(
                            PlayerRoute(
                                videoUrl = videoUrl,
                                title = title,
                                speakers = speakers,
                                date = date,
                                conference = conference,
                                eventGuid = eventGuid
                            )
                        )
                    },
                    onBackClick = ::popBack
                )
            }

            entry<SettingsRoute> {
                SettingsScreen(onBackClick = ::popBack)
            }

            entry<ConferenceDetailRoute> { route ->
                ConferenceDetailScreen(
                    acronym = route.acronym,
                    onEventClick = { event ->
                        backStack.add(EventDetailRoute(event.guid))
                    },
                    onBackClick = ::popBack
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
                    onBackClick = ::popBack
                )
            }

            entry<PlayerRoute> { route ->
                PlayerScreen(
                    videoUrl = route.videoUrl,
                    title = route.title,
                    eventGuid = route.eventGuid,
                    onBackClick = ::popBack
                )
            }
        }
    )
}
