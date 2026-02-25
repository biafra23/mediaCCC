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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jaeckel.mediaccc.api.model.Conference
import com.jaeckel.mediaccc.ui.components.ConferenceCard
import com.jaeckel.mediaccc.viewmodel.HomeViewModel
import mediaccc.shared.generated.resources.Res
import mediaccc.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ConferencesScreen(
    viewModel: HomeViewModel = koinViewModel(),
    onConferenceClick: (Conference) -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTag by remember { mutableStateOf<String?>(null) }

    val tagCounts by remember(uiState.conferences) {
        derivedStateOf {
            val counts = mutableMapOf<String, Int>()
            for (conf in uiState.conferences) {
                val tag = conf.slug.substringBefore("/", missingDelimiterValue = "")
                if (tag.isNotBlank()) counts[tag] = (counts[tag] ?: 0) + 1
            }
            counts.entries.sortedByDescending { it.value }.map { it.key to it.value }
        }
    }

    val filteredConferences by remember(uiState.conferences, selectedTag) {
        derivedStateOf {
            if (selectedTag == null) uiState.conferences
            else uiState.conferences.filter { it.slug.substringBefore("/", missingDelimiterValue = "") == selectedTag }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.conferences)) },
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
                uiState.isLoading && uiState.conferences.isEmpty() -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.conferences.isNotEmpty() -> {
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val isWide = maxWidth >= 600.dp
                        Column(modifier = Modifier.fillMaxSize()) {
                            if (tagCounts.isNotEmpty()) {
                                if (isWide) {
                                    FlowRow(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        tagCounts.forEach { (tag, count) ->
                                            FilterChip(
                                                selected = selectedTag == tag,
                                                onClick = { selectedTag = if (selectedTag == tag) null else tag },
                                                label = { Text("$tag ($count)") }
                                            )
                                        }
                                    }
                                } else {
                                    FlowRow(
                                        modifier = Modifier
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp),
                                        maxLines = 1,
                                        overflow = FlowRowOverflow.Clip
                                    ) {
                                        tagCounts.forEach { (tag, count) ->
                                            FilterChip(
                                                selected = selectedTag == tag,
                                                onClick = { selectedTag = if (selectedTag == tag) null else tag },
                                                label = { Text("$tag ($count)") }
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }

                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 150.dp),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(filteredConferences, key = { it.acronym }) { conference ->
                                    ConferenceCard(
                                        conference = conference,
                                        onClick = { onConferenceClick(conference) }
                                    )
                                }
                            }
                        }
                    }
                }
                else -> {
                    Text(
                        text = stringResource(Res.string.no_conferences_available),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}
