package io.github.humayanx.lastdrop

import android.app.Application
import io.github.humayanx.lastdrop.data.local.AppDatabase
import io.github.humayanx.lastdrop.data.repository.NotificationRepository
import io.github.humayanx.lastdrop.data.repository.NotificationRepositoryImpl
import io.github.humayanx.lastdrop.data.repository.UserPreferencesRepository
import io.github.humayanx.lastdrop.worker.RetentionWorker
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.CancellationException
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LastDropApp : Application() {

    val database: AppDatabase by lazy {
        AppDatabase.getInstance(this)
    }

    val notificationRepository: NotificationRepository by lazy {
        NotificationRepositoryImpl(database.notificationDao())
    }

    val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(this)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        RetentionWorker.schedule(this)
        // Catch up immediately whenever Android starts the app or its listener.
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val preferences = userPreferencesRepository.userPreferencesFlow.first()
                notificationRepository.purgeAuthenticationCodes()
                notificationRepository.purgeOlderThan(preferences.retentionPeriod.days)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                Log.w("LastDrop", "Startup retention cleanup failed; scheduled cleanup will retry")
            }
        }
    }

    companion object {
        lateinit var instance: LastDropApp
            private set
    }
}
