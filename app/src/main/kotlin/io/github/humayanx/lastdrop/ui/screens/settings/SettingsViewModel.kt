package io.github.humayanx.lastdrop.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.humayanx.lastdrop.LastDropApp
import io.github.humayanx.lastdrop.data.repository.NotificationRepository
import io.github.humayanx.lastdrop.data.repository.RetentionPeriod
import io.github.humayanx.lastdrop.data.repository.ThemeMode
import io.github.humayanx.lastdrop.data.repository.UserPreferencesRepository
import io.github.humayanx.lastdrop.service.LastDropListenerService
import io.github.humayanx.lastdrop.service.PermissionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

class SettingsViewModel(
    private val notificationRepository: NotificationRepository = LastDropApp.instance.notificationRepository,
    private val preferencesRepository: UserPreferencesRepository = LastDropApp.instance.userPreferencesRepository
) : ViewModel() {

    private val isNotificationListenerGranted = MutableStateFlow(
        runCatching { PermissionManager.isNotificationListenerEnabled(LastDropApp.instance) }.getOrDefault(false)
    )
    private val isBatteryOptimizedIgnored = MutableStateFlow(
        runCatching { PermissionManager.isBatteryOptimizationIgnored(LastDropApp.instance) }.getOrDefault(false)
    )
    private var batteryRefreshJob: Job? = null
    private val showClearAllDialog = MutableStateFlow(false)

    val uiState: StateFlow<SettingsUiState> = combine(
        preferencesRepository.userPreferencesFlow,
        isNotificationListenerGranted,
        LastDropListenerService.isServiceConnected,
        isBatteryOptimizedIgnored,
        showClearAllDialog
    ) { prefs, listenerGranted, serviceConnected, batteryIgnored, showDialog ->
        SettingsUiState(
            isNotificationListenerGranted = listenerGranted,
            isServiceConnected = serviceConnected,
            isBatteryOptimizedIgnored = batteryIgnored,
            setupCompleted = prefs.setupCompleted,
            themeMode = prefs.themeMode,
            retentionPeriod = prefs.retentionPeriod,
            showClearAllDialog = showDialog
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun refreshPermissions(context: Context) {
        val granted = PermissionManager.isNotificationListenerEnabled(context)
        isNotificationListenerGranted.value = granted
        refreshBatteryStatus(context)
        if (granted) {
            PermissionManager.ensureServiceBound(context)
        }
    }

    fun refreshBatteryStatus(context: Context) {
        val appContext = context.applicationContext
        batteryRefreshJob?.cancel()
        isBatteryOptimizedIgnored.value = PermissionManager.isBatteryOptimizationIgnored(appContext)
        batteryRefreshJob = viewModelScope.launch {
            // Some OEMs publish the new state shortly after their settings Activity closes.
            repeat(6) {
                delay(250L)
                isBatteryOptimizedIgnored.value = PermissionManager.isBatteryOptimizationIgnored(appContext)
            }
        }
    }

    fun reconnectService(context: Context) {
        PermissionManager.ensureServiceBound(context, force = true)
        refreshPermissions(context)
    }

    suspend fun completeSetup(context: Context): Boolean {
        refreshPermissions(context)
        if (!isNotificationListenerGranted.value || !isBatteryOptimizedIgnored.value ||
            !LastDropListenerService.isServiceConnected.value) return false
        preferencesRepository.completeSetup()
        return true
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(mode)
        }
    }

    fun setRetentionPeriod(period: RetentionPeriod) {
        viewModelScope.launch {
            preferencesRepository.setRetentionPeriod(period)
            if (period.days > 0) {
                notificationRepository.purgeOlderThan(period.days)
            }
        }
    }

    fun setClearAllDialogVisible(visible: Boolean) {
        showClearAllDialog.value = visible
    }

    fun clearAllLogs() {
        viewModelScope.launch {
            notificationRepository.deleteAll()
            showClearAllDialog.value = false
        }
    }
}
