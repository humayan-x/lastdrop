package io.github.humayanx.lastdrop.ui.screens.timeline

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import io.github.humayanx.lastdrop.ui.components.NativeAppIcon
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.humayanx.lastdrop.data.local.entity.NotificationRecord
import io.github.humayanx.lastdrop.service.PermissionManager
import io.github.humayanx.lastdrop.ui.components.AppFilterChips
import io.github.humayanx.lastdrop.ui.components.DeleteConfirmDialog
import io.github.humayanx.lastdrop.ui.components.LastDropHeader
import io.github.humayanx.lastdrop.ui.components.LoadingContent
import io.github.humayanx.lastdrop.ui.components.NotificationCard
import io.github.humayanx.lastdrop.ui.components.NotificationDetailSheet
import io.github.humayanx.lastdrop.ui.components.PermissionPromptDialog
import io.github.humayanx.lastdrop.ui.components.SearchInputBar
import io.github.humayanx.lastdrop.ui.screens.archive.ArchiveScreen
import io.github.humayanx.lastdrop.ui.theme.AppIcons
import io.github.humayanx.lastdrop.ui.theme.MonoBody

@Composable
fun TimelineScreen(
    viewModel: TimelineViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var recordPendingDelete by remember { mutableStateOf<NotificationRecord?>(null) }
    val groups = remember(uiState.notifications, uiState.systemPackages) { groupNotificationsByApp(uiState.notifications, uiState.systemPackages) }
    var expandedPackages by rememberSaveable { mutableStateOf(emptyList<String>()) }
    var showArchive by rememberSaveable { mutableStateOf(false) }

    if (showArchive) {
        ArchiveScreen(
            onBack = { showArchive = false },
            modifier = modifier
        )
        return
    }

    // Real-time resume listener: updates immediately when returning from system settings
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkPermission(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            viewModel.checkPermission(context)
        }
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (uiState.isLoading) {
        Column(modifier.fillMaxSize()) {
            LastDropHeader(title = "History")
            LoadingContent(Modifier.weight(1f))
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Unified Header with Timer icon pointing to Archive
        LastDropHeader(
            title = "History",
            actions = {
                IconButton(
                    onClick = { showArchive = true },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = AppIcons.Timer,
                        contentDescription = "Archived Drops",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        )

        // Search Bar (Always visible)
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            SearchInputBar(
                query = uiState.searchQuery,
                onQueryChange = viewModel::onSearchQueryChanged,
                placeholder = "Search ledger..."
            )
        }

        // App Filters Row with breathing room
        if (uiState.appFilters.isNotEmpty()) {
            Spacer(modifier = Modifier.height(2.dp))
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                AppFilterChips(
                    filters = uiState.appFilters,
                    totalCount = uiState.totalCount,
                    selectedApp = uiState.selectedApp,
                    onAppSelected = viewModel::onAppSelected
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Notification Stream
        if (uiState.notifications.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "NO DROPS TODAY",
                    style = MonoBody,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 112.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (uiState.selectedApp != null) {
                    items(uiState.notifications, key = { "notification:${it.id}" }) { record ->
                        NotificationCard(
                            record = record,
                            onClick = { viewModel.onSelectRecord(record) },
                            onToggleStar = { viewModel.onToggleStar(record) },
                            onDelete = { recordPendingDelete = record }
                        )
                    }
                } else groups.forEach { group ->
                    val expanded = group.packageName in expandedPackages
                    item(key = "app:${group.packageName}") {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .semantics { stateDescription = if (expanded) "Expanded" else "Collapsed" }
                                    .clickable {
                                        expandedPackages = if (expanded) expandedPackages - group.packageName
                                        else expandedPackages + group.packageName
                                    }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (group.packageName == SYSTEM_GROUP_KEY) {
                                    Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(28.dp))
                                } else {
                                    NativeAppIcon(group.packageName)
                                }
                                Text(group.appName, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                                Text("${group.notifications.size}", style = MonoBody)
                                Icon(
                                    imageVector = AppIcons.ChevronRight,
                                    modifier = Modifier.rotate(if (expanded) -90f else 90f),
                                    contentDescription = if (expanded) "Collapse notifications" else "Expand notifications"
                                )
                            }
                        }
                    }
                    if (expanded) {
                        items(group.notifications, key = { "notification:${it.id}" }) { record ->
                            NotificationCard(
                                record = record,
                                onClick = { viewModel.onSelectRecord(record) },
                                onToggleStar = { viewModel.onToggleStar(record) },
                                onDelete = { recordPendingDelete = record },
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        }
                    }
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

    // Confirmation alert box before deletion
    DeleteConfirmDialog(
        record = recordPendingDelete,
        onConfirm = {
            recordPendingDelete?.let { viewModel.onDeleteRecord(it) }
            recordPendingDelete = null
        },
        onDismiss = { recordPendingDelete = null }
    )
}
