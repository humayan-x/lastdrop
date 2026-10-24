package io.github.humayanx.lastdrop.ui.screens.settings

import io.github.humayanx.lastdrop.data.repository.RetentionPeriod
import io.github.humayanx.lastdrop.data.repository.ThemeMode

data class SettingsUiState(
    val isNotificationListenerGranted: Boolean = false,
    val isServiceConnected: Boolean = false,
    val isBatteryOptimizedIgnored: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val retentionPeriod: RetentionPeriod = RetentionPeriod.DAYS_30,
    val setupCompleted: Boolean = false,
    val showClearAllDialog: Boolean = false
)
