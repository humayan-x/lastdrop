package io.github.humayanx.lastdrop.ui.screens

import io.github.humayanx.lastdrop.data.local.entity.NotificationRecord
import io.github.humayanx.lastdrop.data.local.model.AppNotificationCount
import io.github.humayanx.lastdrop.ui.screens.analytics.AnalyticsUiState
import io.github.humayanx.lastdrop.ui.screens.timeline.groupNotificationsByApp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppGroupingTest {
    @Test
    fun historyGroupsByPackageAndOrdersGroupsAndNotificationsByRecency() {
        val records = listOf(
            record(1, "a", 10), record(2, "b", 30), record(3, "a", 40), record(4, "b", 20)
        )
        val groups = groupNotificationsByApp(records)
        assertEquals(listOf("a", "b"), groups.map { it.packageName })
        assertEquals(listOf(3L, 1L), groups[0].notifications.map { it.id })
        assertEquals(listOf(2L, 4L), groups[1].notifications.map { it.id })
        assertTrue(groupNotificationsByApp(emptyList()).isEmpty())
    }

    @Test
    fun systemSourcesShareOneGroupWhileOtherAppsRemainSeparate() {
        val groups = groupNotificationsByApp(
            listOf(record(1, "system.a", 10), record(2, "regular", 20), record(3, "system.b", 30)),
            setOf("system.a", "system.b")
        )
        assertEquals(2, groups.size)
        assertEquals("System", groups[0].appName)
        assertEquals(listOf(3L, 1L), groups[0].notifications.map { it.id })
        assertEquals("regular", groups[1].packageName)
    }

    @Test
    fun homeRanksTopThreeAndExcludesThemFromOtherApps() {
        val state = AnalyticsUiState(appCounts = listOf(app("a", 1), app("b", 8), app("c", 4), app("d", 6)))
        assertEquals(listOf("b", "d", "c"), state.topSpammerApps.map { it.packageName })
        assertEquals(listOf("a"), state.otherAppsToday.map { it.packageName })
        assertTrue(AnalyticsUiState(appCounts = listOf(app("a", 1))).otherAppsToday.isEmpty())
        assertTrue(AnalyticsUiState().topSpammerApps.isEmpty())
    }

    private fun app(pkg: String, count: Int) = AppNotificationCount(pkg, "Same name", count)
    private fun record(id: Long, pkg: String, time: Long) = NotificationRecord(
        id = id, packageName = pkg, appName = "Same name", title = null, content = null, timestamp = time
    )
}
