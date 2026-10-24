package io.github.humayanx.lastdrop.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import io.github.humayanx.lastdrop.LastDropApp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class RetentionWorker(context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters) {

    override suspend fun doWork(): Result {
        return try {
            val app = applicationContext as LastDropApp
            val preferences = app.userPreferencesRepository.userPreferencesFlow.first()
            app.notificationRepository.purgeAuthenticationCodes()
            app.notificationRepository.purgeOlderThan(preferences.retentionPeriod.days)
            Result.success()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            Result.retry()
        }
    }

    companion object {
        fun schedule(context: Context) {
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "notification-retention",
                ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<RetentionWorker>(12, TimeUnit.HOURS).build()
            )
        }
    }
}
