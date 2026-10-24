package io.github.humayanx.lastdrop

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import io.github.humayanx.lastdrop.data.local.AppDatabase
import io.github.humayanx.lastdrop.data.local.entity.NotificationRecord
import io.github.humayanx.lastdrop.data.repository.NotificationRepositoryImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class PrivacyAndAnalyticsTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val message = NotificationRecord(packageName = "chat", appName = "Chat",
        title = "Alice", content = "Hello", timestamp = 1)

    @Test fun privacyCleanupRemovesSavedCodesAndSearchIndex() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            val dao = db.notificationDao()
            val repo = NotificationRepositoryImpl(dao)
            dao.insertNotification(message.copy(content = "Verification code: 123456", isStarred = true))
            val id = repo.insertNotification(message)
            assertEquals(1, repo.allNotifications.first().size)
            assertTrue(repo.starredNotifications.first().isEmpty())
            assertTrue(repo.searchNotifications("verification").first().isEmpty())
            assertEquals(1, repo.purgeAuthenticationCodes())
            assertEquals(1, dao.getTotalCount().first())
            assertTrue(dao.searchNotifications("verification").first().isEmpty())
            repo.toggleStar(id, true)
            assertTrue(repo.observeById(id).first()!!.isStarred)
            repo.toggleStar(id, false)
            assertFalse(repo.observeById(id).first()!!.isStarred)
            repo.deleteById(id)
            assertNull(repo.observeById(id).first())
        } finally { db.close() }
    }

    @Test fun fallBackDayIncludesBothRepeatedHoursAndExcludesNextMidnight() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            val dao = db.notificationDao()
            fun millis(value: String) = Instant.parse(value).toEpochMilli()
            listOf("2026-11-01T03:59:59Z", "2026-11-01T05:30:00Z", "2026-11-01T06:30:00Z",
                "2026-11-02T04:30:00Z", "2026-11-02T05:00:00Z").forEach {
                dao.insertNotification(message.copy(timestamp = millis(it)))
            }
            val counts = NotificationRepositoryImpl(dao).getHourlyDistribution(
                millis("2026-11-01T04:00:00Z"), millis("2026-11-02T05:00:00Z"),
                ZoneId.of("America/New_York")
            ).first()
            assertEquals(2, counts[1].count)
            assertEquals(1, counts[23].count)
            assertEquals(3, counts.sumOf { it.count })
        } finally { db.close() }
    }
}
