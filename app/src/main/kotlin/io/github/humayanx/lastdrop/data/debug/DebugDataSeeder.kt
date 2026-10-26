package io.github.humayanx.lastdrop.data.debug

import io.github.humayanx.lastdrop.data.local.NotificationDao
import io.github.humayanx.lastdrop.data.local.entity.NotificationRecord
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

object DebugDataSeeder {

    suspend fun seedIfEmpty(dao: NotificationDao) {
        val count = dao.getTotalCount().first()
        if (count == 0) {
            seed(dao)
        }
    }

    suspend fun seed(dao: NotificationDao) {
        val now = System.currentTimeMillis()
        val dummyRecords = listOf(
            // TODAY NOTIFICATIONS
            NotificationRecord(
                packageName = "com.github.android",
                appName = "GitHub",
                title = "Pull Request #42 Merged",
                content = "humayanx/lastdrop: Modernize notification ledger UI",
                bigText = "Pull Request #42 'Modernize notification ledger UI' was approved by octocat and merged into main.",
                subText = "Repository: lastdrop",
                timestamp = now - TimeUnit.MINUTES.toMillis(12),
                isRead = false,
                isStarred = true,
                notificationKey = "key_gh_1"
            ),
            NotificationRecord(
                packageName = "com.Slack",
                appName = "Slack",
                title = "#mobile-team · Sarah Chen",
                content = "Updated the Figma mockups for the new bottom nav bar!",
                bigText = "Hey everyone! I just updated the Figma mockups with the new floating bottom navigation bar and token specs. Please take a look when you have a moment.",
                subText = "#mobile-team",
                timestamp = now - TimeUnit.MINUTES.toMillis(35),
                isRead = false,
                isStarred = true,
                notificationKey = "key_slack_1"
            ),
            NotificationRecord(
                packageName = "com.whatsapp",
                appName = "WhatsApp",
                title = "Mom",
                content = "Let me know when you reach home safely!",
                bigText = "Let me know when you reach home safely! Dinner will be ready at 8 PM.",
                subText = null,
                timestamp = now - TimeUnit.MINUTES.toMillis(50),
                isRead = true,
                isStarred = false,
                notificationKey = "key_wa_1"
            ),
            NotificationRecord(
                packageName = "com.google.android.gm",
                appName = "Gmail",
                title = "Flight Booking Confirmation",
                content = "Your flight to San Francisco (SFO) is confirmed. Reservation: QW789X.",
                bigText = "Booking reference: QW789X. Departure: Nov 12, Terminal 2. Check-in opens 24 hours prior to departure.",
                subText = "travel@airlines.com",
                timestamp = now - TimeUnit.HOURS.toMillis(1),
                isRead = true,
                isStarred = true,
                notificationKey = "key_gmail_1"
            ),
            NotificationRecord(
                packageName = "com.spotify.music",
                appName = "Spotify",
                title = "New Release Radar",
                content = "Check out the latest tracks from your favorite artists this week.",
                bigText = "New music from Tycho, Bonobo, and Ólafur Arnalds is waiting for you in Release Radar.",
                subText = "Release Radar",
                timestamp = now - TimeUnit.HOURS.toMillis(2),
                isRead = true,
                isStarred = false,
                notificationKey = "key_spotify_1"
            ),
            NotificationRecord(
                packageName = "com.whatsapp",
                appName = "WhatsApp",
                title = "Alex & Friends (3)",
                content = "David: Are we meeting at the coffee shop at 4?",
                bigText = "David: Are we meeting at the coffee shop at 4? I'll bring the design samples.",
                subText = "Group",
                timestamp = now - TimeUnit.HOURS.toMillis(2) - TimeUnit.MINUTES.toMillis(15),
                isRead = false,
                isStarred = false,
                notificationKey = "key_wa_2"
            ),
            NotificationRecord(
                packageName = "org.telegram.messenger",
                appName = "Telegram",
                title = "Kotlin Developers",
                content = "Jetpack Compose 1.8 Release Notes & Architecture Guide",
                bigText = "Check out the latest improvements in Compose Multiplatform, compiler metrics, and performance optimizations.",
                subText = "Telegram Channel",
                timestamp = now - TimeUnit.HOURS.toMillis(3),
                isRead = false,
                isStarred = true,
                notificationKey = "key_tg_1"
            ),
            NotificationRecord(
                packageName = "com.Slack",
                appName = "Slack",
                title = "#announcements · Dave Miller",
                content = "Deploy to production scheduled for 5:00 PM UTC.",
                bigText = "Reminder: v1.2.0 deployment window starts at 5:00 PM UTC. Minor maintenance window expected.",
                subText = "#announcements",
                timestamp = now - TimeUnit.HOURS.toMillis(3) - TimeUnit.MINUTES.toMillis(30),
                isRead = true,
                isStarred = false,
                notificationKey = "key_slack_2"
            ),
            NotificationRecord(
                packageName = "com.whatsapp",
                appName = "WhatsApp",
                title = "Emma",
                content = "Sent you the document over email.",
                bigText = "Sent you the document over email. Let me know if you need any adjustments before the client meeting.",
                subText = null,
                timestamp = now - TimeUnit.HOURS.toMillis(4),
                isRead = true,
                isStarred = false,
                notificationKey = "key_wa_3"
            ),
            NotificationRecord(
                packageName = "com.google.android.gm",
                appName = "Gmail",
                title = "Stripe",
                content = "Payment confirmation: Monthly Subscription renewed.",
                bigText = "Your invoice #INV-2026-10 has been processed successfully. Total: $29.00 USD.",
                subText = "receipts@stripe.com",
                timestamp = now - TimeUnit.HOURS.toMillis(4) - TimeUnit.MINUTES.toMillis(20),
                isRead = true,
                isStarred = false,
                notificationKey = "key_gmail_2"
            ),
            NotificationRecord(
                packageName = "com.twitter.android",
                appName = "X",
                title = "Android Developers @AndroidDev",
                content = "What's new in Android Studio: Profiler and layout inspector upgrades.",
                bigText = "Explore new real-time layout inspection tools, live preview rendering, and deep memory diagnostics.",
                subText = null,
                timestamp = now - TimeUnit.HOURS.toMillis(5),
                isRead = false,
                isStarred = false,
                notificationKey = "key_x_1"
            ),
            NotificationRecord(
                packageName = "com.github.android",
                appName = "GitHub",
                title = "Issue #18 assigned to you",
                content = "Investigate SQLite index performance on large datasets",
                bigText = "octocat assigned you to issue #18: Investigate SQLite index performance on large datasets in lastdrop.",
                subText = "humayanx/lastdrop",
                timestamp = now - TimeUnit.HOURS.toMillis(6),
                isRead = true,
                isStarred = false,
                notificationKey = "key_gh_2"
            ),

            // YESTERDAY (PAST DAY 1)
            NotificationRecord(
                packageName = "com.Slack",
                appName = "Slack",
                title = "#general · Lisa Wang",
                content = "Welcome our new team member, Kevin!",
                bigText = "Please give a warm welcome to Kevin who is joining us as an Android engineer!",
                subText = "#general",
                timestamp = now - TimeUnit.DAYS.toMillis(1) - TimeUnit.HOURS.toMillis(2),
                isRead = true,
                isStarred = false,
                notificationKey = "key_slack_y1"
            ),
            NotificationRecord(
                packageName = "com.whatsapp",
                appName = "WhatsApp",
                title = "Family Group (8)",
                content = "Grandma: Happy Sunday everyone!",
                bigText = "Grandma: Happy Sunday everyone! Hope you all have a wonderful week ahead.",
                subText = "Family Group",
                timestamp = now - TimeUnit.DAYS.toMillis(1) - TimeUnit.HOURS.toMillis(5),
                isRead = true,
                isStarred = false,
                notificationKey = "key_wa_y1"
            ),
            NotificationRecord(
                packageName = "com.github.android",
                appName = "GitHub",
                title = "Release v1.1.0 published",
                content = "lastdrop v1.1.0 is now live on GitHub Releases",
                bigText = "Release v1.1.0 includes major performance improvements and Material 3 design enhancements.",
                subText = "humayanx/lastdrop",
                timestamp = now - TimeUnit.DAYS.toMillis(1) - TimeUnit.HOURS.toMillis(7),
                isRead = true,
                isStarred = true,
                notificationKey = "key_gh_y1"
            ),
            NotificationRecord(
                packageName = "com.google.android.gm",
                appName = "Gmail",
                title = "Cloudflare",
                content = "Security alert: SSL certificate auto-renewed successfully.",
                bigText = "Your Universal SSL certificate has been renewed for the next 12 months with no downtime.",
                subText = "alerts@cloudflare.com",
                timestamp = now - TimeUnit.DAYS.toMillis(1) - TimeUnit.HOURS.toMillis(10),
                isRead = true,
                isStarred = false,
                notificationKey = "key_gm_y1"
            ),

            // 2 DAYS AGO (PAST DAY 2)
            NotificationRecord(
                packageName = "org.telegram.messenger",
                appName = "Telegram",
                title = "Tech Crunch Daily",
                content = "Top breakthrough AI developer tools this quarter",
                bigText = "Read how autonomous coding agents and on-device machine learning models are shaping app development.",
                subText = "News",
                timestamp = now - TimeUnit.DAYS.toMillis(2) - TimeUnit.HOURS.toMillis(3),
                isRead = true,
                isStarred = false,
                notificationKey = "key_tg_d2"
            ),
            NotificationRecord(
                packageName = "com.spotify.music",
                appName = "Spotify",
                title = "Daily Mix 1",
                content = "Featuring electronic, ambient, and downtempo favorites",
                bigText = "Your Daily Mix 1 is updated with new music based on your recent listening habits.",
                subText = "Daily Mix",
                timestamp = now - TimeUnit.DAYS.toMillis(2) - TimeUnit.HOURS.toMillis(6),
                isRead = true,
                isStarred = false,
                notificationKey = "key_spot_d2"
            ),
            NotificationRecord(
                packageName = "com.twitter.android",
                appName = "X",
                title = "Kotlin Language @kotlin",
                content = "Kotlin 2.1 is officially here! Check out the compiler performance gains.",
                bigText = "Kotlin 2.1 brings faster incremental compilation, improved K2 compiler stability, and multiplatform refinements.",
                subText = null,
                timestamp = now - TimeUnit.DAYS.toMillis(2) - TimeUnit.HOURS.toMillis(9),
                isRead = true,
                isStarred = true,
                notificationKey = "key_x_d2"
            ),

            // 4 DAYS AGO (PAST DAY 4)
            NotificationRecord(
                packageName = "com.google.android.youtube",
                appName = "YouTube",
                title = "Android Worldwide Conference",
                content = "Keynote Livestream: The future of Android UI with Jetpack Compose",
                bigText = "Join the live streaming session discussing deep architectural patterns and layout optimization.",
                subText = "Live Event",
                timestamp = now - TimeUnit.DAYS.toMillis(4) - TimeUnit.HOURS.toMillis(4),
                isRead = true,
                isStarred = false,
                notificationKey = "key_yt_d4"
            ),
            NotificationRecord(
                packageName = "com.whatsapp",
                appName = "WhatsApp",
                title = "Michael Brown",
                content = "Check out this article on SQLite FTS4 performance tuning.",
                bigText = "Found this great article on tuning SQLite virtual tables and index sizing in Android.",
                subText = null,
                timestamp = now - TimeUnit.DAYS.toMillis(4) - TimeUnit.HOURS.toMillis(8),
                isRead = true,
                isStarred = false,
                notificationKey = "key_wa_d4"
            )
        )
        dao.insertAll(dummyRecords)
    }
}
