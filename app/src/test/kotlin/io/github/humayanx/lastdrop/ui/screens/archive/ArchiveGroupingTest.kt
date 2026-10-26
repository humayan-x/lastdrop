package io.github.humayanx.lastdrop.ui.screens.archive

import io.github.humayanx.lastdrop.data.local.entity.NotificationRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import java.util.concurrent.TimeUnit

class ArchiveGroupingTest {

    @Test
    fun formatsArchiveDateItemWithShortDayAndFullDate() {
        val zone = ZoneId.of("UTC")
        val baseTime = Instant.parse("2026-10-26T12:00:00Z").toEpochMilli() // Monday

        val yesterday = baseTime - TimeUnit.DAYS.toMillis(1) // Sunday, Oct 25
        val twoDaysAgo = baseTime - TimeUnit.DAYS.toMillis(2) // Saturday, Oct 24

        val records = listOf(
            NotificationRecord(
                id = 1,
                packageName = "com.slack",
                appName = "Slack",
                title = "Msg 1",
                content = "Content 1",
                timestamp = yesterday
            ),
            NotificationRecord(
                id = 2,
                packageName = "com.whatsapp",
                appName = "WhatsApp",
                title = "Msg 2",
                content = "Content 2",
                timestamp = yesterday - TimeUnit.HOURS.toMillis(2)
            ),
            NotificationRecord(
                id = 3,
                packageName = "org.telegram",
                appName = "Telegram",
                title = "Msg 3",
                content = "Content 3",
                timestamp = twoDaysAgo
            )
        )

        val grouped = records.groupBy {
            Instant.ofEpochMilli(it.timestamp).atZone(zone).toLocalDate()
        }

        val fullDateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH)

        val cards = grouped.entries.sortedByDescending { it.key }.map { (date, recs) ->
            ArchiveDateItem(
                date = date,
                dayShortName = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH).uppercase(),
                dayFullName = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                fullDate = date.format(fullDateFormatter),
                relativeLabel = null,
                count = recs.size,
                notifications = recs
            )
        }

        assertEquals(2, cards.size)

        // Card 1: Sunday
        assertEquals("SUN", cards[0].dayShortName)
        assertEquals("Sunday", cards[0].dayFullName)
        assertEquals("25 October 2026", cards[0].fullDate)
        assertEquals(2, cards[0].count)
        assertEquals(2, cards[0].notifications.size)

        // Card 2: Saturday
        assertEquals("SAT", cards[1].dayShortName)
        assertEquals("Saturday", cards[1].dayFullName)
        assertEquals("24 October 2026", cards[1].fullDate)
        assertEquals(1, cards[1].count)
        assertEquals(1, cards[1].notifications.size)
    }

    @Test
    fun searchesByDateAndDayName() {
        val fullDateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH)
        val zone = ZoneId.of("UTC")
        val oct25 = Instant.parse("2026-10-25T12:00:00Z").atZone(zone).toLocalDate()
        val oct24 = Instant.parse("2026-10-24T12:00:00Z").atZone(zone).toLocalDate()

        val cards = listOf(
            ArchiveDateItem(
                date = oct25,
                dayShortName = "SUN",
                dayFullName = "Sunday",
                fullDate = oct25.format(fullDateFormatter), // "25 October 2026"
                relativeLabel = "YESTERDAY",
                count = 4,
                notifications = emptyList()
            ),
            ArchiveDateItem(
                date = oct24,
                dayShortName = "SAT",
                dayFullName = "Saturday",
                fullDate = oct24.format(fullDateFormatter), // "24 October 2026"
                relativeLabel = null,
                count = 3,
                notifications = emptyList()
            )
        )

        // Search by short day
        val sunResults = cards.filter { it.dayShortName.contains("SUN", ignoreCase = true) }
        assertEquals(1, sunResults.size)
        assertEquals("SUN", sunResults.first().dayShortName)

        // Search by day full name
        val satResults = cards.filter { it.dayFullName.contains("Saturday", ignoreCase = true) }
        assertEquals(1, satResults.size)
        assertEquals("SAT", satResults.first().dayShortName)

        // Search by day number in date
        val day25Results = cards.filter { it.fullDate.contains("25", ignoreCase = true) }
        assertEquals(1, day25Results.size)
        assertEquals("SUN", day25Results.first().dayShortName)

        // Search by month
        val octResults = cards.filter { it.fullDate.contains("October", ignoreCase = true) }
        assertEquals(2, octResults.size)
    }
}
