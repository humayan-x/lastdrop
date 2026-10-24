package io.github.humayanx.lastdrop.service

import android.app.Notification
import android.app.Person
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaMetadata
import android.media.session.MediaSessionManager
import android.os.Build
import android.os.Bundle
import android.service.notification.StatusBarNotification
import android.util.Log
import io.github.humayanx.lastdrop.data.local.entity.NotificationRecord
import java.util.concurrent.ConcurrentHashMap

object NotificationParser {

    private const val TAG = "LastDropParser"
    private val appLabelCache = ConcurrentHashMap<String, String>()

    /**
     * Parses a StatusBarNotification into a clean NotificationRecord.
     * Returns null for group summaries, ongoing/media noise, or LastDrop itself.
     */
    fun parse(context: Context, sbn: StatusBarNotification): NotificationRecord? {
        return runCatching {
            val packageName = sbn.packageName ?: return null

            // Discard self-generated notifications
            if (packageName == context.packageName) {
                Log.v(TAG, "Ignoring self notification: $packageName")
                return null
            }

            // Noise Filter: Silently discard ongoing/sticky alerts (navigation, foreground downloads)
            if (sbn.isOngoing) {
                Log.d(TAG, "Ignoring ongoing notification from $packageName (id: ${sbn.id})")
                return null
            }

            val notification = sbn.notification ?: return null

            // Summary containers repeat child messages, even when they include readable text.
            if ((notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0) return null

            // Media Filter: Silently discard active music/media playback notifications without blocking the app
            if (isMediaPlaybackNotification(context, sbn, notification)) {
                Log.d(TAG, "Ignoring active media playback notification from $packageName (id: ${sbn.id})")
                return null
            }

            val extras = notification.extras

            // Protect against ClassNotFoundException when unmarshalling custom parcelables
            extras?.classLoader = context.classLoader

            var title: String? = null
            var text: String? = null
            var bigText: String? = null
            var subText: String? = null

            if (extras != null) {
                title = extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE)?.toString()?.trim()
                    ?: extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim()
                    ?: extras.getCharSequence(Notification.EXTRA_TITLE_BIG)?.toString()?.trim()

                text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.trim()
                bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()?.trim()

                subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()?.trim()
                    ?: extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT)?.toString()?.trim()
                    ?: extras.getCharSequence(Notification.EXTRA_INFO_TEXT)?.toString()?.trim()
            }

            // Extract rich message formats (Crucial for Telegram, WhatsApp, Messenger, Gmail)
            val messagingText = extras?.let { extractMessagingStyleText(it) }
            val inboxText = extras?.let { extractInboxStyleLines(it) }

            // Priority: MessagingStyle conversation > InboxStyle lines > bigText > standard text
            val bestBody = messagingText ?: inboxText ?: bigText ?: text

            // Fallback to ticker text if standard bodies are absent
            val tickerText = notification.tickerText?.toString()?.trim()
            val finalContent = bestBody ?: subText ?: tickerText ?: if (!title.isNullOrEmpty()) "" else null

            // Discard only empty shell notifications with completely zero readable metadata
            if (title.isNullOrEmpty() && finalContent.isNullOrEmpty()) {
                Log.d(TAG, "Discarded empty shell notification from $packageName (id: ${sbn.id})")
                return null
            }

            // Inspect every parsed representation, not just the selected display body.
            if (AuthenticationCodeFilter.matches(title, text, bigText, subText,
                    messagingText, inboxText, tickerText, finalContent,
                    extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString(),
                    extras?.getCharSequence(Notification.EXTRA_TITLE_BIG)?.toString(),
                    extras?.getCharSequence(Notification.EXTRA_SUMMARY_TEXT)?.toString(),
                    extras?.getCharSequence(Notification.EXTRA_INFO_TEXT)?.toString())) return null

            val appName = resolveAppLabel(context.packageManager, packageName)
            val timestamp = if (sbn.postTime > 0) sbn.postTime else System.currentTimeMillis()


            NotificationRecord(
                notificationKey = sbn.key,
                packageName = packageName,
                appName = appName,
                title = title,
                content = finalContent,
                bigText = bigText ?: messagingText ?: inboxText,
                subText = subText,
                timestamp = timestamp
            )
        }.getOrElse { e ->
            Log.e(TAG, "Failed to parse notification")
            null
        }
    }

    private fun extractMessagingStyleText(extras: Bundle): String? {
        return runCatching {
            @Suppress("DEPRECATION")
            val messages = extras.getParcelableArray(Notification.EXTRA_MESSAGES) ?: return null
            if (messages.isEmpty()) return null

            val sb = StringBuilder()
            for (item in messages) {
                if (item is Bundle) {
                    val msgText = item.getCharSequence("text")?.toString()?.trim()
                    val sender = runCatching {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            @Suppress("DEPRECATION")
                            (item.getParcelable<Person>("person"))?.name?.toString()?.trim()
                        } else null
                    }.getOrNull() ?: item.getCharSequence("sender")?.toString()?.trim()

                    if (!msgText.isNullOrEmpty()) {
                        if (sb.isNotEmpty()) sb.append("\n")
                        if (!sender.isNullOrEmpty()) {
                            sb.append(sender).append(": ")
                        }
                        sb.append(msgText)
                    }
                }
            }
            sb.toString().ifEmpty { null }
        }.getOrNull()
    }

    private fun extractInboxStyleLines(extras: Bundle): String? {
        return runCatching {
            val lines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES) ?: return null
            val nonBlank = lines.mapNotNull { it?.toString()?.trim() }.filter { it.isNotEmpty() }
            if (nonBlank.isEmpty()) null else nonBlank.joinToString("\n")
        }.getOrNull()
    }

    private fun resolveAppLabel(pm: PackageManager, packageName: String): String {
        return appLabelCache.getOrPut(packageName) {
            runCatching {
                val appInfo = pm.getApplicationInfo(packageName, 0)
                pm.getApplicationLabel(appInfo).toString()
            }.getOrDefault(packageName.substringAfterLast('.'))
        }
    }

    /**
     * Determines whether a notification represents active media/music playback.
     * Identifies transport controls, MediaSession tokens, MediaStyle templates,
     * and active MediaSession playback metadata without blocking normal notifications from the same app.
     */
    fun isMediaPlaybackNotification(
        context: Context,
        sbn: StatusBarNotification,
        notification: Notification
    ): Boolean {
        // 1. Explicit transport category (designated by Android specifically for media playback transport controls)
        if (Notification.CATEGORY_TRANSPORT.equals(notification.category, ignoreCase = true)) {
            return true
        }

        val extras = notification.extras
        if (extras != null) {
            // 2. Bound MediaSession token (used by Android 11+ Quick Settings media controls)
            if (extras.containsKey(Notification.EXTRA_MEDIA_SESSION) || extras.containsKey("android.mediaSession")) {
                return true
            }

            // 3. MediaStyle template layout
            val template = extras.getString(Notification.EXTRA_TEMPLATE)
            if (template != null && template.contains("MediaStyle", ignoreCase = true)) {
                return true
            }

            if (extras.containsKey("android.mediaRemoteIntent")) {
                return true
            }
        }

        // 4. Cross-reference with active MediaSessionManager playback
        runCatching {
            val mediaSessionManager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager
            val componentName = ComponentName(context, LastDropListenerService::class.java)
            val controllers = mediaSessionManager?.getActiveSessions(componentName)
            if (!controllers.isNullOrEmpty()) {
                val title = extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim()
                val text = extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.trim()

                for (controller in controllers) {
                    if (controller.packageName == sbn.packageName) {
                        val metadata = controller.metadata
                        val mediaTitle = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE)
                        val mediaArtist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST)

                        // If the notification title or text matches the active track or artist, discard
                        if (!mediaTitle.isNullOrBlank() && (title.equals(mediaTitle, ignoreCase = true) || text.equals(mediaTitle, ignoreCase = true))) {
                            return true
                        }
                        if (!mediaArtist.isNullOrBlank() && (title.equals(mediaArtist, ignoreCase = true) || text.equals(mediaArtist, ignoreCase = true))) {
                            return true
                        }
                    }
                }
            }
        }

        return false
    }
}
