package com.jaeckel.mediaccc

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.jaeckel.mediaccc.api.MediaCCCApi
import com.jaeckel.mediaccc.api.model.Event
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource

import mediaccc.shared.generated.resources.Res
import mediaccc.shared.generated.resources.compose_multiplatform

@Composable
@Preview
fun App() {
    MaterialTheme {
        var showContent by remember { mutableStateOf(false) }

        val api = remember { MediaCCCApi() }
        val repository = remember { MediaRepository(api) }
        var recentEvents by remember { mutableStateOf<List<Event>>(emptyList()) }
        var statusMessage by remember { mutableStateOf("Waiting...") }

        // Define a simple format: "DD.MM.YYYY hh:mm"
        val dateTimeFormat = remember {
            LocalDateTime.Format {
                dayOfMonth()
                char('.')
                monthNumber()
                char('.')
                year()
                char(' ')
                hour()
                char(':')
                minute()
            }
        }

        LaunchedEffect(Unit) {
            statusMessage = "Loading..."
            repository.getPopularEvents(2025).collect { result ->
                result.fold(
                    onSuccess = { response ->
                        recentEvents = response.events
                        statusMessage = "Loaded ${response.events.size} events"
                    },
                    onFailure = { error ->
                        statusMessage = "Error: ${error.message}"
                    }
                )
            }
        }

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(onClick = { showContent = !showContent }) {
                Text("Click me!")
            }
            AnimatedVisibility(showContent) {
                val greeting = remember { Greeting().greet() }
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(painterResource(Res.drawable.compose_multiplatform), null)
                    Text("Compose: $greeting")
                }
            }

            Text(text = statusMessage)

            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                items(recentEvents) { event ->
                    Text(text = event.title, modifier = Modifier.background(MaterialTheme.colorScheme.surface))

                    // Convert Instant to LocalDateTime in System TimeZone, then format
                    val formattedDate = event.date?.toLocalDateTime(TimeZone.currentSystemDefault())
                        ?.format(dateTimeFormat) ?: "No Date"

                    Text(text = formattedDate, modifier = Modifier.background(MaterialTheme.colorScheme.surface))
                }
            }
        }
    }
}