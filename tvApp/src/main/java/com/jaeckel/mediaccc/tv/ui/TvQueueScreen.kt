package com.jaeckel.mediaccc.tv.ui

import android.view.KeyEvent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.jaeckel.mediaccc.data.db.QueueEventEntity
import com.jaeckel.mediaccc.tv.R
import com.jaeckel.mediaccc.viewmodel.QueueViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TvQueueScreen(
    viewModel: QueueViewModel = koinViewModel(),
    onEventClick: (String) -> Unit = {}
) {
    val queueItems by viewModel.queueItems.collectAsState()
    val currentEventGuid by viewModel.currentEventGuid.collectAsState()

    val items = remember { mutableStateListOf<QueueEventEntity>() }
    var reorderingGuid by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(queueItems) {
        if (reorderingGuid == null) {
            items.clear()
            items.addAll(queueItems)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A2E))
            // Intercept key events at screen level so focus location doesn't matter
            .onPreviewKeyEvent { event ->
                val guid = reorderingGuid ?: return@onPreviewKeyEvent false
                val index = items.indexOfFirst { it.eventGuid == guid }
                if (index == -1) return@onPreviewKeyEvent false

                when (event.nativeKeyEvent.keyCode) {
                    KeyEvent.KEYCODE_DPAD_UP -> {
                        if (event.type == KeyEventType.KeyDown && index > 0)
                            items.move(index, index - 1)
                        true
                    }
                    KeyEvent.KEYCODE_DPAD_DOWN -> {
                        if (event.type == KeyEventType.KeyDown && index < items.size - 1)
                            items.move(index, index + 1)
                        true
                    }
                    KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                        // Only confirm on a fresh press (repeatCount == 0).
                        // Repeated KeyDown events fired while still holding after long-press
                        // would otherwise immediately deselect the item.
                        if (event.type == KeyEventType.KeyDown && event.nativeKeyEvent.repeatCount == 0) {
                            reorderingGuid = null
                            viewModel.reorderQueue(items.toList())
                        }
                        true
                    }
                    KeyEvent.KEYCODE_BACK -> {
                        if (event.type == KeyEventType.KeyDown) {
                            reorderingGuid = null
                            items.clear()
                            items.addAll(queueItems)
                        }
                        true
                    }
                    else -> false
                }
            }
    ) {
        if (items.isEmpty()) {
            Text(
                text = stringResource(R.string.no_queue_yet),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 32.dp, top = 32.dp, bottom = 8.dp, end = 32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.queue),
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    if (reorderingGuid != null) {
                        Text(
                            text = stringResource(R.string.queue_reorder_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF9A80D8)
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(items, key = { _, item -> item.eventGuid }) { index, item ->
                        TvQueueItem(
                            item = item,
                            index = index,
                            totalCount = items.size,
                            isReordering = reorderingGuid == item.eventGuid,
                            isCurrent = item.eventGuid == currentEventGuid,
                            onClick = {
                                viewModel.setCurrentEventGuid(item.eventGuid)
                                onEventClick(item.eventGuid)
                            },
                            onLongClick = {
                                reorderingGuid = if (reorderingGuid == item.eventGuid) null
                                               else item.eventGuid
                            },
                            onRemove = { viewModel.removeFromQueue(item.eventGuid) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun TvQueueItem(
    item: QueueEventEntity,
    index: Int,
    totalCount: Int,
    isReordering: Boolean,
    isCurrent: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onRemove: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isFocused || isReordering) 1.02f else 1f,
        label = "scale"
    )
    val containerColor by animateColorAsState(
        targetValue = when {
            isReordering -> Color(0xFF4A3A7E)
            isCurrent -> Color(0xFF2E3A5E)
            isFocused -> Color(0xFF3A3A5E)
            else -> Color(0xFF2A2A3E)
        },
        label = "containerColor"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Move arrows shown while reordering
        if (isReordering) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Text(
                    text = if (index > 0) "▲" else " ",
                    color = Color.White.copy(alpha = if (index > 0) 1f else 0f),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = if (index < totalCount - 1) "▼" else " ",
                    color = Color.White.copy(alpha = if (index < totalCount - 1) 1f else 0f),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Card(
            onClick = onClick,
            onLongClick = onLongClick,
            modifier = Modifier
                .weight(1f)
                .height(90.dp)
                .scale(scale)
                .onFocusChanged { isFocused = it.hasFocus },
            colors = CardDefaults.colors(
                containerColor = containerColor,
                focusedContainerColor = if (isReordering) Color(0xFF5A4A8E) else Color(0xFF3A3A5E)
            ),
            shape = CardDefaults.shape(RoundedCornerShape(12.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Thumbnail with duration badge
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(vertical = 8.dp)
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    AsyncImage(
                        model = item.thumbUrl ?: item.posterUrl,
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    item.duration?.let { duration ->
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(3.dp)
                                .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(3.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${duration / 60} min",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                    }
                }

                // Title and conference
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    if (isCurrent) {
                        Text(
                            text = stringResource(R.string.queue_now_playing),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF9A80D8)
                        )
                    }
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    item.conferenceTitle?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Position indicator while reordering
                if (isReordering) {
                    Text(
                        text = "${index + 1} / $totalCount",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF9A80D8)
                    )
                }
            }
        }

        // Remove button — hidden while reordering
        if (!isReordering) {
            Button(
                onClick = onRemove,
                modifier = Modifier.size(56.dp),
                colors = ButtonDefaults.colors(
                    containerColor = Color(0xFF3A2A2A),
                    focusedContainerColor = Color(0xFF6A3A3A)
                )
            ) {
                Text(
                    text = stringResource(R.string.queue_remove),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
            }
        }
    }
}

private fun <T> MutableList<T>.move(from: Int, to: Int) {
    if (from == to) return
    val item = removeAt(from)
    add(to, item)
}
