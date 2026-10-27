package io.github.humayanx.lastdrop.ui.screens.analytics

import io.github.humayanx.lastdrop.data.local.model.AppNotificationCount

data class AnalyticsUiState(
    val isLoading: Boolean = true,
    val todayCount: Int = 0,
    val appCounts: List<AppNotificationCount> = emptyList(),
    val maxAppCount: Int = 1,
    val hourlyDistribution: List<Int> = List(24) { 0 },
    val maxHourlyCount: Int = 1,
    val currentHour: Int = 0,
    val isPermissionGranted: Boolean = false,
    val isServiceConnected: Boolean = false,
    val isBatteryOptimizedIgnored: Boolean = false
) {
    val topSpammerApps: List<AppNotificationCount>
        get() = appCounts.sortedWith(
            compareByDescending<AppNotificationCount> { it.count }.thenBy { it.packageName }
        ).take(3)

    val otherAppsToday: List<AppNotificationCount>
        get() {
            val topPackages = topSpammerApps.map { it.packageName }.toSet()
            return appCounts.filterNot { it.packageName in topPackages }
        }
}
