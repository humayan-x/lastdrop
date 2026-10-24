package io.github.humayanx.lastdrop.service

import android.app.Notification
import android.os.SystemClock
import android.content.ComponentName
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import io.github.humayanx.lastdrop.LastDropApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Background ingestion engine intercepting status bar events.
 * Operates strictly on Dispatchers.IO to maintain smooth UI and low battery overhead.
 */
class LastDropListenerService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onListenerConnected() {
        super.onListenerConnected()
        _isServiceConnected.value = true
        Log.i(TAG, ">>> LastDropListenerService successfully CONNECTED to system NotificationManager <<<")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        _isServiceConnected.value = false
        Log.w(TAG, ">>> LastDropListenerService DISCONNECTED from system NotificationManager. Requesting rebind... <<<")
        runCatching {
            requestRebind(ComponentName(this, LastDropListenerService::class.java))
        }.onFailure { e ->
            Log.e(TAG, "Failed to request rebind in onListenerDisconnected", e)
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null || sbn.isOngoing) return
        if ((sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0) return

        val pkg = sbn.packageName ?: "unknown"
        Log.v(TAG, "onNotificationPosted: incoming from $pkg (id=${sbn.id}, isOngoing=${sbn.isOngoing})")

        serviceScope.launch {
            val record = NotificationParser.parse(applicationContext, sbn)
            if (record == null) {
                Log.d(TAG, "onNotificationPosted: discarded notification from $pkg (id=${sbn.id})")
                return@launch
            }

            runCatching {
                recentNotifications.saveUnlessDuplicate(record) {
                    LastDropApp.instance.notificationRepository.insertNotification(record)
                }
            }.onFailure { error ->
                Log.e(TAG, "Failed to insert notification into database")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _isServiceConnected.value = false
        Log.w(TAG, "LastDropListenerService destroyed")
        serviceScope.cancel()
    }

    companion object {
        private val recentNotifications = NotificationFingerprintCache(SystemClock::elapsedRealtime)
        private const val TAG = "LastDropListener"
        private val _isServiceConnected = MutableStateFlow(false)
        val isServiceConnected: StateFlow<Boolean> = _isServiceConnected.asStateFlow()
    }
}
