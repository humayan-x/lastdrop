package io.github.humayanx.lastdrop.data.repository

import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeModeTest {

    @Test
    fun themeMode_allValuesExist() {
        val modes = ThemeMode.entries.map { it.name }
        assertEquals(listOf("SYSTEM", "DARK", "LIGHT"), modes)
    }

    @Test
    fun themeMode_safeValueLookupFallback() {
        val mode = runCatching { ThemeMode.valueOf("INVALID_THEME") }.getOrDefault(ThemeMode.SYSTEM)
        assertEquals(ThemeMode.SYSTEM, mode)
    }
}
