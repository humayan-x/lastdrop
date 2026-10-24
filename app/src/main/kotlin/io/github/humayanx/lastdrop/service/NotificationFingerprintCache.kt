package io.github.humayanx.lastdrop.service

import io.github.humayanx.lastdrop.data.local.entity.NotificationRecord
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Serializes duplicate checks with persistence; failed writes remain retryable. */
internal class NotificationFingerprintCache(private val clock: () -> Long) {
    private data class Fingerprint(
        val key: String, val packageName: String, val timestamp: Long,
        val title: String?, val content: String?, val bigText: String?, val subText: String?
    )
    private val recent = LinkedHashMap<Fingerprint, Long>()
    private val mutex = Mutex()

    suspend fun saveUnlessDuplicate(record: NotificationRecord, save: suspend () -> Unit): Boolean =
        mutex.withLock {
            val key = record.notificationKey
            if (key == null) {
                save()
                return@withLock true
            }
            val now = clock()
            recent.entries.removeAll { now - it.value >= WINDOW_MS }
            val fingerprint = Fingerprint(key, record.packageName, record.timestamp,
                record.title, record.content, record.bigText, record.subText)
            if (recent.containsKey(fingerprint)) {
                // Repeated delivery of this exact event does not create another row.
                recent.remove(fingerprint)
                recent[fingerprint] = now
                return@withLock false
            }
            save()
            recent[fingerprint] = clock()
            while (recent.size > MAX_ENTRIES) recent.remove(recent.keys.first())
            true
        }

    private companion object {
        const val WINDOW_MS = 3_000L
        const val MAX_ENTRIES = 200
    }
}
