package io.github.humayanx.lastdrop.ui.screens.timeline

import android.content.Context
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
        val source = when {
            query.isNotBlank() -> repository.searchNotifications(query).map { records ->
                if (app == null) records else records.filter { it.appName == app }
            }
            app != null -> repository.getNotificationsByAppName(app)
            else -> repository.allNotifications
        }
        source.map { records ->
            records.filter { it.timestamp >= todayStart }
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
        val countsByApp = todayRecords.groupingBy { it.appName }.eachCount()
        val filterList = countsByApp.entries
            .sortedBy { it.key.lowercase() }
            .map { AppFilterItem(it.key, it.value) }
        Pair(total, filterList)
    }

    val uiState: StateFlow<TimelineUiState> = combine(
        notificationsFlow,
        appFiltersFlow,
        filterStateFlow,
        LastDropListenerService.isServiceConnected
    ) { notifications, (total, filters), filter, isConnected ->
        TimelineUiState(
            notifications = notifications,
            appFilters = filters,
            totalCount = total,
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
