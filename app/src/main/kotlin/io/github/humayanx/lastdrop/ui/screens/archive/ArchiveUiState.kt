package io.github.humayanx.lastdrop.ui.screens.archive

import io.github.humayanx.lastdrop.data.local.entity.NotificationRecord
import io.github.humayanx.lastdrop.data.repository.RetentionPeriod
import java.time.LocalDate

data class ArchiveDateItem(
    val date: LocalDate,
    val dayShortName: String,     // e.g. "SUN"
    val dayFullName: String,      // e.g. "Sunday"
    val fullDate: String,         // e.g. "25 October 2026"
    val relativeLabel: String?,   // e.g. "YESTERDAY"
    val count: Int,
    val notifications: List<NotificationRecord>
)

data class ArchiveUiState(
    val dateCards: List<ArchiveDateItem> = emptyList(),
    val selectedDateItem: ArchiveDateItem? = null,
    val searchQuery: String = "",
    val retentionPeriod: RetentionPeriod = RetentionPeriod.DAYS_7,
    val selectedRecordForDetail: NotificationRecord? = null,
    val isLoading: Boolean = true
)
