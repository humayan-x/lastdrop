package io.github.humayanx.lastdrop.ui.screens.starred

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.humayanx.lastdrop.data.local.entity.NotificationRecord
import io.github.humayanx.lastdrop.ui.components.DeleteConfirmDialog
import io.github.humayanx.lastdrop.ui.components.LastDropHeader
import io.github.humayanx.lastdrop.ui.components.NotificationCard
import io.github.humayanx.lastdrop.ui.components.NotificationDetailSheet
import io.github.humayanx.lastdrop.ui.theme.MonoBody

@Composable
fun StarredScreen(
    viewModel: StarredViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var recordPendingDelete by remember { mutableStateOf<NotificationRecord?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Unified Header (No subtext, exact 56.dp baseline)
        LastDropHeader(
            title = "Saved"
        )

        if (uiState.starredNotifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "VAULT EMPTY",
                    style = MonoBody,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 112.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = uiState.starredNotifications,
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

    NotificationDetailSheet(
        record = uiState.selectedRecordForDetail,
        onDismiss = { viewModel.onSelectRecord(null) },
        onToggleStar = viewModel::onToggleStar,
        onDelete = { recordPendingDelete = it }
    )

    DeleteConfirmDialog(
        record = recordPendingDelete,
        onConfirm = {
            recordPendingDelete?.let { viewModel.onDeleteRecord(it) }
            recordPendingDelete = null
        },
        onDismiss = { recordPendingDelete = null }
    )
}
