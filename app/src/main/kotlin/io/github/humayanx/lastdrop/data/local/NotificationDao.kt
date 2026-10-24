package io.github.humayanx.lastdrop.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Transaction
import androidx.room.Query
import io.github.humayanx.lastdrop.data.local.entity.NotificationRecord
import io.github.humayanx.lastdrop.data.local.model.AppNotificationCount
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(record: NotificationRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<NotificationRecord>)

    @Query("""
        SELECT id FROM notifications
        WHERE notificationKey = :notificationKey AND packageName = :packageName
            AND timestamp = :timestamp AND title IS :title AND content IS :content
            AND bigText IS :bigText AND subText IS :subText
        ORDER BY id DESC LIMIT 1
    """)
    suspend fun findDuplicateId(
        notificationKey: String, packageName: String, timestamp: Long,
        title: String?, content: String?, bigText: String?, subText: String?
    ): Long?

    // Only repeated delivery of the same event is a duplicate. New posts and changed
    // payloads are history, even when their notification key or visible text repeats.
    @Transaction
    suspend fun insertUnlessDuplicate(record: NotificationRecord): Long {
        val key = record.notificationKey ?: return insertNotification(record)
        return findDuplicateId(key, record.packageName, record.timestamp,
            record.title, record.content, record.bigText, record.subText)
            ?: insertNotification(record)
    }

    @Query("""
        DELETE FROM notifications
        WHERE notificationKey IS NOT NULL AND isStarred = 0 AND id NOT IN (
            SELECT MIN(id) FROM notifications
            GROUP BY notificationKey, packageName, title, content, bigText, subText, timestamp
        )
    """)
    suspend fun deleteExistingDuplicates(): Int

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationRecord>>

    @Query("SELECT * FROM notifications WHERE isStarred = 1 ORDER BY timestamp DESC")
    fun getStarredNotifications(): Flow<List<NotificationRecord>>

    @Query("SELECT * FROM notifications WHERE packageName = :packageName ORDER BY timestamp DESC")
    fun getNotificationsByPackage(packageName: String): Flow<List<NotificationRecord>>

    @Query("SELECT * FROM notifications WHERE appName = :appName ORDER BY timestamp DESC")
    fun getNotificationsByAppName(appName: String): Flow<List<NotificationRecord>>

    @Query("""
        SELECT notifications.* FROM notifications
        JOIN notifications_fts ON notifications.id = notifications_fts.rowid
        WHERE notifications_fts MATCH :ftsQuery
        ORDER BY notifications.timestamp DESC
    """)
    fun searchNotifications(ftsQuery: String): Flow<List<NotificationRecord>>

    @Query("SELECT DISTINCT appName FROM notifications ORDER BY appName ASC")
    fun getDistinctAppNames(): Flow<List<String>>

    @Query("""
        SELECT packageName, appName, COUNT(*) AS count
        FROM notifications
        GROUP BY packageName
        ORDER BY count DESC
    """)
    fun getAppNotificationCounts(): Flow<List<AppNotificationCount>>

    @Query("""
        SELECT packageName, MAX(appName) AS appName, COUNT(id) AS count
        FROM notifications
        WHERE timestamp >= :sinceTimestamp
        GROUP BY packageName
        ORDER BY count DESC, packageName ASC
    """)
    fun getTopSpamApps(sinceTimestamp: Long): Flow<List<AppNotificationCount>>

    @Query("SELECT COUNT(*) FROM notifications")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM notifications WHERE timestamp >= :sinceTimestamp")
    fun getTodayCount(sinceTimestamp: Long): Flow<Int>

    @Query("SELECT timestamp FROM notifications WHERE timestamp >= :sinceTimestamp AND timestamp < :untilTimestamp")
    fun getNotificationTimestamps(sinceTimestamp: Long, untilTimestamp: Long): Flow<List<Long>>

    @Query("SELECT * FROM notifications WHERE id = :id")
    fun observeById(id: Long): Flow<NotificationRecord?>

    @Query("SELECT * FROM notifications WHERE id > :afterId ORDER BY id LIMIT 200")
    suspend fun getCleanupBatch(afterId: Long): List<NotificationRecord>

    @Query("UPDATE notifications SET isStarred = :isStarred WHERE id = :id")
    suspend fun updateStarred(id: Long, isStarred: Boolean): Int

    @Query("UPDATE notifications SET isRead = :isRead WHERE id = :id")
    suspend fun updateReadStatus(id: Long, isRead: Boolean): Int

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("DELETE FROM notifications")
    suspend fun deleteAll(): Int

    @Query("DELETE FROM notifications WHERE timestamp < :threshold AND isStarred = 0")
    suspend fun deleteOlderThan(threshold: Long): Int
}
