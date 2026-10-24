package io.github.humayanx.lastdrop.ui.state

import io.github.humayanx.lastdrop.data.local.entity.NotificationRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class NotificationSelectionTest {
    @Test fun savingUnsavingAndDeletionRefreshTheOpenSelection() = runBlocking {
        val initial = NotificationRecord(id = 1, packageName = "chat", appName = "Chat",
            title = "Alice", content = "Hello", timestamp = 1)
        val rows = MutableStateFlow(listOf(initial))
        val selection = NotificationSelection { id -> rows.map { records -> records.find { it.id == id } } }
        selection.select(initial)
        assertFalse(selection.record.first()!!.isStarred)
        rows.value = listOf(initial.copy(isStarred = true))
        assertTrue(selection.record.first()!!.isStarred)
        // Unstarring must still leave the record observable outside the Saved list.
        rows.value = listOf(initial)
        assertFalse(selection.record.first()!!.isStarred)
        rows.value = emptyList()
        assertNull(selection.record.first())
        selection.select(null)
        assertNull(selection.record.first())
    }
}
