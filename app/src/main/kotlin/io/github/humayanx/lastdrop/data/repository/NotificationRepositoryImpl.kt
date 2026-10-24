package io.github.humayanx.lastdrop.data.repository

import io.github.humayanx.lastdrop.data.local.NotificationDao
import io.github.humayanx.lastdrop.data.local.entity.NotificationRecord
import io.github.humayanx.lastdrop.data.local.model.AppNotificationCount
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import java.time.ZoneId
import io.github.humayanx.lastdrop.service.AuthenticationCodeFilter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext

class NotificationRepositoryImpl(
    private val dao: NotificationDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val clock: () -> Long = System::currentTimeMillis
) : NotificationRepository {

    override val allNotifications: Flow<List<NotificationRecord>> = dao.getAllNotifications().withoutAuthenticationCodes()

    override val starredNotifications: Flow<List<NotificationRecord>> = dao.getStarredNotifications().withoutAuthenticationCodes()

    override val distinctAppNames: Flow<List<String>> = dao.getDistinctAppNames()

    override val appNotificationCounts: Flow<List<AppNotificationCount>> = dao.getAppNotificationCounts()

    override val totalCount: Flow<Int> = dao.getTotalCount()

    override fun getTopSpamApps(sinceTimestamp: Long) = dao.getTopSpamApps(sinceTimestamp)

    override fun getTodayCount(sinceTimestamp: Long): Flow<Int> = dao.getTodayCount(sinceTimestamp)

    override fun getHourlyDistribution(sinceTimestamp: Long, untilTimestamp: Long, zone: ZoneId) =
        dao.getNotificationTimestamps(sinceTimestamp, untilTimestamp)
            .map { hourlyCounts(it, zone) }.flowOn(ioDispatcher)

    override fun observeById(id: Long) = dao.observeById(id).map { it?.takeUnless(AuthenticationCodeFilter::matches) }

    override fun getNotificationsByPackage(packageName: String): Flow<List<NotificationRecord>> {
        return dao.getNotificationsByPackage(packageName).withoutAuthenticationCodes()
    }

    override fun getNotificationsByAppName(appName: String): Flow<List<NotificationRecord>> {
        return dao.getNotificationsByAppName(appName).withoutAuthenticationCodes()
    }

    override fun searchNotifications(query: String): Flow<List<NotificationRecord>> {
        val ftsQuery = buildFtsQuery(query)
        return if (ftsQuery.isEmpty()) flowOf(emptyList()) else dao.searchNotifications(ftsQuery).withoutAuthenticationCodes()
    }

    private fun Flow<List<NotificationRecord>>.withoutAuthenticationCodes() =
        map { records -> records.filterNot(AuthenticationCodeFilter::matches) }.flowOn(ioDispatcher)

    companion object {
        fun sanitizeQuery(query: String): String =
            Regex("[\\p{L}\\p{M}\\p{N}_]+").findAll(query).joinToString(" ") { it.value }

        // FTS4 prefix terms inside quotes keep AND/OR/NOT and punctuation literal.
        fun buildFtsQuery(query: String): String = sanitizeQuery(query)
            .split(" ").filter { it.isNotEmpty() }.joinToString(" ") { "\"$it*\"" }
    }

    override suspend fun insertNotification(record: NotificationRecord): Long = withContext(ioDispatcher) {
        if (AuthenticationCodeFilter.matches(record)) return@withContext -1L
        dao.insertUnlessDuplicate(record)
    }

    override suspend fun toggleStar(id: Long, isStarred: Boolean) = withContext(ioDispatcher) {
        dao.updateStarred(id, isStarred)
        Unit
    }

    override suspend fun markAsRead(id: Long, isRead: Boolean) = withContext(ioDispatcher) {
        dao.updateReadStatus(id, isRead)
        Unit
    }

    override suspend fun deleteById(id: Long) = withContext(ioDispatcher) {
        dao.deleteById(id)
        Unit
    }

    override suspend fun deleteAll() = withContext(ioDispatcher) {
        dao.deleteAll()
        Unit
    }

    override suspend fun purgeOlderThan(retentionDays: Int): Int = withContext(ioDispatcher) {
        if (retentionDays <= 0) return@withContext 0
        val cutoff = clock() - (retentionDays.toLong() * 24L * 60L * 60L * 1000L)
        dao.deleteOlderThan(cutoff)
    }

    override suspend fun cleanupDuplicates(): Int = withContext(ioDispatcher) {
        dao.deleteExistingDuplicates()
    }
    override suspend fun purgeAuthenticationCodes(): Int = withContext(ioDispatcher) {
        var afterId = 0L
        var deleted = 0
        while (true) {
            val batch = dao.getCleanupBatch(afterId)
            if (batch.isEmpty()) break
            batch.filter(AuthenticationCodeFilter::matches).forEach { deleted += dao.deleteById(it.id) }
            afterId = batch.last().id
        }
        deleted
    }

}
