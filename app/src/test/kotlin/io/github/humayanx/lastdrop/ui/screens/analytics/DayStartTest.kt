package io.github.humayanx.lastdrop.ui.screens.analytics

import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class DayStartTest {
    @Test fun usesLocalMidnightInIndia() {
        assertEquals(Instant.parse("2026-10-23T18:30:00Z").toEpochMilli(),
            startOfDay(Instant.parse("2026-10-24T10:00:00Z"), ZoneId.of("Asia/Kolkata")))
    }
    @Test fun handlesDaylightSavingTransition() {
        assertEquals(Instant.parse("2026-03-08T05:00:00Z").toEpochMilli(),
            startOfDay(Instant.parse("2026-03-08T18:00:00Z"), ZoneId.of("America/New_York")))
    }
    @Test fun advancesAtMidnight() {
        assertEquals(Instant.parse("2026-10-24T18:30:00Z").toEpochMilli(),
            startOfDay(Instant.parse("2026-10-24T18:30:00Z"), ZoneId.of("Asia/Kolkata")))
    }
}
