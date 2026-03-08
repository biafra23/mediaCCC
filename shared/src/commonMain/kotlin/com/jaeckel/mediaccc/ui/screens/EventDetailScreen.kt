package com.jaeckel.mediaccc.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.jaeckel.mediaccc.api.model.Event
import com.jaeckel.mediaccc.api.model.Recording
import com.jaeckel.mediaccc.ui.util.PipState
import com.jaeckel.mediaccc.ui.util.SystemAppearance
import com.jaeckel.mediaccc.viewmodel.EventDetailUiState
import com.jaeckel.mediaccc.viewmodel.EventDetailViewModel
import io.github.kdroidfilter.composemediaplayer.VideoPlayerSurface
import io.github.kdroidfilter.composemediaplayer.VideoPlayerState
import io.github.kdroidfilter.composemediaplayer.rememberVideoPlayerState
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import mediaccc.shared.generated.resources.Res
import mediaccc.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import com.jaeckel.mediaccc.ui.util.MultiplatformPreview
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventGuid: String,
    onPlayClick: (videoUrl: String, title: String, speakers: String, date: String, conference: String) -> Unit,
    onBackClick: () -> Unit,
    /**
     * Optional platform-specific actions (e.g. a Cast button) rendered in the TopAppBar.
     * Receives the current recording URL and MIME type so callers can act on the media being displayed.
     */
    extraTopBarActions: @Composable (recordingUrl: String?, mimeType: String?, title: String?) -> Unit = { _, _, _ -> }
) {
    val viewModel: EventDetailViewModel = koinViewModel(
        key = eventGuid,
        parameters = { parametersOf(eventGuid) }
    )
    val uiState by viewModel.uiState.collectAsState()

    var isPlaying by rememberSaveable { mutableStateOf(false) }
    var hasRestoredPosition by rememberSaveable { mutableStateOf(false) }
    val playerState = rememberVideoPlayerState()

    SystemAppearance(playerState.isFullscreen)

    // Track playing state for PiP
    LaunchedEffect(isPlaying, playerState.isPlaying) {
        PipState.setPlaying(isPlaying && playerState.isPlaying)
    }
    DisposableEffect(Unit) {
        onDispose { PipState.setPlaying(false) }
    }

    val recordingUrl = uiState.bestRecording?.recordingUrl
    val mimeType = uiState.bestRecording?.mimeType
    val savedSliderPos = uiState.savedSliderPos

    // Open URI and seek to saved position in one coroutine to guarantee ordering
    LaunchedEffect(recordingUrl, isPlaying) {
        if (isPlaying && recordingUrl != null) {
            playerState.openUri(recordingUrl)
            // Wait for the player to actually start playing
            while (!playerState.isPlaying) {
                delay(100)
            }
            // Now seek to saved position if needed
            if (!hasRestoredPosition && savedSliderPos > 5f) {
                hasRestoredPosition = true
                // Retry seek — some players need repeated attempts after buffering
                repeat(6) {
                    delay(500)
                    playerState.seekTo(savedSliderPos)
                }
            }
        }
    }

    // As soon as play starts: write the initial history entry, then refresh position every 5 s.
    // Wait a bit before first auto-save to avoid overwriting restored position.
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            viewModel.saveProgress(savedSliderPos)
            delay(5_000)
            while (true) {
                viewModel.saveProgress(playerState.sliderPos)
                delay(5_000)
            }
        }
    }

    val dateTimeFormat = remember {
        LocalDateTime.Format {
            day()
            char('.')
            monthNumber()
            char('.')
            year()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.event?.title ?: stringResource(Res.string.event),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    Text(
                        text = "←",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .clickable {
                                if (isPlaying) {
                                    viewModel.saveProgress(playerState.sliderPos)
                                }
                                onBackClick()
                            }
                    )
                },
                actions = {
                    if (uiState.event != null) {
                        var showMenu by remember { mutableStateOf(false) }
                        Box {
                            Text(
                                text = "⋮",
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .clickable { showMenu = true }
                            )
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                if (uiState.isInQueue) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(Res.string.remove_from_queue)) },
                                        onClick = {
                                            showMenu = false
                                            viewModel.removeFromQueue()
                                        }
                                    )
                                } else {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(Res.string.add_to_queue_start)) },
                                        onClick = {
                                            showMenu = false
                                            viewModel.addToQueueStart()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(Res.string.add_to_queue_end)) },
                                        onClick = {
                                            showMenu = false
                                            viewModel.addToQueueEnd()
                                        }
                                    )
                                }
                            }
                        }

                        Text(
                            text = if (uiState.isFavorite) "★" else "☆",
                            style = MaterialTheme.typography.headlineSmall,
                            color = if (uiState.isFavorite) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .padding(horizontal = 12.dp)
                                .clickable { viewModel.toggleFavorite() }
                        )
                    }
                    extraTopBarActions(recordingUrl, mimeType, uiState.event?.title)
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
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                uiState.errorMessage != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(Res.string.error_message, uiState.errorMessage ?: ""),                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onBackClick) { Text(stringResource(Res.string.go_back)) }
                    }
                }

                uiState.event != null -> {
                    val event = uiState.event!!
                    EventDetailContent(
                        event = event,
                        uiState = uiState,
                        playerState = playerState,
                        isPlaying = isPlaying,
                        recordingUrl = recordingUrl,
                        savedSliderPos = savedSliderPos,
                        dateTimeFormat = dateTimeFormat,
                        onPlayClick = { isPlaying = true },
                        onExitFullscreen = {
                            viewModel.saveProgress(playerState.sliderPos)
                            playerState.toggleFullscreen()
                            isPlaying = false
                            playerState.pause()
                        },
                        onLanguageSelected = { viewModel.selectLanguage(it) },
                        onToggleFavorite = { viewModel.toggleFavorite() }
                    )
                }
            }
        }
    }
}

@Composable
private fun EventDetailContent(
    event: Event,
    uiState: EventDetailUiState,
    playerState: VideoPlayerState,
    isPlaying: Boolean,
    recordingUrl: String?,
    savedSliderPos: Float,
    dateTimeFormat: DateTimeFormat<LocalDateTime>,
    onPlayClick: () -> Unit,
    onExitFullscreen: () -> Unit,
    onLanguageSelected: (String) -> Unit,
    onToggleFavorite: () -> Unit = {}
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWide = maxWidth >= 600.dp

        // In narrow/portrait mode, play also enters fullscreen
        val effectivePlayClick: () -> Unit = if (isWide) {
            onPlayClick
        } else {
            { onPlayClick(); playerState.toggleFullscreen() }
        }

        if (isWide) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    EventVideoPane(
                        event = event,
                        uiState = uiState,
                        playerState = playerState,
                        isPlaying = isPlaying,
                        recordingUrl = recordingUrl,
                        dateTimeFormat = dateTimeFormat,
                        onPlayClick = effectivePlayClick,
                        onExitFullscreen = onExitFullscreen,
                        onToggleFavorite = onToggleFavorite,
                        onEnterFullscreen = { playerState.toggleFullscreen() },
                        showInlineControls = true,
                        modifier = Modifier
                            .weight(0.5f)
                            .aspectRatio(16f / 9f)
                    )
                    EventDescriptionSide(
                        event = event,
                        modifier = Modifier
                            .weight(0.5f)
                            .padding(16.dp)
                    )
                }
                EventMetadataPane(
                    event = event,
                    uiState = uiState,
                    isPlaying = isPlaying,
                    savedSliderPos = savedSliderPos,
                    dateTimeFormat = dateTimeFormat,
                    onPlayClick = effectivePlayClick,
                    onLanguageSelected = onLanguageSelected,
                    showDescription = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                EventVideoPane(
                    event = event,
                    uiState = uiState,
                    playerState = playerState,
                    isPlaying = isPlaying,
                    recordingUrl = recordingUrl,
                    dateTimeFormat = dateTimeFormat,
                    onPlayClick = effectivePlayClick,
                    onExitFullscreen = onExitFullscreen,
                    onToggleFavorite = onToggleFavorite,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                )
                EventMetadataPane(
                    event = event,
                    uiState = uiState,
                    isPlaying = isPlaying,
                    savedSliderPos = savedSliderPos,
                    dateTimeFormat = dateTimeFormat,
                    onPlayClick = effectivePlayClick,
                    onLanguageSelected = onLanguageSelected,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun EventVideoPane(
    event: Event,
    uiState: EventDetailUiState,
    playerState: VideoPlayerState,
    isPlaying: Boolean,
    recordingUrl: String?,
    dateTimeFormat: DateTimeFormat<LocalDateTime>,
    onPlayClick: () -> Unit,
    onExitFullscreen: () -> Unit,
    onToggleFavorite: () -> Unit,
    onEnterFullscreen: (() -> Unit)? = null,
    showInlineControls: Boolean = false,
    modifier: Modifier = Modifier
) {
    val formattedDate = event.date?.let {
        dateTimeFormat.format(it.toLocalDateTime(TimeZone.currentSystemDefault()))
    } ?: ""
    val speakers = event.persons?.joinToString(", ") ?: ""

    Box(modifier = modifier) {
        VideoPlayerSurface(
            playerState = playerState,
            modifier = Modifier.fillMaxSize(),
            overlay = {
                if (playerState.isFullscreen) {
                    PlayerControlsOverlay(
                        playerState = playerState,
                        title = event.title,
                        speakers = speakers,
                        conference = event.conferenceTitle ?: "",
                        date = formattedDate,
                        isFavorite = uiState.isFavorite,
                        isInline = false,
                        onExitFullscreen = onExitFullscreen,
                        onToggleFavorite = onToggleFavorite
                    )
                } else if (showInlineControls && isPlaying) {
                    PlayerControlsOverlay(
                        playerState = playerState,
                        title = event.title,
                        speakers = speakers,
                        conference = event.conferenceTitle ?: "",
                        date = formattedDate,
                        isFavorite = uiState.isFavorite,
                        isInline = true,
                        onExitFullscreen = onExitFullscreen,
                        onEnterFullscreen = onEnterFullscreen,
                        onToggleFavorite = onToggleFavorite
                    )
                }
            }
        )

        if (!isPlaying || recordingUrl == null) {
            AsyncImage(
                model = event.posterUrl ?: event.thumbUrl,
                contentDescription = event.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            if (uiState.bestRecording != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.9f))
                        .clickable { onPlayClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "▶",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        if (isPlaying && !playerState.isFullscreen && onEnterFullscreen != null && !showInlineControls) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable { onEnterFullscreen() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⛶",
                    color = Color.White,
                    fontSize = 20.sp
                )
            }
        }
    }
}

@Composable
private fun EventMetadataPane(
    event: Event,
    uiState: EventDetailUiState,
    isPlaying: Boolean,
    savedSliderPos: Float,
    dateTimeFormat: DateTimeFormat<LocalDateTime>,
    onPlayClick: () -> Unit,
    onLanguageSelected: (String) -> Unit,
    showDescription: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = event.title,
            style = MaterialTheme.typography.headlineSmall
        )

        event.subtitle?.let { subtitle ->
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        event.conferenceTitle?.let { MetaInfoRow(label = "📅", text = it) }

        event.date?.let { date ->
            val localDateTime = date.toLocalDateTime(TimeZone.currentSystemDefault())
            MetaInfoRow(label = "🗓", text = dateTimeFormat.format(localDateTime))
        }

        event.persons?.let { persons ->
            if (persons.isNotEmpty()) MetaInfoRow(label = "👤", text = persons.joinToString(", "))
        }

        event.duration?.let { duration ->
            MetaInfoRow(label = "⏱", text = "${duration / 60} min")
        }

        // Language selector
        if (uiState.availableLanguages.size > 1) {
            var languageMenuExpanded by remember { mutableStateOf(false) }
            Box {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .clickable { languageMenuExpanded = true }
                ) {
                    Text(text = "🌐", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${stringResource(Res.string.language)}: ${uiState.selectedLanguage?.uppercase() ?: ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = " ▾",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                DropdownMenu(
                    expanded = languageMenuExpanded,
                    onDismissRequest = { languageMenuExpanded = false }
                ) {
                    uiState.availableLanguages.forEach { lang ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = lang.uppercase(),
                                    color = if (lang == uiState.selectedLanguage)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurface
                                )
                            },
                            onClick = {
                                onLanguageSelected(lang)
                                languageMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.bestRecording != null && !isPlaying) {
            Button(
                onClick = onPlayClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (savedSliderPos > 5f) stringResource(Res.string.resume) else stringResource(Res.string.play))
            }
        }

        if (showDescription) {
            Spacer(modifier = Modifier.height(24.dp))

            event.description?.let { description ->
                if (description.isNotBlank()) {
                    Text(
                        text = stringResource(Res.string.description),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@MultiplatformPreview
@Composable
private fun EventDetailContentPreview() {
    val sampleEvent = Event(
        guid = "preview-guid",
        title = "The Future of Open Source Security",
        subtitle = "How communities protect critical infrastructure",
        slug = "future-open-source-security",
        url = "https://api.media.ccc.de/public/events/preview",
        conferenceTitle = "38th Chaos Communication Congress",
        persons = listOf("Alice Example", "Bob Speaker"),
        duration = 3600,
        description = "In this talk we explore the challenges and opportunities facing open source security in an increasingly connected world. We discuss supply chain attacks, reproducible builds, and community-driven security audits that help protect the software everyone depends on."
    )
    val sampleRecording = Recording(
        size = 500,
        length = 3600,
        mimeType = "video/mp4",
        language = "eng",
        filename = "sample.mp4",
        state = "new",
        folder = "",
        highQuality = true,
        width = 1920,
        height = 1080,
        recordingUrl = "https://example.com/video.mp4",
        url = "https://example.com/video.mp4",
        eventUrl = "https://example.com/event",
        conferenceUrl = "https://example.com/conference"
    )
    val uiState = EventDetailUiState(
        isLoading = false,
        event = sampleEvent,
        bestRecording = sampleRecording,
        availableLanguages = listOf("eng", "deu"),
        selectedLanguage = "eng"
    )
    val dateTimeFormat = remember {
        LocalDateTime.Format {
            day()
            char('.')
            monthNumber()
            char('.')
            year()
        }
    }
    MaterialTheme {
        EventDetailContent(
            event = sampleEvent,
            uiState = uiState,
            playerState = rememberVideoPlayerState(),
            isPlaying = false,
            recordingUrl = sampleRecording.recordingUrl,
            savedSliderPos = 0f,
            dateTimeFormat = dateTimeFormat,
            onPlayClick = {},
            onExitFullscreen = {},
            onLanguageSelected = {}
        )
    }
}

@Composable
private fun EventDescriptionSide(
    event: Event,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        event.description?.let { description ->
            if (description.isNotBlank()) {
                Text(
                    text = stringResource(Res.string.description),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MetaInfoRow(label: String, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PlayerControlsOverlay(
    playerState: VideoPlayerState,
    title: String,
    speakers: String,
    conference: String,
    date: String,
    isFavorite: Boolean = false,
    isInline: Boolean = false,
    onExitFullscreen: () -> Unit,
    onEnterFullscreen: (() -> Unit)? = null,
    onToggleFavorite: () -> Unit = {}
) {
    var showControls by remember { mutableStateOf(true) }
    var interactionCount by remember { mutableStateOf(0) }

    LaunchedEffect(showControls, interactionCount) {
        if (showControls) {
            delay(8_000)
            showControls = false
        }
    }

    fun onInteract() { interactionCount++ }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                showControls = !showControls
                if (showControls) onInteract()
            }
    ) {
        if (showControls) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(8.dp)
                    .align(Alignment.TopStart),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isInline && onEnterFullscreen != null) {
                    Text(
                        text = "⛶",
                        color = Color.White,
                        fontSize = 24.sp,
                        modifier = Modifier
                            .clickable {
                                onInteract()
                                onEnterFullscreen()
                            }
                            .padding(8.dp)
                    )
                } else {
                    Text(
                        text = "✕",
                        color = Color.White,
                        fontSize = 24.sp,
                        modifier = Modifier
                            .clickable {
                                onInteract()
                                onExitFullscreen()
                            }
                            .padding(8.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .weight(1f)
                ) {
                    Text(
                        text = title,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    val metaText = buildString {
                        if (speakers.isNotBlank()) append(speakers)
                        val parts = listOfNotNull(
                            conference.takeIf { it.isNotBlank() },
                            date.takeIf { it.isNotBlank() }
                        )
                        if (parts.isNotEmpty()) {
                            if (isNotEmpty()) append(" • ")
                            append(parts.joinToString(" • "))
                        }
                    }

                    if (metaText.isNotBlank()) {
                        Text(
                            text = metaText,
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Text(
                    text = if (isFavorite) "★" else "☆",
                    color = if (isFavorite) Color(0xFFFFD700) else Color.White,
                    fontSize = 24.sp,
                    modifier = Modifier
                        .clickable {
                            onInteract()
                            onToggleFavorite()
                        }
                        .padding(8.dp)
                )
            }

            val isPlaying = playerState.isPlaying
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.9f))
                    .clickable {
                        onInteract()
                        if (isPlaying) playerState.pause() else playerState.play()
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isPlaying) {
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Box(
                            modifier = Modifier
                                .width(7.dp)
                                .height(26.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(MaterialTheme.colorScheme.onPrimary)
                        )
                        Box(
                            modifier = Modifier
                                .width(7.dp)
                                .height(26.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(MaterialTheme.colorScheme.onPrimary)
                        )
                    }
                } else {
                    Text(
                        text = "▶",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .align(Alignment.BottomCenter)
            ) {
                Slider(
                    value = playerState.sliderPos,
                    onValueChange = {
                        onInteract()
                        playerState.sliderPos = it
                        playerState.userDragging = true
                    },
                    onValueChangeFinished = {
                        onInteract()
                        playerState.userDragging = false
                        playerState.seekTo(playerState.sliderPos)
                    },
                    valueRange = 0f..1000f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.White,
                        inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = playerState.positionText, color = Color.White, fontSize = 12.sp)
                    Text(
                        text = "-${computeRemainingTime(playerState.positionText, playerState.durationText)}",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

private fun parseTimeToSeconds(text: String): Int {
    val parts = text.split(":")
    return when (parts.size) {
        3 -> (parts[0].toIntOrNull() ?: 0) * 3600 + (parts[1].toIntOrNull() ?: 0) * 60 + (parts[2].toIntOrNull() ?: 0)
        2 -> (parts[0].toIntOrNull() ?: 0) * 60 + (parts[1].toIntOrNull() ?: 0)
        else -> 0
    }
}

private fun formatSeconds(totalSeconds: Int): String {
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    val pad = { v: Int -> v.toString().padStart(2, '0') }
    return if (h > 0) "$h:${pad(m)}:${pad(s)}" else "$m:${pad(s)}"
}

private fun computeRemainingTime(positionText: String, durationText: String): String {
    val position = parseTimeToSeconds(positionText)
    val duration = parseTimeToSeconds(durationText)
    val remaining = (duration - position).coerceAtLeast(0)
    return formatSeconds(remaining)
}
