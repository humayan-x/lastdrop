package io.github.humayanx.lastdrop.ui.state

import io.github.humayanx.lastdrop.data.local.entity.NotificationRecord
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

/** Selection survives changes to filters and saved status; deletion closes the sheet. */
@OptIn(ExperimentalCoroutinesApi::class)
internal class NotificationSelection(observe: (Long) -> Flow<NotificationRecord?>) {
    private val id = MutableStateFlow<Long?>(null)
    val record = id.flatMapLatest { selectedId ->
        if (selectedId == null) flowOf(null) else observe(selectedId)
    }

    fun select(record: NotificationRecord?) {
        id.value = record?.id
    }
}
