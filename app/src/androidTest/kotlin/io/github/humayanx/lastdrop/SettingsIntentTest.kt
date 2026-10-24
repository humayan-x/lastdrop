package io.github.humayanx.lastdrop

import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.Settings
import androidx.test.platform.app.InstrumentationRegistry
import io.github.humayanx.lastdrop.service.PermissionManager
import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsIntentTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun batteryPromptSupportsActivityResult() {
        var request: Intent? = null
        PermissionManager.requestIgnoreBatteryOptimizations(context) { request = it }
        assertEquals(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, request?.action)
        assertEquals("package:${context.packageName}", request?.dataString)
        assertEquals(0, request!!.flags and Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    @Test
    fun batteryFallbackAlsoUsesTheResultLauncher() {
        val actions = mutableListOf<String?>()
        PermissionManager.requestIgnoreBatteryOptimizations(context) {
            actions.add(it.action)
            if (actions.size == 1) throw ActivityNotFoundException()
            assertEquals(0, it.flags and Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        assertEquals(listOf(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
            Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS), actions)
    }
}
