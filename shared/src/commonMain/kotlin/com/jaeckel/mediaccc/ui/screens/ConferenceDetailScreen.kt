package com.jaeckel.mediaccc.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowOverflow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jaeckel.mediaccc.api.model.Event
import com.jaeckel.mediaccc.ui.components.EventCard
import com.jaeckel.mediaccc.viewmodel.ConferenceDetailViewModel
import mediaccc.shared.generated.resources.Res
import mediaccc.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ConferenceDetailScreen(
    acronym: String,
    onEventClick: (Event) -> Unit,
    onBackClick: () -> Unit
) {
    val viewModel: ConferenceDetailViewModel = koinViewModel(
        key = acronym,
        parameters = { parametersOf(acronym) }
    )
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.conference?.title ?: stringResource(Res.string.conference),
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
                            .clickable { onBackClick() }
                    )
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
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.errorMessage != null -> {
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
                uiState.conference != null -> {
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val isWide = maxWidth >= 600.dp
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 280.dp),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Conference description
                            uiState.conference!!.description?.takeIf { it.isNotBlank() }?.let { description ->
                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    Text(
                                        text = description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                }
                            }

                            // Tags
                            if (uiState.tagCounts.isNotEmpty()) {
                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    if (isWide) {
                                        FlowRow(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 8.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            uiState.tagCounts.forEach { (tag, count) ->
                                                FilterChip(
                                                    selected = uiState.selectedTag == tag,
                                                    onClick = { viewModel.selectTag(tag) },
                                                    label = { Text("$tag ($count)") }
                                                )
                                            }
                                        }
                                    } else {
                                        FlowRow(
                                            modifier = Modifier
                                                .padding(bottom = 8.dp)
                                                .horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp),
                                            maxLines = 2,
                                            overflow = FlowRowOverflow.Clip
                                        ) {
                                            uiState.tagCounts.forEach { (tag, count) ->
                                                FilterChip(
                                                    selected = uiState.selectedTag == tag,
                                                    onClick = { viewModel.selectTag(tag) },
                                                    label = { Text("$tag ($count)") }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Events count
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Text(
                                    text = stringResource(Res.string.events_count, uiState.filteredEvents.size),
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }

                            items(uiState.filteredEvents, key = { it.guid }) { event ->
                                EventCard(
                                    event = event,
                                    onClick = { onEventClick(event) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun ConferenceDetailPreview() {
    val sampleEvents = listOf(
        Event(
            guid = "evt-1",
            title = "Hacking the Planet",
            slug = "hacking-planet",
            url = "https://example.com",
            conferenceTitle = "38C3",
            tags = listOf("security", "hacking"),
            persons = listOf("Jane Hacker"),
            duration = 3600
        ),
        Event(
            guid = "evt-2",
            title = "Privacy in the Age of AI",
            slug = "privacy-ai",
            url = "https://example.com",
            conferenceTitle = "38C3",
            tags = listOf("privacy", "ai"),
            persons = listOf("John Privacy"),
            duration = 2700
        )
    )
    MaterialTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "A conference about hacking and digital rights.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp)
            )
            Text(
                text = "Events (2)",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 280.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sampleEvents, key = { it.guid }) { event ->
                    EventCard(event = event, onClick = {})
                }
            }
        }
    }
}
