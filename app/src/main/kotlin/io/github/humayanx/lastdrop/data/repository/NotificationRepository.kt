package io.github.humayanx.lastdrop.data.repository

import io.github.humayanx.lastdrop.data.local.entity.NotificationRecord
import io.github.humayanx.lastdrop.data.local.model.AppNotificationCount
import io.github.humayanx.lastdrop.data.local.model.HourlyNotificationCount
import java.time.ZoneId
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    val allNotifications: Flow<List<NotificationRecord>>
    val starredNotifications: Flow<List<NotificationRecord>>
    val distinctAppNames: Flow<List<String>>
    val appNotificationCounts: Flow<List<AppNotificationCount>>
    val totalCount: Flow<Int>

    fun getTopSpamApps(sinceTimestamp: Long): Flow<List<AppNotificationCount>>
    fun getTodayCount(sinceTimestamp: Long): Flow<Int>
    fun getHourlyDistribution(sinceTimestamp: Long, untilTimestamp: Long, zone: ZoneId): Flow<List<HourlyNotificationCount>>
    fun observeById(id: Long): Flow<NotificationRecord?>
    fun getNotificationsByPackage(packageName: String): Flow<List<NotificationRecord>>
    fun getNotificationsByAppName(appName: String): Flow<List<NotificationRecord>>
    fun searchNotifications(query: String): Flow<List<NotificationRecord>>

    suspend fun insertNotification(record: NotificationRecord): Long
    suspend fun toggleStar(id: Long, isStarred: Boolean)
    suspend fun markAsRead(id: Long, isRead: Boolean)
    suspend fun deleteById(id: Long)
    suspend fun deleteAll()
    suspend fun purgeOlderThan(retentionDays: Int): Int
    suspend fun cleanupDuplicates(): Int
    suspend fun purgeAuthenticationCodes(): Int
}
