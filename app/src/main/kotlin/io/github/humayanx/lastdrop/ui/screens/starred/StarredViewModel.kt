package io.github.humayanx.lastdrop.ui.screens.starred

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.humayanx.lastdrop.LastDropApp
import io.github.humayanx.lastdrop.data.local.entity.NotificationRecord
import io.github.humayanx.lastdrop.data.repository.NotificationRepository
import io.github.humayanx.lastdrop.ui.state.NotificationSelection
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StarredViewModel(
    private val repository: NotificationRepository = LastDropApp.instance.notificationRepository
) : ViewModel() {

    private val selection = NotificationSelection(repository::observeById)
    private val selectedRecord = selection.record

    val uiState: StateFlow<StarredUiState> = combine(
        repository.starredNotifications,
        selectedRecord
    ) { starred, record ->
        StarredUiState(
            starredNotifications = starred,
            selectedRecordForDetail = record
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StarredUiState()
    )

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
