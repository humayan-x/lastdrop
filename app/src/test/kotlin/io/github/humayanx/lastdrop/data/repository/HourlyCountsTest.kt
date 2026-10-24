package io.github.humayanx.lastdrop.data.repository

import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class HourlyCountsTest {
    private val newYork = ZoneId.of("America/New_York")
    private fun counts(vararg times: String) = hourlyCounts(times.map { Instant.parse(it).toEpochMilli() }, newYork)

    @Test fun springForwardUsesClockHourAndKeepsLateEvening() {
        val result = counts("2026-03-08T06:30:00Z", "2026-03-08T07:30:00Z", "2026-03-09T03:30:00Z")
        assertEquals(1, result[1].count)
        assertEquals(0, result[2].count)
        assertEquals(1, result[3].count)
        assertEquals(1, result[23].count)
        assertEquals(3, result.sumOf { it.count })
    }

    @Test fun fallBackCombinesRepeatedHourWithoutDroppingHour23() {
        val result = counts("2026-11-01T05:30:00Z", "2026-11-01T06:30:00Z", "2026-11-02T04:30:00Z")
        assertEquals(2, result[1].count)
        assertEquals(1, result[23].count)
        assertEquals(3, result.sumOf { it.count })
    }

    @Test fun usesRequestedZoneIncludingFractionalOffsets() {
        val time = listOf(Instant.parse("2026-10-24T18:00:00Z").toEpochMilli())
        assertEquals(1, hourlyCounts(time, ZoneId.of("Asia/Kolkata"))[23].count)
        assertEquals(1, hourlyCounts(time, ZoneId.of("UTC"))[18].count)
    }
}
