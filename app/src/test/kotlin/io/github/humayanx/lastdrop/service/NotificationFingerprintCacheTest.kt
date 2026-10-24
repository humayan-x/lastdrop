package io.github.humayanx.lastdrop.service

import io.github.humayanx.lastdrop.data.local.entity.NotificationRecord
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class NotificationFingerprintCacheTest {
    private val record = NotificationRecord(packageName = "chat", appName = "Chat",
        notificationKey = "chat|1", title = "Alice", content = "Hello", timestamp = 1L)

    @Test fun repeatedDeliveryUsesSlidingWindow() = runBlocking {
        var now = 0L
        var writes = 0
        val cache = NotificationFingerprintCache { now }
        cache.saveUnlessDuplicate(record) { writes++ }
        for (i in 1..10) {
            now += 2_000L
            cache.saveUnlessDuplicate(record) { writes++ }
        }
        assertEquals(1, writes)
        now += 3_000L
        cache.saveUnlessDuplicate(record) { writes++ }
        assertEquals(2, writes)
    }

    @Test fun distinctAppsSendersAndBodiesAreKept() = runBlocking {
        val cache = NotificationFingerprintCache { 0L }
        var writes = 0
        listOf(record, record.copy(packageName = "mail"), record.copy(title = "Bob"),
            record.copy(content = "Bye")).forEach { cache.saveUnlessDuplicate(it) { writes++ } }
        assertEquals(4, writes)
    }

    @Test fun distinctKeysRepostsAndUpdatesAreNotSuppressed() = runBlocking {
        val cache = NotificationFingerprintCache { 0L }
        var writes = 0
        listOf(record, record.copy(notificationKey = "chat|2"), record.copy(timestamp = 2L),
            record.copy(bigText = "Expanded"), record.copy(subText = "Account 2"),
            record.copy(content = "Updated"), record.copy(notificationKey = null),
            record.copy(notificationKey = null)).forEach {
            cache.saveUnlessDuplicate(it) { writes++ }
        }
        assertEquals(8, writes)
    }

    @Test fun concurrentCallbacksWriteOnce() = runBlocking {
        val cache = NotificationFingerprintCache { 0L }
        var writes = 0
        (1..20).map { async(kotlinx.coroutines.Dispatchers.Default) {
            cache.saveUnlessDuplicate(record) { writes++ }
        } }.awaitAll()
        assertEquals(1, writes)
    }

    @Test fun failedInsertCanBeRetried() = runBlocking {
        val cache = NotificationFingerprintCache { 0L }
        runCatching { cache.saveUnlessDuplicate(record) { error("Database unavailable") } }
        var writes = 0
        cache.saveUnlessDuplicate(record) { writes++ }
        assertEquals(1, writes)
    }

    @Test fun cacheIsBounded() = runBlocking {
        val cache = NotificationFingerprintCache { 0L }
        for (i in 0..200) cache.saveUnlessDuplicate(record.copy(title = "$i")) { }
        var writes = 0
        cache.saveUnlessDuplicate(record.copy(title = "0")) { writes++ }
        assertEquals(1, writes)
    }
}
