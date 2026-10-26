package io.github.humayanx.lastdrop.ui.screens.archive

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.humayanx.lastdrop.data.local.entity.NotificationRecord
import io.github.humayanx.lastdrop.ui.components.DeleteConfirmDialog
import io.github.humayanx.lastdrop.ui.components.LastDropHeader
import io.github.humayanx.lastdrop.ui.components.LoadingContent
import io.github.humayanx.lastdrop.ui.components.NotificationCard
import io.github.humayanx.lastdrop.ui.components.NotificationDetailSheet
import io.github.humayanx.lastdrop.ui.components.SearchInputBar
import io.github.humayanx.lastdrop.ui.theme.AppIcons
import io.github.humayanx.lastdrop.ui.theme.MonoBody
import io.github.humayanx.lastdrop.ui.theme.MonoHeader

@Composable
fun ArchiveScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ArchiveViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var recordPendingDelete by remember { mutableStateOf<NotificationRecord?>(null) }

    // If a specific date card is clicked, open the Day Notifications Page
    if (uiState.selectedDateItem != null) {
        val selectedItem = uiState.selectedDateItem!!
        BackHandler { viewModel.onSelectDate(null) }

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header for Day View: Day (short) + Full Date
            LastDropHeader(
                title = "${selectedItem.dayShortName}, ${selectedItem.fullDate}",
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.onSelectDate(null) },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = AppIcons.ArrowBack,
                            contentDescription = "Back to Archive",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${selectedItem.notifications.size} DROPS",
                            style = MonoBody.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )

            // Notifications of this specific day
            if (selectedItem.notifications.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "NO NOTIFICATIONS RECORDED",
                        style = MonoBody,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 112.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = selectedItem.notifications,
                        key = { it.id }
                    ) { record ->
                        NotificationCard(
                            record = record,
                            onClick = { viewModel.onSelectRecord(record) },
                            onToggleStar = { viewModel.onToggleStar(record) },
                            onDelete = { recordPendingDelete = record }
                        )
                    }
                }
            }
        }

        // Detail Bottom Sheet
        NotificationDetailSheet(
            record = uiState.selectedRecordForDetail,
            onDismiss = { viewModel.onSelectRecord(null) },
            onToggleStar = viewModel::onToggleStar,
            onDelete = { recordPendingDelete = it }
        )

        // Delete Confirm Dialog
        DeleteConfirmDialog(
            record = recordPendingDelete,
            onConfirm = {
                recordPendingDelete?.let { viewModel.onDeleteRecord(it) }
                recordPendingDelete = null
            },
            onDismiss = { recordPendingDelete = null }
        )

        return
    }

    // Default View: Archive Date Cards List
    BackHandler(onBack = onBack)

    if (uiState.isLoading) {
        Column(modifier.fillMaxSize()) {
            LastDropHeader(
                title = "Archive",
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                        Icon(
                            imageVector = AppIcons.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            )
            LoadingContent(Modifier.weight(1f))
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header with Back Arrow & Retention Policy Info
        LastDropHeader(
            title = "Archive",
            navigationIcon = {
                IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = AppIcons.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            actions = {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${uiState.retentionPeriod.days}D RETENTION",
                        style = MonoBody.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        )

        // Search Bar that searches date and day name
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            SearchInputBar(
                query = uiState.searchQuery,
                onQueryChange = viewModel::onSearchQueryChanged,
                placeholder = "Search date or day (e.g. Sun, Oct 25)..."
            )
        }

        // List cards with full date and day (short form) name
        if (uiState.dateCards.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Text(
                        text = if (uiState.searchQuery.isNotBlank()) "NO MATCHING DATES" else "NO ARCHIVED DATES",
                        style = MonoHeader,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (uiState.searchQuery.isNotBlank())
                            "No recorded past dates match \"${uiState.searchQuery}\"."
                        else
                            "Notifications from previous days (within your ${uiState.retentionPeriod.days}-day cleanup policy) will be organized here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 112.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = uiState.dateCards,
                    key = { it.date.toString() }
                ) { item ->
                    ArchiveDateCard(
                        item = item,
                        onClick = { viewModel.onSelectDate(item.date) }
                    )
                }
            }
        }
    }
}

/**
 * List card displaying day (short form) name, full date, and drop count.
 */
@Composable
private fun ArchiveDateCard(
    item: ArchiveDateItem,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Day short form name badge (e.g. "SUN", "MON")
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = item.dayShortName,
                        style = MonoHeader.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.fullDate,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        if (item.relativeLabel != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "• ${item.relativeLabel}",
                                style = MonoBody.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${item.count} ${if (item.count == 1) "notification" else "notifications"}",
                        style = MonoBody.copy(
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            Icon(
                imageVector = AppIcons.ChevronRight,
                contentDescription = "Open date",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
