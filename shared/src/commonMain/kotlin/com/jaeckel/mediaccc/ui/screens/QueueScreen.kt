package com.jaeckel.mediaccc.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import com.jaeckel.mediaccc.data.db.QueueEventEntity
import com.jaeckel.mediaccc.viewmodel.QueueViewModel
import mediaccc.shared.generated.resources.Res
import mediaccc.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import com.jaeckel.mediaccc.ui.util.MultiplatformPreview
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun QueueScreen(
    viewModel: QueueViewModel = koinViewModel(),
    onEventClick: (videoUrl: String, title: String, speakers: String, date: String, conference: String, eventGuid: String) -> Unit = { _, _, _, _, _, _ -> },
    onBackClick: () -> Unit
) {
    val queueItems by viewModel.queueItems.collectAsState()
    val currentEventGuid by viewModel.currentEventGuid.collectAsState()

    // Local mutable copy for drag reordering — synced from DB when not dragging
    val items = remember { mutableStateListOf<QueueEventEntity>() }
    var isDragging by remember { mutableStateOf(false) }

    LaunchedEffect(queueItems) {
        if (!isDragging) {
            items.clear()
            items.addAll(queueItems)
        }
    }

    val lazyListState = rememberLazyListState()
    var draggedItemIndex by remember { mutableStateOf<Int?>(null) }
    var dragDelta by remember { mutableStateOf(0f) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.queue)) },
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
        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(Res.string.no_queue_yet),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                itemsIndexed(items, key = { _, item -> item.eventGuid }) { index, item ->
                    val isBeingDragged = index == draggedItemIndex

                    QueueItemRow(
                        item = item,
                        isCurrent = item.eventGuid == currentEventGuid,
                        modifier = Modifier
                            .zIndex(if (isBeingDragged) 1f else 0f)
                            .graphicsLayer {
                                translationY = if (isBeingDragged) dragDelta else 0f
                                shadowElevation = if (isBeingDragged) 8.dp.toPx() else 0f
                                scaleX = if (isBeingDragged) 1.02f else 1f
                                scaleY = if (isBeingDragged) 1.02f else 1f
                            },
                        // Key pointerInput by stable eventGuid so drag continues through reorders
                        dragHandleModifier = Modifier.pointerInput(item.eventGuid) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = {
                                    isDragging = true
                                    draggedItemIndex = items.indexOfFirst { it.eventGuid == item.eventGuid }
                                    dragDelta = 0f
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragDelta += dragAmount.y

                                    val currentIndex = draggedItemIndex
                                        ?: return@detectDragGesturesAfterLongPress
                                    val layoutInfo = lazyListState.layoutInfo
                                    val draggedItemInfo = layoutInfo.visibleItemsInfo
                                        .firstOrNull { it.index == currentIndex }
                                        ?: return@detectDragGesturesAfterLongPress

                                    val draggedCenter = draggedItemInfo.offset.toFloat() +
                                        dragDelta + draggedItemInfo.size / 2f

                                    val targetItemInfo = layoutInfo.visibleItemsInfo
                                        .firstOrNull { itemInfo ->
                                            itemInfo.index != currentIndex &&
                                                draggedCenter > itemInfo.offset &&
                                                draggedCenter < itemInfo.offset + itemInfo.size
                                        } ?: return@detectDragGesturesAfterLongPress

                                    // Adjust delta so card stays at same visual position after list swap
                                    dragDelta -= (targetItemInfo.offset - draggedItemInfo.offset).toFloat()
                                    items.move(currentIndex, targetItemInfo.index)
                                    draggedItemIndex = targetItemInfo.index
                                },
                                onDragEnd = {
                                    draggedItemIndex = null
                                    dragDelta = 0f
                                    isDragging = false
                                    viewModel.reorderQueue(items.toList())
                                },
                                onDragCancel = {
                                    draggedItemIndex = null
                                    dragDelta = 0f
                                    isDragging = false
                                }
                            )
                        },
                        onClick = {
                            viewModel.setCurrentEventGuid(item.eventGuid)
                            onEventClick(
                                "", item.title, item.persons ?: "",
                                "", item.conferenceTitle ?: "", item.eventGuid
                            )
                        },
                        onRemove = { viewModel.removeFromQueue(item.eventGuid) }
                    )
                }
            }
        }
    }
}

@Composable
private fun QueueItemRow(
    item: QueueEventEntity,
    isCurrent: Boolean = false,
    modifier: Modifier = Modifier,
    dragHandleModifier: Modifier = Modifier,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrent) 4.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Drag handle — gesture is attached here only
            Icon(
                imageVector = Icons.Default.DragHandle,
                contentDescription = "Drag to reorder",
                modifier = dragHandleModifier
                    .fillMaxHeight()
                    .width(40.dp)
                    .padding(horizontal = 8.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )

            // Thumbnail (16:9 aspect at 72dp row height)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(4.dp))
            ) {
                AsyncImage(
                    model = item.thumbUrl ?: item.posterUrl,
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                item.duration?.let { duration ->
                    val minutes = duration / 60
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(3.dp)
                            .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(3.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "$minutes min",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }
            }

            // Info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.Center
            ) {
                if (isCurrent) {
                    Text(
                        text = "▶ ${stringResource(Res.string.currently_playing)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = if (isCurrent) 1 else 2,
                    overflow = TextOverflow.Ellipsis
                )
                item.conferenceTitle?.let { conference ->
                    Text(
                        text = conference,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Remove button
            IconButton(
                onClick = onRemove,
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(Res.string.remove_from_queue),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
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

private val previewQueueItems = listOf(
    QueueEventEntity(
        eventGuid = "preview-1",
        title = "The State of Onion",
        thumbUrl = null,
        posterUrl = null,
        conferenceTitle = "37C3: Unlocked",
        persons = "Roger Dingledine",
        duration = 3180,
        order = 0
    ),
    QueueEventEntity(
        eventGuid = "preview-2",
        title = "How to Build a Satellite That Actually Works in Space",
        thumbUrl = null,
        posterUrl = null,
        conferenceTitle = "37C3: Unlocked",
        persons = "Alice Engineer, Bob Rocket",
        duration = 2700,
        order = 1
    ),
    QueueEventEntity(
        eventGuid = "preview-3",
        title = "Inside the Mechanical Keyboard Rabbit Hole",
        thumbUrl = null,
        posterUrl = null,
        conferenceTitle = "36C3",
        persons = null,
        duration = 1800,
        order = 2
    )
)

@MultiplatformPreview
@Composable
private fun QueueItemRowPreview() {
    MaterialTheme {
        QueueItemRow(
            item = previewQueueItems[0],
            onClick = {},
            onRemove = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@MultiplatformPreview
@Composable
private fun QueueScreenPreview() {
    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Queue") },
                    navigationIcon = {
                        Text(
                            text = "←",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                itemsIndexed(previewQueueItems, key = { _, item -> item.eventGuid }) { _, item ->
                    QueueItemRow(
                        item = item,
                        onClick = {},
                        onRemove = {}
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@MultiplatformPreview
@Composable
private fun QueueScreenEmptyPreview() {
    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Queue") },
                    navigationIcon = {
                        Text(
                            text = "←",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(horizontal = 12.dp)
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
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No queue yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
