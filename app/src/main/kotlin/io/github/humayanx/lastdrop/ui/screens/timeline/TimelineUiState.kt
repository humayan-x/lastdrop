package io.github.humayanx.lastdrop.ui.screens.timeline

import io.github.humayanx.lastdrop.data.local.entity.NotificationRecord

data class AppFilterItem(
    val appName: String,
    val count: Int,
    val key: String = appName
)

data class TimelineUiState(
    val notifications: List<NotificationRecord> = emptyList(),
    val appFilters: List<AppFilterItem> = emptyList(),
    val totalCount: Int = 0,
    val systemPackages: Set<String> = emptySet(),
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

data class NotificationAppGroup(
    val packageName: String,
    val notifications: List<NotificationRecord>
) {
    val appName: String get() = if (packageName == SYSTEM_GROUP_KEY) "System" else notifications.first().appName
}

const val SYSTEM_GROUP_KEY = "system:notifications"

fun groupNotificationsByApp(records: List<NotificationRecord>, systemPackages: Set<String> = emptySet()): List<NotificationAppGroup> =
    records.sortedWith(compareByDescending<NotificationRecord> { it.timestamp }.thenByDescending { it.id })
        .groupBy { if (it.packageName in systemPackages) SYSTEM_GROUP_KEY else it.packageName }
        .map { (packageName, notifications) -> NotificationAppGroup(packageName, notifications) }
