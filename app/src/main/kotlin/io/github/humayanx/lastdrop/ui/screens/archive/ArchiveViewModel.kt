package io.github.humayanx.lastdrop.ui.screens.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.humayanx.lastdrop.LastDropApp
import io.github.humayanx.lastdrop.data.local.entity.NotificationRecord
import io.github.humayanx.lastdrop.data.repository.NotificationRepository
import io.github.humayanx.lastdrop.data.repository.RetentionPeriod
import io.github.humayanx.lastdrop.data.repository.UserPreferencesRepository
import io.github.humayanx.lastdrop.ui.state.NotificationSelection
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class ArchiveViewModel(
    private val repository: NotificationRepository = LastDropApp.instance.notificationRepository,
    private val preferencesRepository: UserPreferencesRepository = LastDropApp.instance.userPreferencesRepository
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val selectedDate = MutableStateFlow<LocalDate?>(null)
    private val selection = NotificationSelection(repository::observeById)

    // Tick every 30s to update local date & midnight rollover
    private val todayFlow = flow {
        while (true) {
            val now = Instant.now()
            val zone = ZoneId.systemDefault()
            emit(now.atZone(zone).toLocalDate())
            delay(30_000L)
        }
    }.distinctUntilChanged()

    private val baseArchiveFlow = combine(
        repository.allNotifications,
        preferencesRepository.userPreferencesFlow,
        todayFlow
    ) { allRecords, prefs, today ->
        val zone = ZoneId.systemDefault()
        val todayStart = today.atStartOfDay(zone).toInstant().toEpochMilli()
        val cutoff = today.minusDays(prefs.retentionPeriod.days.toLong()).atStartOfDay(zone).toInstant().toEpochMilli()

        val archived = allRecords.filter { it.timestamp in cutoff until todayStart }
        Triple(archived, prefs.retentionPeriod, today)
    }

    val uiState: StateFlow<ArchiveUiState> = combine(
        baseArchiveFlow,
        searchQuery,
        selectedDate,
        selection.record
    ) { (archived, retentionPeriod, today), query, activeDate, detailRecord ->
        val zone = ZoneId.systemDefault()

        // Group archived records by LocalDate
        val grouped = archived.groupBy {
            Instant.ofEpochMilli(it.timestamp).atZone(zone).toLocalDate()
        }

        val fullDateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault())

        val allDateCards = grouped.entries.sortedByDescending { it.key }.map { (date, records) ->
            val dayShortName = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase()
            val dayFullName = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
            val fullDate = date.format(fullDateFormatter)
            val relativeLabel = when (date) {
                today.minusDays(1) -> "YESTERDAY"
                else -> null
            }
            ArchiveDateItem(
                date = date,
                dayShortName = dayShortName,
                dayFullName = dayFullName,
                fullDate = fullDate,
                relativeLabel = relativeLabel,
                count = records.size,
                notifications = records.sortedByDescending { it.timestamp }
            )
        }

        // Search bar that searches date and day name
        val trimmedQuery = query.trim()
        val filteredCards = if (trimmedQuery.isEmpty()) {
            allDateCards
        } else {
            allDateCards.filter { item ->
                item.dayShortName.contains(trimmedQuery, ignoreCase = true) ||
                item.dayFullName.contains(trimmedQuery, ignoreCase = true) ||
                item.fullDate.contains(trimmedQuery, ignoreCase = true) ||
                (item.relativeLabel?.contains(trimmedQuery, ignoreCase = true) == true)
            }
        }

        val selectedItem = allDateCards.find { it.date == activeDate }

        ArchiveUiState(
            dateCards = filteredCards,
            selectedDateItem = selectedItem,
            searchQuery = query,
            retentionPeriod = retentionPeriod,
            selectedRecordForDetail = detailRecord,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ArchiveUiState(retentionPeriod = RetentionPeriod.DAYS_7)
    )

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }

    fun onSelectDate(date: LocalDate?) {
        selectedDate.value = date
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
}
