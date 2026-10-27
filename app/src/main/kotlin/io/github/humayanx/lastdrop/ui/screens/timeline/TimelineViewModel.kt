package io.github.humayanx.lastdrop.ui.screens.timeline

import android.content.Context
import android.content.pm.ApplicationInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.humayanx.lastdrop.LastDropApp
import io.github.humayanx.lastdrop.data.local.entity.NotificationRecord
import io.github.humayanx.lastdrop.data.repository.NotificationRepository
import io.github.humayanx.lastdrop.service.LastDropListenerService
import io.github.humayanx.lastdrop.service.PermissionManager
import io.github.humayanx.lastdrop.ui.screens.analytics.startOfDay
import io.github.humayanx.lastdrop.ui.state.NotificationSelection
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private data class FilterState(
    val app: String?,
    val query: String,
    val isSearchActive: Boolean,
    val selectedRecord: NotificationRecord?,
    val isPermissionGranted: Boolean,
    val isBatteryOptimizedIgnored: Boolean
)

@OptIn(ExperimentalCoroutinesApi::class)
class TimelineViewModel(
    private val repository: NotificationRepository = LastDropApp.instance.notificationRepository
) : ViewModel() {

    private val systemPackageCache = java.util.concurrent.ConcurrentHashMap<String, Boolean>()

    private suspend fun systemPackages(records: List<NotificationRecord>): Set<String> = withContext(Dispatchers.IO) {
        val pm = LastDropApp.instance.packageManager
        records.map { it.packageName }.distinct().filter { packageName ->
            systemPackageCache.getOrPut(packageName) {
                runCatching {
                    val info = pm.getApplicationInfo(packageName, 0)
                    val isSystem = info.flags and (ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
                    // System services without a launcher icon belong together; regular apps remain separate.
                    isSystem && pm.getLaunchIntentForPackage(packageName) == null
                }.getOrDefault(false)
            }
        }.toSet()
    }

    private val searchQuery = MutableStateFlow("")
    private val selectedApp = MutableStateFlow<String?>(null)
    private val isSearchActive = MutableStateFlow(false)
    private val selection = NotificationSelection(repository::observeById)
    private val selectedRecord = selection.record
    private val isPermissionGranted = MutableStateFlow(
        runCatching { PermissionManager.isNotificationListenerEnabled(LastDropApp.instance) }.getOrDefault(false)
    )
    private val isBatteryOptimizedIgnored = MutableStateFlow(
        runCatching { PermissionManager.isBatteryOptimizationIgnored(LastDropApp.instance) }.getOrDefault(false)
    )

    private val dayStartFlow = flow {
        while (true) {
            val now = Instant.now()
            val zone = ZoneId.systemDefault()
            emit(startOfDay(now, zone))
            delay(30_000L)
        }
    }.distinctUntilChanged()

    private val notificationsFlow = combine(
        searchQuery,
        selectedApp,
        dayStartFlow
    ) { query, app, todayStart ->
        Triple(query, app, todayStart)
    }.flatMapLatest { (query, app, todayStart) ->
        val source = if (query.isNotBlank()) repository.searchNotifications(query) else repository.allNotifications
        source.map { records ->
            val todayRecords = records.filter { it.timestamp >= todayStart }
            val system = systemPackages(todayRecords)
            todayRecords.filter {
                app == null || if (app == SYSTEM_GROUP_KEY) it.packageName in system else it.packageName == app
            }.sortedByDescending { it.timestamp }
        }
    }

    private val permissionsFlow = combine(
        isPermissionGranted,
        isBatteryOptimizedIgnored
    ) { hasPermission, batteryIgnored ->
        Pair(hasPermission, batteryIgnored)
    }

    private val filterStateFlow = combine(
        selectedApp,
        searchQuery,
        isSearchActive,
        selectedRecord,
        permissionsFlow
    ) { app, query, searchActive, record, (hasPermission, batteryIgnored) ->
        FilterState(app, query, searchActive, record, hasPermission, batteryIgnored)
    }

    private val appFiltersFlow = combine(
        repository.allNotifications,
        dayStartFlow
    ) { allRecords, todayStart ->
        val todayRecords = allRecords.filter { it.timestamp >= todayStart }
        val total = todayRecords.size
        val system = systemPackages(todayRecords)
        val filterList = groupNotificationsByApp(todayRecords, system)
            .map { AppFilterItem(it.appName, it.notifications.size, it.packageName) }
            .sortedBy { it.appName.lowercase() }
        Triple(total, filterList, system)
    }

    val uiState: StateFlow<TimelineUiState> = combine(
        notificationsFlow,
        appFiltersFlow,
        filterStateFlow,
        LastDropListenerService.isServiceConnected
    ) { notifications, (total, filters, system), filter, isConnected ->
        TimelineUiState(
            notifications = notifications,
            appFilters = filters,
            totalCount = total,
            systemPackages = system,
            selectedApp = filter.app,
            searchQuery = filter.query,
            isSearchActive = filter.isSearchActive,
            selectedRecordForDetail = filter.selectedRecord,
            isPermissionGranted = filter.isPermissionGranted,
            isServiceConnected = isConnected,
            isBatteryOptimizedIgnored = filter.isBatteryOptimizedIgnored,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TimelineUiState()
    )

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }

    fun onToggleSearch() {
        isSearchActive.value = !isSearchActive.value
        if (!isSearchActive.value) {
            searchQuery.value = ""
        }
    }

    fun onAppSelected(app: String?) {
        selectedApp.value = if (selectedApp.value == app) null else app
    }

    fun onSelectRecord(record: NotificationRecord?) {
        selection.select(record)
    }

    fun onToggleStar(record: NotificationRecord) {
        viewModelScope.launch {
            repository.toggleStar(record.id, !record.isStarred)
        }
    }

    fun onDeleteRecord(record: NotificationRecord) {
        viewModelScope.launch {
            repository.deleteById(record.id)
        }
    }

    fun checkPermission(context: Context) {
        val granted = PermissionManager.isNotificationListenerEnabled(context)
        isPermissionGranted.value = granted
        isBatteryOptimizedIgnored.value = PermissionManager.isBatteryOptimizationIgnored(context)
        if (granted) {
            PermissionManager.ensureServiceBound(context)
        }
    }

    fun reconnectService(context: Context) {
        PermissionManager.ensureServiceBound(context, force = true)
        checkPermission(context)
    }
}
