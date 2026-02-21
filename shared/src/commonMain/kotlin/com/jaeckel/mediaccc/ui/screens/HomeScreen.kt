package com.jaeckel.mediaccc.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jaeckel.mediaccc.api.model.Conference
import com.jaeckel.mediaccc.api.model.Event
import com.jaeckel.mediaccc.data.db.PlaybackHistoryEntity
import com.jaeckel.mediaccc.ui.components.ConferenceCard
import com.jaeckel.mediaccc.ui.components.EventCard
import com.jaeckel.mediaccc.ui.components.EventCardCompact
import com.jaeckel.mediaccc.ui.components.HistoryCardCompact
import com.jaeckel.mediaccc.ui.components.LiveStreamCard
import com.jaeckel.mediaccc.viewmodel.HistoryViewModel
import com.jaeckel.mediaccc.viewmodel.HomeViewModel
import com.jaeckel.mediaccc.viewmodel.LiveStreamItem
import mediaccc.shared.generated.resources.Res
import mediaccc.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
    historyViewModel: HistoryViewModel = koinViewModel(),
    onEventClick: (Event) -> Unit,
    onConferenceClick: (Conference) -> Unit,
    onHistoryEventClick: (String) -> Unit,
    onLiveStreamClick: (LiveStreamItem) -> Unit,
    onOpenDrawer: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val continueWatching by historyViewModel.continueWatching.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.app_name)) },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = stringResource(Res.string.menu))
                    }
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
                uiState.isLoading && uiState.promotedEvents.isEmpty() -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.errorMessage != null && uiState.promotedEvents.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(Res.string.error_message, uiState.errorMessage ?: ""),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                else -> {
                    HomeContent(
                        liveStreams = uiState.liveStreams,
                        continueWatching = continueWatching,
                        promotedEvents = uiState.promotedEvents,
                        recentEvents = uiState.recentEvents,
                        conferences = uiState.conferences,
                        onEventClick = onEventClick,
                        onConferenceClick = onConferenceClick,
                        onHistoryEventClick = onHistoryEventClick,
                        onLiveStreamClick = onLiveStreamClick
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeContent(
    liveStreams: List<LiveStreamItem>,
    continueWatching: List<PlaybackHistoryEntity>,
    promotedEvents: List<Event>,
    recentEvents: List<Event>,
    conferences: List<Conference>,
    onEventClick: (Event) -> Unit,
    onConferenceClick: (Conference) -> Unit,
    onHistoryEventClick: (String) -> Unit,
    onLiveStreamClick: (LiveStreamItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Live Streams Section
        if (liveStreams.isNotEmpty()) {
            item {
                SectionHeader(title = stringResource(Res.string.live_streams))
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(liveStreams, key = { "${it.conferenceName}-${it.roomName}" }) { stream ->
                        LiveStreamCard(
                            item = stream,
                            onClick = { onLiveStreamClick(stream) },
                            modifier = Modifier.width(280.dp)
                        )
                    }
                }
            }
        }

        // Continue Watching Section
        if (continueWatching.isNotEmpty()) {
            item {
                SectionHeader(title = stringResource(Res.string.continue_watching))
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(continueWatching.take(10), key = { it.eventGuid }) { entry ->
                        HistoryCardCompact(
                            entry = entry,
                            onClick = { onHistoryEventClick(entry.eventGuid) },
                            modifier = Modifier
                                .width(200.dp)
                                .height(145.dp)
                        )
                    }
                }
            }
        }

        // Promoted Events Section
        if (promotedEvents.isNotEmpty()) {
            item { SectionHeader(title = stringResource(Res.string.featured)) }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(promotedEvents, key = { it.guid }) { event ->
                        EventCard(
                            event = event,
                            onClick = { onEventClick(event) },
                            modifier = Modifier.width(300.dp)
                        )
                    }
                }
            }
        }

        // Recent Events Section
        if (recentEvents.isNotEmpty()) {
            item { SectionHeader(title = stringResource(Res.string.recent)) }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(recentEvents, key = { it.guid }) { event ->
                        EventCardCompact(
                            event = event,
                            onClick = { onEventClick(event) },
                            modifier = Modifier
                                .width(200.dp)
                                .height(130.dp)
                        )
                    }
                }
            }
        }

        // Conferences Section
        if (conferences.isNotEmpty()) {
            item { SectionHeader(title = stringResource(Res.string.conferences)) }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(conferences, key = { it.acronym }) { conference ->
                        ConferenceCard(
                            conference = conference,
                            onClick = { onConferenceClick(conference) },
                            modifier = Modifier
                                .width(150.dp)
                                .height(150.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

