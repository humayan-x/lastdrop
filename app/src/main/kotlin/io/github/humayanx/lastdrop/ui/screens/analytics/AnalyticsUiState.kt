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
)
