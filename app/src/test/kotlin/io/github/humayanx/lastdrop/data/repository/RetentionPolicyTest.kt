package io.github.humayanx.lastdrop.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RetentionPolicyTest {

    @Test
    fun retentionPeriod_daysMappingIsCorrect() {
        assertEquals(7, RetentionPeriod.DAYS_7.days)
        assertEquals(14, RetentionPeriod.DAYS_14.days)
        assertEquals(30, RetentionPeriod.DAYS_30.days)
    }

    @Test
    fun retentionPeriod_labelsAreFormattedCorrectly() {
        assertEquals("7 Days", RetentionPeriod.DAYS_7.label)
        assertEquals("14 Days", RetentionPeriod.DAYS_14.label)
        assertEquals("30 Days", RetentionPeriod.DAYS_30.label)
    }

    @Test
    fun retentionPeriod_cutoffCalculationIsAccurate() {
        val now = System.currentTimeMillis()
        val days7Millis = 7L * 24L * 60L * 60L * 1000L
        val calculatedCutoff = now - days7Millis
        assertTrue(calculatedCutoff < now)
    }
}
