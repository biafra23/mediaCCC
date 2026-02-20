package com.jaeckel.mediaccc.tv.ui

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed as gridItemsIndexed
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed as rowItemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import androidx.tv.material3.SelectableSurfaceDefaults
import androidx.tv.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import com.jaeckel.mediaccc.api.model.Conference
import com.jaeckel.mediaccc.tv.ui.cards.ConferenceCard
import com.jaeckel.mediaccc.viewmodel.HomeViewModel
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ConferencesScreen(
    viewModel: HomeViewModel = koinViewModel(),
    onConferenceClick: (Conference) -> Unit
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

    val firstTagFocusRequester = remember { FocusRequester() }
    val firstResultFocusRequester = remember { FocusRequester() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A2E))
    ) {
        if (uiState.isLoading && uiState.conferences.isEmpty()) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.White
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 48.dp, vertical = 32.dp)
            ) {
                Text(
                    text = "Conferences",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Tags Row
                if (tagCounts.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        rowItemsIndexed(tagCounts) { index, (tag, count) ->
                            val isSelected = selectedTag == tag
                            Surface(
                                selected = isSelected,
                                onClick = { selectedTag = if (isSelected) null else tag },
                                modifier = Modifier
                                    .then(if (index == 0) Modifier.focusRequester(firstTagFocusRequester) else Modifier)
                                    .focusProperties {
                                        down = firstResultFocusRequester
                                    },
                                shape = SelectableSurfaceDefaults.shape(RoundedCornerShape(16.dp)),
                                colors = SelectableSurfaceDefaults.colors(
                                    containerColor = Color.White.copy(alpha = 0.1f),
                                    contentColor = Color.White.copy(alpha = 0.8f),
                                    focusedContainerColor = Color.White,
                                    focusedContentColor = Color.Black,
                                    selectedContainerColor = Color(0xFF6366F1),
                                    selectedContentColor = Color.White,
                                    focusedSelectedContainerColor = Color.White,
                                    focusedSelectedContentColor = Color.Black
                                )
                            ) {
                                Text(
                                    text = "$tag ($count)",
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    contentPadding = PaddingValues(bottom = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    gridItemsIndexed(filteredConferences, key = { _, it -> it.acronym }) { index, conference ->
                        ConferenceCard(
                            conference = conference,
                            onClick = { onConferenceClick(conference) },
                            modifier = Modifier
                                .fillMaxSize()
                                .then(if (index == 0) Modifier.focusRequester(firstResultFocusRequester) else Modifier)
                                .focusProperties {
                                    if (tagCounts.isNotEmpty()) {
                                        up = firstTagFocusRequester
                                    }
                                }
                        )
                    }
                }
            }
        }
    }
}
