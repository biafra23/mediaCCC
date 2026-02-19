package com.jaeckel.mediaccc.tv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import com.jaeckel.mediaccc.api.model.Conference
import com.jaeckel.mediaccc.tv.ui.cards.ConferenceCard
import com.jaeckel.mediaccc.viewmodel.HomeViewModel
import org.koin.compose.viewmodel.koinViewModel

import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.remember
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
    val focusRequesters = remember(uiState.conferences) {
        List(uiState.conferences.size) { FocusRequester() }
    }

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

                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    contentPadding = PaddingValues(bottom = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(uiState.conferences, key = { _, it -> it.acronym }) { index, conference ->
                        ConferenceCard(
                            conference = conference,
                            onClick = { onConferenceClick(conference) },
                            modifier = Modifier
                                .fillMaxSize()
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
