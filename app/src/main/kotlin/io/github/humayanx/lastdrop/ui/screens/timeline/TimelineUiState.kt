package io.github.humayanx.lastdrop.ui.screens.timeline

import io.github.humayanx.lastdrop.data.local.entity.NotificationRecord

data class AppFilterItem(
    val appName: String,
    val count: Int
)

data class TimelineUiState(
    val notifications: List<NotificationRecord> = emptyList(),
    val appFilters: List<AppFilterItem> = emptyList(),
    val totalCount: Int = 0,
    val selectedApp: String? = null,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val selectedRecordForDetail: NotificationRecord? = null,
    val isPermissionGranted: Boolean = false,
    val isServiceConnected: Boolean = false,
    val isBatteryOptimizedIgnored: Boolean = false,
    val isLoading: Boolean = true
) {
    val distinctApps: List<String> get() = appFilters.map { it.appName }
}
