package com.jaeckel.mediaccc.tv.ui

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
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import com.jaeckel.mediaccc.api.model.Event
import com.jaeckel.mediaccc.tv.ui.cards.EventCard
import com.jaeckel.mediaccc.viewmodel.SearchViewModel
import org.koin.compose.viewmodel.koinViewModel

import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.runtime.remember

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = koinViewModel(),
    onEventClick: (Event) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusRequesters = remember(uiState.searchResults) {
        List(uiState.searchResults.size) { FocusRequester() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 48.dp, vertical = 32.dp)
    ) {
        Text(
            text = "Search Events",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = uiState.query,
            onValueChange = { viewModel.onQueryChanged(it) },
            modifier = Modifier
                .fillMaxWidth()
                .focusProperties {
                    if (focusRequesters.isNotEmpty()) {
                        down = focusRequesters[0]
                    }
                },
            placeholder = { Text("Enter at least 3 characters...", color = Color.Gray) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.Gray,
                cursorColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Box(modifier = Modifier.fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )
            } else if (uiState.errorMessage != null) {
                Text(
                    text = "Error: ${uiState.errorMessage}",
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (uiState.searchResults.isEmpty() && uiState.query.length >= 3) {
                Text(
                    text = "No events found.",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    contentPadding = PaddingValues(bottom = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(uiState.searchResults, key = { _, event -> event.guid }) { index, event ->
                        EventCard(
                            event = event,
                            onClick = { onEventClick(event) },
                            modifier = Modifier
                                .focusRequester(focusRequesters[index])
                                .focusProperties {
                                    if (index + 1 < focusRequesters.size) {
                                        right = focusRequesters[index + 1]
                                    }
                                    // Only override left if NOT at the start of a row (4 columns)
                                    if (index % 4 != 0) {
                                        left = focusRequesters[index - 1]
                                    }
                                }
                        )
                    }
                }
            }
        }
    }
}
