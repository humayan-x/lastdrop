package io.github.humayanx.lastdrop.data.debug

import io.github.humayanx.lastdrop.data.local.NotificationDao
import io.github.humayanx.lastdrop.data.local.entity.NotificationRecord
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.TimeUnit

object DebugDataSeeder {

    suspend fun seedIfEmpty(dao: NotificationDao) {
        val now = System.currentTimeMillis()
        val zone = ZoneId.systemDefault()
        val todayStart = Instant.ofEpochMilli(now).atZone(zone).toLocalDate().atStartOfDay(zone).toInstant().toEpochMilli()

        val totalCount = dao.getTotalCount().first()
        if (totalCount == 0) {
            seed(dao)
        } else {
            val todayCount = dao.getTodayCount(todayStart).first()
            if (todayCount == 0) {
                seedTodayOnly(dao, now, todayStart)
            }
        }
    }

    suspend fun seed(dao: NotificationDao) {
        val now = System.currentTimeMillis()
        val zone = ZoneId.systemDefault()
        val todayStart = Instant.ofEpochMilli(now).atZone(zone).toLocalDate().atStartOfDay(zone).toInstant().toEpochMilli()
        val allRecords = getTodayRecords(now, todayStart) + getPastRecords(todayStart)
        dao.insertAll(allRecords)
    }

    suspend fun seedTodayOnly(dao: NotificationDao, now: Long = System.currentTimeMillis(), todayStart: Long) {
        dao.insertAll(getTodayRecords(now, todayStart))
    }

    private fun calculateTodayTimestamps(now: Long, todayStart: Long): List<Long> {
        val elapsed = (now - todayStart).coerceAtLeast(TimeUnit.MINUTES.toMillis(1))
        val sixHours = TimeUnit.HOURS.toMillis(6)

        val idealOffsets = listOf(
            TimeUnit.MINUTES.toMillis(12),
            TimeUnit.MINUTES.toMillis(35),
            TimeUnit.MINUTES.toMillis(50),
            TimeUnit.HOURS.toMillis(1),
            TimeUnit.HOURS.toMillis(2),
            TimeUnit.HOURS.toMillis(2) + TimeUnit.MINUTES.toMillis(15),
            TimeUnit.HOURS.toMillis(3),
            TimeUnit.HOURS.toMillis(3) + TimeUnit.MINUTES.toMillis(30),
            TimeUnit.HOURS.toMillis(4),
            TimeUnit.HOURS.toMillis(4) + TimeUnit.MINUTES.toMillis(20),
            TimeUnit.HOURS.toMillis(5),
            TimeUnit.HOURS.toMillis(5) + TimeUnit.MINUTES.toMillis(45)
        )

        // If >= 6 hours have passed today, use the natural relative hour offsets
        if (elapsed >= sixHours) {
            return idealOffsets.map { offset -> (now - offset).coerceAtLeast(todayStart + 1_000L) }
        }

        // When testing near midnight / early morning, scale all 12 notifications into the elapsed window
        val usableWindow = (elapsed - TimeUnit.SECONDS.toMillis(10)).coerceAtLeast(TimeUnit.SECONDS.toMillis(10))
        val fractions = listOf(
            0.05, 0.12, 0.20, 0.28, 0.38, 0.46,
            0.55, 0.63, 0.72, 0.80, 0.88, 0.96
        )

        return fractions.map { fraction ->
            val offset = (usableWindow * fraction).toLong()
            (now - offset).coerceIn(todayStart + 1_000L, now)
        }
    }

    private fun getTodayRecords(now: Long, todayStart: Long): List<NotificationRecord> {
        val timestamps = calculateTodayTimestamps(now, todayStart)
        val suffix = System.currentTimeMillis()

        return listOf(
            NotificationRecord(
                packageName = "com.github.android",
                appName = "GitHub",
                title = "Pull Request #42 Merged",
                content = "humayanx/lastdrop: Modernize notification ledger UI",
                bigText = "Pull Request #42 'Modernize notification ledger UI' was approved by octocat and merged into main.",
                subText = "Repository: lastdrop",
                timestamp = timestamps[0],
                isRead = false,
                isStarred = true,
                notificationKey = "key_gh_1_$suffix"
            ),
            NotificationRecord(
                packageName = "com.Slack",
                appName = "Slack",
                title = "#mobile-team · Sarah Chen",
                content = "Updated the Figma mockups for the new bottom nav bar!",
                bigText = "Hey everyone! I just updated the Figma mockups with the new floating bottom navigation bar and token specs. Please take a look when you have a moment.",
                subText = "#mobile-team",
                timestamp = timestamps[1],
                isRead = false,
                isStarred = true,
                notificationKey = "key_slack_1_$suffix"
            ),
            NotificationRecord(
                packageName = "com.whatsapp",
                appName = "WhatsApp",
                title = "Mom",
                content = "Let me know when you reach home safely!",
                bigText = "Let me know when you reach home safely! Dinner will be ready at 8 PM.",
                subText = null,
                timestamp = timestamps[2],
                isRead = true,
                isStarred = false,
                notificationKey = "key_wa_1_$suffix"
            ),
            NotificationRecord(
                packageName = "com.google.android.gm",
                appName = "Gmail",
                title = "Flight Booking Confirmation",
                content = "Your flight to San Francisco (SFO) is confirmed. Reservation: QW789X.",
                bigText = "Booking reference: QW789X. Departure: Nov 12, Terminal 2. Check-in opens 24 hours prior to departure.",
                subText = "travel@airlines.com",
                timestamp = timestamps[3],
                isRead = true,
                isStarred = true,
                notificationKey = "key_gmail_1_$suffix"
            ),
            NotificationRecord(
                packageName = "com.spotify.music",
                appName = "Spotify",
                title = "New Release Radar",
                content = "Check out the latest tracks from your favorite artists this week.",
                bigText = "New music from Tycho, Bonobo, and Ólafur Arnalds is waiting for you in Release Radar.",
                subText = "Release Radar",
                timestamp = timestamps[4],
                isRead = true,
                isStarred = false,
                notificationKey = "key_spotify_1_$suffix"
            ),
            NotificationRecord(
                packageName = "com.whatsapp",
                appName = "WhatsApp",
                title = "Alex & Friends (3)",
                content = "David: Are we meeting at the coffee shop at 4?",
                bigText = "David: Are we meeting at the coffee shop at 4? I'll bring the design samples.",
                subText = "Group",
                timestamp = timestamps[5],
                isRead = false,
                isStarred = false,
                notificationKey = "key_wa_2_$suffix"
            ),
            NotificationRecord(
                packageName = "org.telegram.messenger",
                appName = "Telegram",
                title = "Kotlin Developers",
                content = "Jetpack Compose 1.8 Release Notes & Architecture Guide",
                bigText = "Check out the latest improvements in Compose Multiplatform, compiler metrics, and performance optimizations.",
                subText = "Telegram Channel",
                timestamp = timestamps[6],
                isRead = false,
                isStarred = true,
                notificationKey = "key_tg_1_$suffix"
            ),
            NotificationRecord(
                packageName = "com.Slack",
                appName = "Slack",
                title = "#announcements · Dave Miller",
                content = "Deploy to production scheduled for 5:00 PM UTC.",
                bigText = "Reminder: v1.2.0 deployment window starts at 5:00 PM UTC. Minor maintenance window expected.",
                subText = "#announcements",
                timestamp = timestamps[7],
                isRead = true,
                isStarred = false,
                notificationKey = "key_slack_2_$suffix"
            ),
            NotificationRecord(
                packageName = "com.whatsapp",
                appName = "WhatsApp",
                title = "Emma",
                content = "Sent you the document over email.",
                bigText = "Sent you the document over email. Let me know if you need any adjustments before the client meeting.",
                subText = null,
                timestamp = timestamps[8],
                isRead = true,
                isStarred = false,
                notificationKey = "key_wa_3_$suffix"
            ),
            NotificationRecord(
                packageName = "com.google.android.gm",
                appName = "Gmail",
                title = "Stripe",
                content = "Payment confirmation: Monthly Subscription renewed.",
                bigText = "Your invoice #INV-2026-10 has been processed successfully. Total: $29.00 USD.",
                subText = "receipts@stripe.com",
                timestamp = timestamps[9],
                isRead = true,
                isStarred = false,
                notificationKey = "key_gmail_2_$suffix"
            ),
            NotificationRecord(
                packageName = "com.twitter.android",
                appName = "X",
                title = "Android Developers @AndroidDev",
                content = "What's new in Android Studio: Profiler and layout inspector upgrades.",
                bigText = "Explore new real-time layout inspection tools, live preview rendering, and deep memory diagnostics.",
                subText = null,
                timestamp = timestamps[10],
                isRead = false,
                isStarred = false,
                notificationKey = "key_x_1_$suffix"
            ),
            NotificationRecord(
                packageName = "com.github.android",
                appName = "GitHub",
                title = "Issue #18 assigned to you",
                content = "Investigate SQLite index performance on large datasets",
                bigText = "octocat assigned you to issue #18: Investigate SQLite index performance on large datasets in lastdrop.",
                subText = "humayanx/lastdrop",
                timestamp = timestamps[11],
                isRead = true,
                isStarred = false,
                notificationKey = "key_gh_2_$suffix"
            )
        )
    }

    private fun getPastRecords(todayStart: Long): List<NotificationRecord> {
        val suffix = System.currentTimeMillis()
        return listOf(
            // YESTERDAY (PAST DAY 1)
            NotificationRecord(
                packageName = "com.Slack",
                appName = "Slack",
                title = "#general · Lisa Wang",
                content = "Welcome our new team member, Kevin!",
                bigText = "Please give a warm welcome to Kevin who is joining us as an Android engineer!",
                subText = "#general",
                timestamp = todayStart - TimeUnit.HOURS.toMillis(2),
                isRead = true,
                isStarred = false,
                notificationKey = "key_slack_y1_$suffix"
            ),
            NotificationRecord(
                packageName = "com.whatsapp",
                appName = "WhatsApp",
                title = "Family Group (8)",
                content = "Grandma: Happy Sunday everyone!",
                bigText = "Grandma: Happy Sunday everyone! Hope you all have a wonderful week ahead.",
                subText = "Family Group",
                timestamp = todayStart - TimeUnit.HOURS.toMillis(5),
                isRead = true,
                isStarred = false,
                notificationKey = "key_wa_y1_$suffix"
            ),
            NotificationRecord(
                packageName = "com.github.android",
                appName = "GitHub",
                title = "Release v1.2.0 published",
                content = "lastdrop v1.2.0 is now live on GitHub Releases",
                bigText = "Release v1.2.0 includes major performance improvements, solid color redesign, and Material 3 enhancements.",
                subText = "humayanx/lastdrop",
                timestamp = todayStart - TimeUnit.HOURS.toMillis(7),
                isRead = true,
                isStarred = true,
                notificationKey = "key_gh_y1_$suffix"
            ),
            NotificationRecord(
                packageName = "com.google.android.gm",
                appName = "Gmail",
                title = "Cloudflare",
                content = "Security alert: SSL certificate auto-renewed successfully.",
                bigText = "Your Universal SSL certificate has been renewed for the next 12 months with no downtime.",
                subText = "alerts@cloudflare.com",
                timestamp = todayStart - TimeUnit.HOURS.toMillis(10),
                isRead = true,
                isStarred = false,
                notificationKey = "key_gm_y1_$suffix"
            ),

            // 2 DAYS AGO (PAST DAY 2)
            NotificationRecord(
                packageName = "org.telegram.messenger",
                appName = "Telegram",
                title = "Tech Crunch Daily",
                content = "Top breakthrough AI developer tools this quarter",
                bigText = "Read how autonomous coding agents and on-device machine learning models are shaping app development.",
                subText = "News",
                timestamp = todayStart - TimeUnit.DAYS.toMillis(1) - TimeUnit.HOURS.toMillis(3),
                isRead = true,
                isStarred = false,
                notificationKey = "key_tg_d2_$suffix"
            ),
            NotificationRecord(
                packageName = "com.spotify.music",
                appName = "Spotify",
                title = "Daily Mix 1",
                content = "Featuring electronic, ambient, and downtempo favorites",
                bigText = "Your Daily Mix 1 is updated with new music based on your recent listening habits.",
                subText = "Daily Mix",
                timestamp = todayStart - TimeUnit.DAYS.toMillis(1) - TimeUnit.HOURS.toMillis(6),
                isRead = true,
                isStarred = false,
                notificationKey = "key_spot_d2_$suffix"
            ),
            NotificationRecord(
                packageName = "com.twitter.android",
                appName = "X",
                title = "Kotlin Language @kotlin",
                content = "Kotlin 2.1 is officially here! Check out the compiler performance gains.",
                bigText = "Kotlin 2.1 brings faster incremental compilation, improved K2 compiler stability, and multiplatform refinements.",
                subText = null,
                timestamp = todayStart - TimeUnit.DAYS.toMillis(1) - TimeUnit.HOURS.toMillis(9),
                isRead = true,
                isStarred = true,
                notificationKey = "key_x_d2_$suffix"
            ),

            // 4 DAYS AGO (PAST DAY 4)
            NotificationRecord(
                packageName = "com.google.android.youtube",
                appName = "YouTube",
                title = "Android Worldwide Conference",
                content = "Keynote Livestream: The future of Android UI with Jetpack Compose",
                bigText = "Join the live streaming session discussing deep architectural patterns and layout optimization.",
                subText = "Live Event",
                timestamp = todayStart - TimeUnit.DAYS.toMillis(3) - TimeUnit.HOURS.toMillis(4),
                isRead = true,
                isStarred = false,
                notificationKey = "key_yt_d4_$suffix"
            ),
            NotificationRecord(
                packageName = "com.whatsapp",
                appName = "WhatsApp",
                title = "Michael Brown",
                content = "Check out this article on SQLite FTS4 performance tuning.",
                bigText = "Found this great article on tuning SQLite virtual tables and index sizing in Android.",
                subText = null,
                timestamp = todayStart - TimeUnit.DAYS.toMillis(3) - TimeUnit.HOURS.toMillis(8),
                isRead = true,
                isStarred = false,
                notificationKey = "key_wa_d4_$suffix"
            )
        )
    }
}
