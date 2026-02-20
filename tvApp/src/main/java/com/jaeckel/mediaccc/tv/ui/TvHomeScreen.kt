package com.jaeckel.mediaccc.tv.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.Carousel
import androidx.tv.material3.CarouselDefaults
import androidx.tv.material3.CarouselState
import androidx.tv.material3.ExperimentalTvMaterial3Api
import coil3.compose.AsyncImage
import com.jaeckel.mediaccc.api.model.Conference
import com.jaeckel.mediaccc.api.model.Event
import com.jaeckel.mediaccc.data.db.PlaybackHistoryEntity
import com.jaeckel.mediaccc.tv.ui.cards.ConferenceCard
import com.jaeckel.mediaccc.tv.ui.cards.EventCard
import com.jaeckel.mediaccc.viewmodel.HistoryViewModel
import com.jaeckel.mediaccc.viewmodel.HomeViewModel
import com.jaeckel.mediaccc.tv.R
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvHomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
    historyViewModel: HistoryViewModel = koinViewModel(),
    onEventClick: (Event) -> Unit,
    onConferenceClick: (Conference) -> Unit,
    onHistoryEventClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val continueWatching by historyViewModel.continueWatching.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A2E))
    ) {
        when {
            uiState.isLoading && uiState.promotedEvents.isEmpty() && uiState.recentEvents.isEmpty() -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )
            }

            uiState.errorMessage != null && uiState.promotedEvents.isEmpty() -> {
                Text(
                    text = stringResource(R.string.error_message, uiState.errorMessage ?: ""),
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            else -> {
                val lazyListState = rememberLazyListState()
                var recentEventsFocused by remember { mutableStateOf(false) }
                val firstRecentEventFocusRequester = remember { FocusRequester() }
                val firstContinueWatchingFocusRequester = remember { FocusRequester() }

                val recentEventsIndex = remember(uiState.promotedEvents, continueWatching) {
                    var index = 0
                    if (uiState.promotedEvents.isNotEmpty()) index++
                    index++ // Spacer
                    if (continueWatching.isNotEmpty()) index++
                    index
                }

                // Scroll to show recent events row when it gets focus
                LaunchedEffect(recentEventsFocused) {
                    if (recentEventsFocused) {
                        // Scroll to the recent events row to make it fully visible
                        lazyListState.animateScrollToItem(recentEventsIndex)
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = lazyListState,
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    // Hero Carousel for promoted events
                    if (uiState.promotedEvents.isNotEmpty()) {
                        item {
                            PromotedCarousel(
                                events = uiState.promotedEvents,
                                onEventClick = onEventClick,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(400.dp)
                                    .focusProperties {
                                        down = if (continueWatching.isNotEmpty()) {
                                            firstContinueWatchingFocusRequester
                                        } else {
                                            firstRecentEventFocusRequester
                                        }
                                    }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // Continue Watching Section
                    if (continueWatching.isNotEmpty()) {
                        item {
                            Column {
                                Text(
                                    text = stringResource(R.string.continue_watching),
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 48.dp)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                ContinueWatchingRow(
                                    entries = continueWatching,
                                    onEventClick = onHistoryEventClick,
                                    firstItemFocusRequester = firstContinueWatchingFocusRequester
                                )
                            }
                        }
                    }

                    // Recent Events Section
                    if (uiState.recentEvents.isNotEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .onFocusChanged { focusState ->
                                        recentEventsFocused = focusState.hasFocus
                                    }
                            ) {
                                Text(
                                    text = stringResource(R.string.recent_events),
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 48.dp)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                EventRow(
                                    events = uiState.recentEvents,
                                    onEventClick = onEventClick,
                                    firstItemFocusRequester = firstRecentEventFocusRequester
                                )
                            }
                        }
                    }

                    // Conferences Section
                    if (uiState.conferences.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        item {
                            Column {
                                Text(
                                    text = stringResource(R.string.conferences),
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 48.dp)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                ConferenceRow(
                                    conferences = uiState.conferences,
                                    onConferenceClick = onConferenceClick
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PromotedCarousel(
    events: List<Event>,
    onEventClick: (Event) -> Unit,
    modifier: Modifier = Modifier
) {
    val carouselState = remember { CarouselState() }

    Carousel(
        itemCount = events.size,
        modifier = modifier,
        carouselState = carouselState,
        carouselIndicator = {
            CarouselDefaults.IndicatorRow(
                itemCount = events.size,
                activeItemIndex = carouselState.activeItemIndex,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            )
        }
    ) { index ->
        val event = events[index]
        var isFocused by remember { mutableStateOf(false) }
        val scale by animateFloatAsState(if (isFocused) 1.05f else 1f)

        Card(
            onClick = { onEventClick(event) },
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isFocused) 8.dp else 0.dp)
                .onFocusChanged { isFocused = it.hasFocus }
                .scale(scale),
            border = CardDefaults.border(
                focusedBorder = Border(BorderStroke(4.dp, Color.White)),
                border = Border(BorderStroke(0.dp, Color.Transparent))
            )
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Background image
                AsyncImage(
                    model = event.posterUrl ?: event.thumbUrl,
                    contentDescription = event.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.8f)
                                ),
                                startY = 100f
                            )
                        )
                )

                // Event info
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(48.dp)
                ) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    event.conferenceTitle?.let { conference ->
                        Text(
                            text = conference,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                    event.subtitle?.let { subtitle ->
                        if (subtitle.isNotBlank()) {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.6f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun EventRow(
    events: List<Event>,
    onEventClick: (Event) -> Unit,
    firstItemFocusRequester: FocusRequester? = null
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 48.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(events, key = { _, it -> it.guid }) { index, event ->
            EventCard(
                event = event,
                onClick = { onEventClick(event) },
                modifier = if (index == 0 && firstItemFocusRequester != null) {
                    Modifier.focusRequester(firstItemFocusRequester)
                } else {
                    Modifier
                }
            )
        }
    }
}


@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ConferenceRow(
    conferences: List<Conference>,
    onConferenceClick: (Conference) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 48.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(conferences, key = { it.acronym }) { conference ->
            ConferenceCard(
                conference = conference,
                onClick = { onConferenceClick(conference) }
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ContinueWatchingRow(
    entries: List<PlaybackHistoryEntity>,
    onEventClick: (String) -> Unit,
    firstItemFocusRequester: FocusRequester? = null
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 48.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(entries.take(10), key = { _, it -> it.eventGuid }) { index, entry ->
            val progress = (entry.sliderPos / 1000f).coerceIn(0f, 1f)
            var isFocused by remember { mutableStateOf(false) }
            val scale by animateFloatAsState(if (isFocused) 1.05f else 1f)

            Card(
                onClick = { onEventClick(entry.eventGuid) },
                modifier = Modifier
                    .width(240.dp)
                    .height(180.dp)
                    .scale(scale)
                    .onFocusChanged { isFocused = it.isFocused }
                    .then(
                        if (index == 0 && firstItemFocusRequester != null) {
                            Modifier.focusRequester(firstItemFocusRequester)
                        } else {
                            Modifier
                        }
                    ),
                colors = CardDefaults.colors(
                    containerColor = Color(0xFF2A2A4E),
                    focusedContainerColor = Color(0xFF3A3A5E)
                )
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        AsyncImage(
                            model = entry.thumbUrl,
                            contentDescription = entry.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                                        startY = 50f
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = entry.title,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            entry.conferenceTitle?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.7f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF6650A4),
                        trackColor = Color.White.copy(alpha = 0.2f)
                    )
                }
            }
        }
    }
}

