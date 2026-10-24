package io.github.humayanx.lastdrop

import android.app.Notification
import android.os.UserHandle
import android.service.notification.StatusBarNotification
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import io.github.humayanx.lastdrop.data.local.AppDatabase
import io.github.humayanx.lastdrop.data.local.entity.NotificationRecord
import io.github.humayanx.lastdrop.service.NotificationParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class IngestionTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun concurrentDuplicateCallbacksInsertOneRow() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            val dao = db.notificationDao()
            val record = NotificationRecord(packageName = "gmail", appName = "Gmail",
                notificationKey = "gmail|1", title = "Alice", content = "Hello", timestamp = 10_000L)
            val ids = (1..20).map {
                async(Dispatchers.IO) { dao.insertUnlessDuplicate(record) }
            }.awaitAll()
            assertEquals(1, ids.toSet().size)
            assertEquals(1, dao.getTotalCount().first())
            dao.insertUnlessDuplicate(record.copy(title = "Bob", timestamp = 11_000L))
            assertEquals(ids.first(), dao.insertUnlessDuplicate(record))
            dao.insertUnlessDuplicate(record.copy(timestamp = 13_001L))
            assertEquals(3, dao.getTotalCount().first())
            dao.deleteExistingDuplicates()
            assertEquals(3, dao.getTotalCount().first())
        } finally {
            db.close()
        }
    }

    @Test
    fun identityUpdatesAndRetentionPreserveValidHistory() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            val dao = db.notificationDao()
            val old = NotificationRecord(packageName = "chat", appName = "Chat",
                notificationKey = "chat|1", title = "Alice", content = "Hello", timestamp = 1L)
            dao.insertUnlessDuplicate(old)
            dao.insertUnlessDuplicate(old.copy(notificationKey = "chat|2"))
            dao.insertUnlessDuplicate(old.copy(timestamp = 2L))
            dao.insertUnlessDuplicate(old.copy(content = "Updated"))
            dao.insertUnlessDuplicate(old.copy(bigText = "Expanded"))
            repeat(2) { dao.insertUnlessDuplicate(old.copy(notificationKey = null)) }
            assertEquals(7, dao.getTotalCount().first())
            assertEquals(0, dao.deleteExistingDuplicates())
            dao.insertNotification(old.copy(isStarred = true))
            dao.insertNotification(old.copy(timestamp = 100L))
            assertEquals(7, dao.deleteOlderThan(100L))
            val remaining = dao.getAllNotifications().first()
            assertEquals(2, remaining.size)
            assertEquals(1, remaining.count { it.isStarred })
            assertEquals(1, remaining.count { it.timestamp == 100L })
        } finally { db.close() }
    }

    @Test
    fun dailyLeaderboardExcludesYesterdayAndRanksPackages() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            val dao = db.notificationDao()
            val record = NotificationRecord(packageName = "mail", appName = "Mail",
                title = "Hello", content = "Body", timestamp = 10_000L)
            dao.insertAll(listOf(record.copy(timestamp = 9_999L), record,
                record.copy(timestamp = 11_000L), record.copy(packageName = "shop", appName = "Shop")))
            val counts = dao.getTopSpamApps(10_000L).first()
            assertEquals(listOf("mail", "shop"), counts.map { it.packageName })
            assertEquals(listOf(2, 1), counts.map { it.count })
            assertEquals(emptyList<Any>(), dao.getTopSpamApps(12_000L).first())
        } finally { db.close() }
    }

    @Test
    fun searchOperatorsAndPunctuationDoNotBreakFts() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            val repo = io.github.humayanx.lastdrop.data.repository.NotificationRepositoryImpl(db.notificationDao())
            repo.insertNotification(NotificationRecord(packageName = "mail", appName = "Mail",
                title = "AND", content = "delivery updates", timestamp = 10_000L))
            assertEquals(1, repo.searchNotifications("AND (del)").first().size)
            assertEquals(0, repo.searchNotifications("():*").first().size)
            assertEquals(0, repo.searchNotifications("OR").first().size)
        } finally { db.close() }
    }

    @Test
    fun summaryWithRealContentIsIgnoredButChildIsKept() {
        fun posted(summary: Boolean): StatusBarNotification {
            val notification = Notification.Builder(context, "test")
                .setContentTitle("Alice").setContentText("A real email body")
                .setGroup("inbox").setGroupSummary(summary).build()
            return StatusBarNotification("test.mail", "test.mail", 1, null, 0, 0, 0,
                notification, UserHandle.getUserHandleForUid(0), 10_000L)
        }
        assertNull(NotificationParser.parse(context, posted(true)))
        assertNotNull(NotificationParser.parse(context, posted(false)))
    }

    @Test
    fun activeMediaNotificationIsIgnoredWhileRegularNotificationFromSameAppIsKept() {
        fun createMediaNotification(): StatusBarNotification {
            val notification = Notification.Builder(context, "media_channel")
                .setContentTitle("Song Name")
                .setContentText("Artist Name")
                .setCategory(Notification.CATEGORY_TRANSPORT)
                .build()
            return StatusBarNotification("com.spotify.music", "com.spotify.music", 101, null, 0, 0, 0,
                notification, UserHandle.getUserHandleForUid(0), 10_000L)
        }

        fun createRegularNotification(): StatusBarNotification {
            val notification = Notification.Builder(context, "social_channel")
                .setContentTitle("Spotify")
                .setContentText("Your friend started following your playlist")
                .build()
            return StatusBarNotification("com.spotify.music", "com.spotify.music", 102, null, 0, 0, 0,
                notification, UserHandle.getUserHandleForUid(0), 10_000L)
        }

        assertNull(NotificationParser.parse(context, createMediaNotification()))
        val parsed = NotificationParser.parse(context, createRegularNotification())
        assertNotNull(parsed)
        assertEquals("com.spotify.music", parsed?.packageName)
        assertEquals("Spotify", parsed?.title)
    }
}
