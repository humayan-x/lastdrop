package io.github.humayanx.lastdrop.data.repository

import io.github.humayanx.lastdrop.data.local.NotificationDao
import io.github.humayanx.lastdrop.data.local.entity.NotificationRecord
import io.github.humayanx.lastdrop.data.local.model.AppNotificationCount
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class DeduplicationTest {

    private class FakeNotificationDao : NotificationDao {
        val records = mutableListOf<NotificationRecord>()
        var nextId = 1L

        override suspend fun insertNotification(record: NotificationRecord): Long {
            val assigned = record.copy(id = nextId++)
            records.add(assigned)
            return assigned.id
        }

        override suspend fun insertAll(records: List<NotificationRecord>) {
            this.records.addAll(records)
        }

        override suspend fun findDuplicateId(
            notificationKey: String, packageName: String, timestamp: Long,
            title: String?, content: String?, bigText: String?, subText: String?
        ): Long? = records.lastOrNull {
            it.notificationKey == notificationKey && it.packageName == packageName &&
                it.timestamp == timestamp && it.title == title && it.content == content &&
                it.bigText == bigText && it.subText == subText
        }?.id

        override suspend fun deleteExistingDuplicates(): Int = 0

        override fun getAllNotifications(): Flow<List<NotificationRecord>> = flowOf(records)
        override fun getStarredNotifications(): Flow<List<NotificationRecord>> = flowOf(records.filter { it.isStarred })
        override fun getNotificationsByPackage(packageName: String): Flow<List<NotificationRecord>> = flowOf(records.filter { it.packageName == packageName })
        override fun getNotificationsByAppName(appName: String): Flow<List<NotificationRecord>> = flowOf(records.filter { it.appName == appName })
        override fun searchNotifications(ftsQuery: String): Flow<List<NotificationRecord>> = flowOf(emptyList())
        override fun getDistinctAppNames(): Flow<List<String>> = flowOf(records.map { it.appName }.distinct())
        override fun getAppNotificationCounts(): Flow<List<AppNotificationCount>> = flowOf(emptyList())
        override fun getTopSpamApps(sinceTimestamp: Long): Flow<List<AppNotificationCount>> = flowOf(
            records.filter { it.timestamp >= sinceTimestamp }.groupBy { it.packageName }
                .map { (pkg, records) -> AppNotificationCount(pkg, records.first().appName, records.size) }
                .sortedByDescending { it.count }
        )
        override fun getTotalCount(): Flow<Int> = flowOf(records.size)
        override fun getTodayCount(sinceTimestamp: Long): Flow<Int> = flowOf(records.count { it.timestamp >= sinceTimestamp })
        override fun getNotificationTimestamps(sinceTimestamp: Long, untilTimestamp: Long): Flow<List<Long>> =
            flowOf(records.filter { it.timestamp >= sinceTimestamp && it.timestamp < untilTimestamp }.map { it.timestamp })
        override fun observeById(id: Long): Flow<NotificationRecord?> = flowOf(records.find { it.id == id })
        override suspend fun getCleanupBatch(afterId: Long): List<NotificationRecord> =
            records.filter { it.id > afterId }.sortedBy { it.id }.take(200)
        override suspend fun updateStarred(id: Long, isStarred: Boolean): Int = 1
        override suspend fun updateReadStatus(id: Long, isRead: Boolean): Int = 1
        override suspend fun deleteById(id: Long): Int = if (records.removeAll { it.id == id }) 1 else 0
        override suspend fun deleteAll(): Int = 0
        override suspend fun deleteOlderThan(threshold: Long): Int {
            val before = records.size
            records.removeAll { it.timestamp < threshold && !it.isStarred }
            return before - records.size
        }
    }

    private val event = NotificationRecord(packageName = "chat", appName = "Chat",
        notificationKey = "0|chat|1", title = "Alice", content = "Hello", timestamp = 10_000L)

    @Test fun repeatedDeliveryReturnsExistingId() = runBlocking {
        val dao = FakeNotificationDao()
        val repo = NotificationRepositoryImpl(dao)
        val id = repo.insertNotification(event)
        assertEquals(id, repo.insertNotification(event))
        assertEquals(1, dao.records.size)
    }

    @Test fun separateKeysWithIdenticalContentArePreserved() = runBlocking {
        val dao = FakeNotificationDao()
        val repo = NotificationRepositoryImpl(dao)
        repo.insertNotification(event)
        repo.insertNotification(event.copy(notificationKey = "0|chat|2"))
        assertEquals(2, dao.records.size)
    }

    @Test fun repostsAndContentUpdatesArePreserved() = runBlocking {
        val dao = FakeNotificationDao()
        val repo = NotificationRepositoryImpl(dao)
        repo.insertNotification(event)
        repo.insertNotification(event.copy(timestamp = 10_001L))
        repo.insertNotification(event.copy(content = "Changed"))
        repo.insertNotification(event.copy(bigText = "Expanded"))
        repo.insertNotification(event.copy(subText = "Other account"))
        assertEquals(5, dao.records.size)
    }

    @Test fun unknownIdentityNeverDropsHistory() = runBlocking {
        val dao = FakeNotificationDao()
        val repo = NotificationRepositoryImpl(dao)
        repeat(2) { repo.insertNotification(event.copy(notificationKey = null)) }
        assertEquals(2, dao.records.size)
    }
    @Test fun retentionUsesCurrentPolicyAndKeepsStarsAndBoundary() = runBlocking {
        val day = 86_400_000L
        val dao = FakeNotificationDao()
        val repo = NotificationRepositoryImpl(dao, clock = { 40 * day })
        dao.insertAll(listOf(
            event.copy(timestamp = 9 * day),
            event.copy(timestamp = 9 * day, isStarred = true),
            event.copy(timestamp = 10 * day),
            event.copy(timestamp = 33 * day)
        ))
        assertEquals(1, repo.purgeOlderThan(30))
        assertEquals(3, dao.records.size)
        assertEquals(1, repo.purgeOlderThan(7))
        assertEquals(2, dao.records.size)
        assertEquals(1, dao.records.count { it.isStarred })
        assertEquals(1, dao.records.count { it.timestamp == 33 * day })
    }

    @Test fun authenticationCodesAreRejectedAndLegacySavedCodesAreRemoved() = runBlocking {
        val dao = FakeNotificationDao()
        val repo = NotificationRepositoryImpl(dao)
        assertEquals(-1L, repo.insertNotification(event.copy(content = "Your OTP is 123456")))
        assertEquals(0, dao.records.size)
        // Simulate data captured by an older version, including a saved code.
        repeat(205) { dao.insertNotification(event.copy(content = "Verification code: 123456", isStarred = true)) }
        repo.insertNotification(event)
        assertEquals(205, repo.purgeAuthenticationCodes())
        assertEquals(listOf("Hello"), dao.records.map { it.content })
    }

}
