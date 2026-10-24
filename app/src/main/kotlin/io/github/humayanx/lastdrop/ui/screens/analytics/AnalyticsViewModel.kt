package io.github.humayanx.lastdrop.ui.screens.analytics

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.humayanx.lastdrop.LastDropApp
import io.github.humayanx.lastdrop.data.repository.NotificationRepository
import io.github.humayanx.lastdrop.data.repository.ThemeMode
import io.github.humayanx.lastdrop.data.repository.UserPreferencesRepository
import io.github.humayanx.lastdrop.service.LastDropListenerService
import io.github.humayanx.lastdrop.service.PermissionManager
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal fun startOfDay(now: Instant, zone: ZoneId): Long =
    now.atZone(zone).toLocalDate().atStartOfDay(zone).toInstant().toEpochMilli()

@OptIn(ExperimentalCoroutinesApi::class)
class AnalyticsViewModel(
    repository: NotificationRepository = LastDropApp.instance.notificationRepository,
    private val preferencesRepository: UserPreferencesRepository = LastDropApp.instance.userPreferencesRepository
) : ViewModel() {
    private val refreshSignal = MutableStateFlow(0L)

    // Re-evaluate local midnight while visible, including changes to the device time zone.
    private val dayStart = flow {
        while (true) {
            val now = Instant.now().atZone(ZoneId.systemDefault())
            emit(Triple(now.toLocalDate(), now.zone, now.hour))
            delay(30_000L)
        }
    }.distinctUntilChanged()

    // Real-time continuous permission sync engine running off-main-thread
    private val permissionsFlow = refreshSignal.flatMapLatest {
        flow {
            while (true) {
                val granted = PermissionManager.isNotificationListenerEnabled(LastDropApp.instance)
                val battery = PermissionManager.isBatteryOptimizationIgnored(LastDropApp.instance)
                emit(Pair(granted, battery))
                if (granted && !LastDropListenerService.isServiceConnected.value) {
                    PermissionManager.ensureServiceBound(LastDropApp.instance)
                }
                // When permissions are ungranted, poll every 350ms so returning from settings updates instantly.
                // Once both are granted, back off to 2500ms.
                val interval = if (!granted || !battery) 350L else 2500L
                delay(interval)
            }
        }
    }.flowOn(Dispatchers.IO)

    private val statsFlow = dayStart.flatMapLatest { (date, zone, currentHour) ->
        val since = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val until = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        combine(
            repository.getTopSpamApps(since),
            repository.getHourlyDistribution(since, until, zone)
        ) { apps, hourlyList ->
            val hourlyMap = hourlyList.associate { it.hourOfDay to it.count }
            val distribution = (0..23).map { hour -> hourlyMap[hour] ?: 0 }
            val maxHour = distribution.maxOrNull()?.coerceAtLeast(1) ?: 1
            Triple(apps, distribution, Pair(maxHour, currentHour))
        }
    }

    val uiState = combine(
        statsFlow,
        permissionsFlow,
        LastDropListenerService.isServiceConnected
    ) { (apps, distribution, hourData), (granted, battery), connected ->
        AnalyticsUiState(
            todayCount = apps.sumOf { it.count },
            appCounts = apps,
            maxAppCount = apps.firstOrNull()?.count ?: 1,
            hourlyDistribution = distribution,
            maxHourlyCount = hourData.first,
            currentHour = hourData.second,
            isPermissionGranted = granted,
            isServiceConnected = connected,
            isBatteryOptimizedIgnored = battery,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AnalyticsUiState())

    fun refreshPermissions() {
        refreshSignal.value = System.currentTimeMillis()
        val granted = PermissionManager.isNotificationListenerEnabled(LastDropApp.instance)
        if (granted) {
            PermissionManager.ensureServiceBound(LastDropApp.instance, force = true)
        }
    }

    fun reconnectService(context: Context) {
        PermissionManager.ensureServiceBound(context, force = true)
        refreshPermissions()
    }

    fun toggleTheme(isCurrentlyDark: Boolean) {
        viewModelScope.launch {
            val targetMode = if (isCurrentlyDark) ThemeMode.LIGHT else ThemeMode.DARK
            preferencesRepository.setThemeMode(targetMode)
        }
    }
}
