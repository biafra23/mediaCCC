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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
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
import com.jaeckel.mediaccc.data.repository.FavoritesRepository
import com.jaeckel.mediaccc.data.db.FavoriteEventDao
import com.jaeckel.mediaccc.data.db.FavoriteEventEntity
import com.jaeckel.mediaccc.viewmodel.HistoryViewModel
import com.jaeckel.mediaccc.viewmodel.HomeViewModel
import com.jaeckel.mediaccc.viewmodel.LiveStreamItem
import com.jaeckel.mediaccc.tv.R
import org.koin.compose.viewmodel.koinViewModel
import org.koin.compose.koinInject
import org.koin.compose.KoinApplication
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvHomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
    historyViewModel: HistoryViewModel = koinViewModel(),
    onEventClick: (Event) -> Unit,
    onConferenceClick: (Conference) -> Unit,
    onHistoryEventClick: (String) -> Unit = {},
    onLiveStreamClick: (LiveStreamItem) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val continueWatching by historyViewModel.continueWatching.collectAsState()

    TvHomeScreenContent(
        isLoading = uiState.isLoading,
        errorMessage = uiState.errorMessage,
        promotedEvents = uiState.promotedEvents,
        recentEvents = uiState.recentEvents,
        conferences = uiState.conferences,
        liveStreams = uiState.liveStreams,
        continueWatching = continueWatching,
        onEventClick = onEventClick,
        onConferenceClick = onConferenceClick,
        onHistoryEventClick = onHistoryEventClick,
        onLiveStreamClick = onLiveStreamClick
    )
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvHomeScreenContent(
    isLoading: Boolean,
    errorMessage: String?,
    promotedEvents: List<Event>,
    recentEvents: List<Event>,
    conferences: List<Conference>,
    liveStreams: List<LiveStreamItem>,
    continueWatching: List<PlaybackHistoryEntity>,
    onEventClick: (Event) -> Unit,
    onConferenceClick: (Conference) -> Unit,
    onHistoryEventClick: (String) -> Unit = {},
    onLiveStreamClick: (LiveStreamItem) -> Unit = {}
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A2E))
    ) {
        when {
            isLoading && promotedEvents.isEmpty() && recentEvents.isEmpty() -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )
            }

            errorMessage != null && promotedEvents.isEmpty() -> {
                Text(
                    text = stringResource(R.string.error_message, errorMessage),
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            else -> {
                val lazyListState = rememberLazyListState()
                var recentEventsFocused by remember { mutableStateOf(false) }
                val firstRecentEventFocusRequester = remember { FocusRequester() }
                val firstContinueWatchingFocusRequester = remember { FocusRequester() }

                // Index of the row right after the carousel (continue watching or recent events)
                val nextSectionAfterCarouselIndex = remember(liveStreams, promotedEvents) {
                    var index = 0
                    if (liveStreams.isNotEmpty()) index += 2 // live streams + spacer
                    if (promotedEvents.isNotEmpty()) index++ // carousel
                    index++ // spacer after carousel
                    index
                }

                val recentEventsIndex = remember(liveStreams, promotedEvents, continueWatching) {
                    var index = 0
                    if (liveStreams.isNotEmpty()) index += 2
                    if (promotedEvents.isNotEmpty()) index++
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
                    // Live Streams Section
                    if (liveStreams.isNotEmpty()) {
                        item {
                            Column {
                                Text(
                                    text = stringResource(R.string.live_streams),
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 48.dp)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                LiveStreamRow(
                                    streams = liveStreams,
                                    onStreamClick = onLiveStreamClick
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }

                    // Hero Carousel for promoted events
                    if (promotedEvents.isNotEmpty()) {
                        item {
                            PromotedCarousel(
                                events = promotedEvents,
                                onEventClick = onEventClick,
                                nextFocusDown = if (continueWatching.isNotEmpty()) {
                                    firstContinueWatchingFocusRequester
                                } else {
                                    firstRecentEventFocusRequester
                                },
                                scrollState = lazyListState,
                                scrollToIndex = nextSectionAfterCarouselIndex,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(400.dp)
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
                    if (recentEvents.isNotEmpty()) {
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
                                    events = recentEvents,
                                    onEventClick = onEventClick,
                                    firstItemFocusRequester = firstRecentEventFocusRequester
                                )
                            }
                        }
                    }

                    // Conferences Section
                    if (conferences.isNotEmpty()) {
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
                                    conferences = conferences,
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
    nextFocusDown: FocusRequester? = null,
    scrollState: LazyListState? = null,
    scrollToIndex: Int = 0,
    modifier: Modifier = Modifier
) {
    val carouselState = remember { CarouselState() }
    val scope = rememberCoroutineScope()

    Carousel(
        itemCount = events.size,
        modifier = modifier
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.DirectionDown) {
                    if (nextFocusDown != null) {
                        scope.launch {
                            scrollState?.animateScrollToItem(scrollToIndex)
                            nextFocusDown.requestFocus()
                        }
                        true
                    } else {
                        false
                    }
                } else {
                    false
                }
            },
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

        val favoritesRepository: FavoritesRepository = koinInject()
        val isFavorite by favoritesRepository.isFavorite(event.guid).collectAsState(initial = false)
        var showMenu by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        Box(modifier = Modifier.fillMaxSize()) {
            Card(
                onClick = { onEventClick(event) },
                onLongClick = { showMenu = true },
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

                    // Favorite star badge
                    if (isFavorite) {
                        Text(
                            text = "★",
                            color = Color(0xFFFFD700),
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp)
                        )
                    }

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

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            if (isFavorite) stringResource(R.string.remove_from_favorites)
                            else stringResource(R.string.add_to_favorites)
                        )
                    },
                    leadingIcon = {
                        Text(
                            text = if (isFavorite) "★" else "☆",
                            color = if (isFavorite) Color(0xFFFFD700) else Color.Unspecified
                        )
                    },
                    onClick = {
                        showMenu = false
                        scope.launch {
                            favoritesRepository.toggleFavorite(
                                eventGuid = event.guid,
                                isFavorite = isFavorite,
                                title = event.title,
                                thumbUrl = event.thumbUrl,
                                posterUrl = event.posterUrl,
                                conferenceTitle = event.conferenceTitle,
                                persons = event.persons?.joinToString(", "),
                                duration = event.duration
                            )
                        }
                    }
                )
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

            val favoritesRepository: FavoritesRepository = koinInject()
            val isFavorite by favoritesRepository.isFavorite(entry.eventGuid).collectAsState(initial = false)
            var showMenu by remember { mutableStateOf(false) }
            val scope = rememberCoroutineScope()

            Box {
                Card(
                    onClick = { onEventClick(entry.eventGuid) },
                    onLongClick = { showMenu = true },
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

                            // Favorite star badge
                            if (isFavorite) {
                                Text(
                                    text = "★",
                                    color = Color(0xFFFFD700),
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(6.dp)
                                )
                            }

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

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                if (isFavorite) stringResource(R.string.remove_from_favorites)
                                else stringResource(R.string.add_to_favorites)
                            )
                        },
                        leadingIcon = {
                            Text(
                                text = if (isFavorite) "★" else "☆",
                                color = if (isFavorite) Color(0xFFFFD700) else Color.Unspecified
                            )
                        },
                        onClick = {
                            showMenu = false
                            scope.launch {
                                favoritesRepository.toggleFavorite(
                                    eventGuid = entry.eventGuid,
                                    isFavorite = isFavorite,
                                    title = entry.title,
                                    thumbUrl = entry.thumbUrl,
                                    posterUrl = null,
                                    conferenceTitle = entry.conferenceTitle,
                                    persons = entry.persons,
                                    duration = entry.duration
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun LiveStreamRow(
    streams: List<LiveStreamItem>,
    onStreamClick: (LiveStreamItem) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 48.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(streams, key = { "${it.conferenceName}-${it.roomName}" }) { stream ->
            var isFocused by remember { mutableStateOf(false) }
            val scale by animateFloatAsState(if (isFocused) 1.05f else 1f)

            Card(
                onClick = { onStreamClick(stream) },
                modifier = Modifier
                    .width(280.dp)
                    .height(200.dp)
                    .scale(scale)
                    .onFocusChanged { isFocused = it.isFocused },
                colors = CardDefaults.colors(
                    containerColor = Color(0xFF2A2A4E),
                    focusedContainerColor = Color(0xFF3A3A5E)
                )
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = stream.thumbUrl,
                        contentDescription = stream.roomName,
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
                    // LIVE badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .background(Color.Red, MaterialTheme.shapes.small)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "🔴 LIVE",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = stream.roomName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = stream.conferenceName,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        stream.currentTalkTitle?.let { title ->
                            Text(
                                text = title,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.5f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}


@Preview(device = "id:tv_1080p", showBackground = true)
@Composable
private fun TvHomeScreenPreview() {
    val sampleEvents = listOf(
        Event(
            guid = "event-1",
            title = "Opening Keynote: Digital Freedom in a Connected World",
            slug = "opening-keynote",
            url = "https://example.com",
            conferenceTitle = "38C3",
            subtitle = "The future of digital rights",
            persons = listOf("Alice", "Bob"),
            duration = 3600
        ),
        Event(
            guid = "event-2",
            title = "Building Secure Systems",
            slug = "secure-systems",
            url = "https://example.com",
            conferenceTitle = "38C3",
            persons = listOf("Charlie"),
            duration = 2700
        ),
        Event(
            guid = "event-3",
            title = "Privacy by Design",
            slug = "privacy-design",
            url = "https://example.com",
            conferenceTitle = "Camp 2023",
            persons = listOf("Diana", "Eve"),
            duration = 1800
        ),
        Event(
            guid = "event-4",
            title = "Open Source Hardware",
            slug = "open-hardware",
            url = "https://example.com",
            conferenceTitle = "Camp 2023",
            persons = listOf("Frank"),
            duration = 2400
        )
    )
    val sampleConferences = listOf(
        Conference(
            acronym = "38c3",
            title = "38th Chaos Communication Congress",
            slug = "congress/38c3",
            url = "https://example.com"
        ),
        Conference(
            acronym = "camp2023",
            title = "Chaos Communication Camp 2023",
            slug = "events/camp2023",
            url = "https://example.com"
        )
    )
    PreviewKoinProvider {
        MaterialTheme {
            TvHomeScreenContent(
                isLoading = false,
                errorMessage = null,
                promotedEvents = sampleEvents.take(2),
                recentEvents = sampleEvents,
                conferences = sampleConferences,
                liveStreams = emptyList(),
                continueWatching = emptyList(),
                onEventClick = {},
                onConferenceClick = {},
                onHistoryEventClick = {},
                onLiveStreamClick = {}
            )
        }
    }
}

@Preview(device = "id:tv_1080p", showBackground = true)
@Composable
private fun TvHomeScreenLoadingPreview() {
    MaterialTheme {
        TvHomeScreenContent(
            isLoading = true,
            errorMessage = null,
            promotedEvents = emptyList(),
            recentEvents = emptyList(),
            conferences = emptyList(),
            liveStreams = emptyList(),
            continueWatching = emptyList(),
            onEventClick = {},
            onConferenceClick = {}
        )
    }
}

@Preview(device = "id:tv_1080p", showBackground = true)
@Composable
private fun TvHomeScreenErrorPreview() {
    MaterialTheme {
        TvHomeScreenContent(
            isLoading = false,
            errorMessage = "Network connection failed",
            promotedEvents = emptyList(),
            recentEvents = emptyList(),
            conferences = emptyList(),
            liveStreams = emptyList(),
            continueWatching = emptyList(),
            onEventClick = {},
            onConferenceClick = {}
        )
    }
}

/**
 * Provides a minimal Koin context for @Preview composables.
 * Registers a FavoritesRepository with a no-op DAO so sub-composables
 * that call koinInject<FavoritesRepository>() don't crash.
 */
@Composable
private fun PreviewKoinProvider(content: @Composable () -> Unit) {
    KoinApplication(application = {
        modules(
            org.koin.dsl.module {
                single { FavoritesRepository(PreviewFavoriteEventDao()) }
            }
        )
    }) {
        content()
    }
}

/** Stub DAO that returns empty flows; used only in @Preview. */
private class PreviewFavoriteEventDao : FavoriteEventDao {
    override suspend fun insert(entity: FavoriteEventEntity) {}
    override suspend fun delete(eventGuid: String) {}
    override fun getAll(): Flow<List<FavoriteEventEntity>> = flowOf(emptyList())
    override fun isFavorite(eventGuid: String): Flow<Boolean> = flowOf(false)
}
