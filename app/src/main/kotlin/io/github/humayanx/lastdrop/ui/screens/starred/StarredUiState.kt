package io.github.humayanx.lastdrop.ui.screens.starred

import io.github.humayanx.lastdrop.data.local.entity.NotificationRecord

data class StarredUiState(
    val starredNotifications: List<NotificationRecord> = emptyList(),
    val selectedRecordForDetail: NotificationRecord? = null
)
