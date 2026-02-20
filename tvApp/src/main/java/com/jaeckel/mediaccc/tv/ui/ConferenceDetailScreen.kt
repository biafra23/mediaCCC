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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import com.jaeckel.mediaccc.api.model.Event
import com.jaeckel.mediaccc.tv.ui.cards.EventCardWithOverlay
import com.jaeckel.mediaccc.viewmodel.ConferenceDetailViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.tv.material3.Surface
import androidx.tv.material3.SelectableSurfaceDefaults

@OptIn(ExperimentalTvMaterial3Api::class)
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
    
    val firstTagFocusRequester = remember { FocusRequester() }
    val firstResultFocusRequester = remember { FocusRequester() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A2E))
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )
            }
            uiState.errorMessage != null -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Error: ${uiState.errorMessage}",
                        color = Color.White
                    )
                }
            }
            uiState.conference != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(48.dp)
                ) {
                    // Conference header
                    Text(
                        text = uiState.conference!!.title,
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    uiState.conference!!.description?.let { description ->
                        if (description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.7f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Tags Row
                    if (uiState.tagCounts.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rowItemsIndexed(uiState.tagCounts) { index, (tag, count) ->
                                val isSelected = uiState.selectedTag == tag
                                Surface(
                                    selected = isSelected,
                                    onClick = { viewModel.selectTag(tag) },
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

                    Text(
                        text = "Events (${uiState.filteredEvents.size})",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Events grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        contentPadding = PaddingValues(bottom = 32.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        gridItemsIndexed(uiState.filteredEvents, key = { _, event -> event.guid }) { index, event ->
                            EventCardWithOverlay(
                                event = event,
                                onClick = { onEventClick(event) },
                                modifier = Modifier
                                    .then(if (index == 0) Modifier.focusRequester(firstResultFocusRequester) else Modifier)
                                    .focusProperties {
                                        if (uiState.tagCounts.isNotEmpty()) {
                                            up = firstTagFocusRequester
                                        }
                                        
                                        // Grid navigation logic
                                        if (index + 1 < uiState.filteredEvents.size) {
                                            // right = ... automatically handled by LazyVerticalGrid unless overridden
                                        }
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}



