package io.github.humayanx.lastdrop.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.os.PowerManager
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat

object PermissionManager {

    private const val TAG = "PermissionManager"
    private const val RECONNECT_INTERVAL_MS = 5_000L
    private var lastReconnectAt: Long? = null

    /**
     * Checks if notification listener access is granted for LastDrop.
     */
    fun isNotificationListenerEnabled(context: Context): Boolean {
        val enabledPackages = NotificationManagerCompat.getEnabledListenerPackages(context)
        if (enabledPackages.contains(context.packageName)) return true

        // Fallback check against Settings.Secure
        val flat = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        ) ?: return false

        val expectedComponent = ComponentName(context, LastDropListenerService::class.java).flattenToString()
        return flat.contains(expectedComponent) || flat.contains(context.packageName)
    }

    /**
     * Checks if the app is exempt from battery optimizations.
     * This reduces some power restrictions but does not guarantee process survival.
     */
    fun isBatteryOptimizationIgnored(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        return powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: true
    }

    /** Nudge a disconnected listener once per retry interval; binding is asynchronous. */
    @Synchronized
    fun ensureServiceBound(context: Context, force: Boolean = false) {
        if (!isNotificationListenerEnabled(context) ||
            LastDropListenerService.isServiceConnected.value
        ) return

        val now = SystemClock.elapsedRealtime()
        if (!force && lastReconnectAt?.let { now - it < RECONNECT_INTERVAL_MS } == true) return
        lastReconnectAt = now

        val componentName = ComponentName(context, LastDropListenerService::class.java)
        // Let Android manage the binding; cycling the component can interrupt delivery.
        runCatching {
            NotificationListenerService.requestRebind(componentName)
        }.onFailure { error ->
            Log.e(TAG, "Failed to request listener rebind", error)
        }
    }

    /**
     * Opens the system settings screen to grant Notification Access.
     */
    fun openNotificationListenerSettings(context: Context) {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        runCatching { context.startActivity(intent) }
    }

    /**
     * Opens the App Info details page in Settings (useful for Android 13+ 'Allow restricted settings').
     */
    fun openAppDetailsSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        runCatching { context.startActivity(intent) }
    }

    /** OEM activities are undocumented and may change; always fall back to App Info. */
    fun openAutostartSettings(context: Context) {
        val manufacturer = Build.MANUFACTURER.lowercase(java.util.Locale.ROOT)
        val components = when (manufacturer) {
            "xiaomi", "poco", "redmi" -> listOf(ComponentName(
                "com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"))
            "oppo", "realme", "oneplus" -> listOf(
                ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity"),
                ComponentName("com.oplus.safecenter", "com.oplus.safecenter.startupapp.StartupAppListActivity"))
            "vivo", "iqoo" -> listOf(ComponentName(
                "com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"))
            else -> emptyList()
        }
        for (component in components) {
            val intent = Intent().setComponent(component).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            // Launch directly: package visibility queries can incorrectly report no handler.
            if (runCatching { context.startActivity(intent) }.isSuccess) return
        }
        openAppDetailsSettings(context)
    }

    fun openAppNotificationSettings(context: Context, packageName: String) {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }.onFailure {
            val fallback = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            runCatching { context.startActivity(fallback) }.onFailure {
                Toast.makeText(context, "App settings are unavailable", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Requests exclusion from battery optimization to safeguard background listener durability.
     */
    fun requestIgnoreBatteryOptimizations(context: Context, launch: (Intent) -> Unit) {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
        val fallback = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        runCatching { launch(intent) }.onFailure {
            runCatching { launch(fallback) }.onFailure {
                Toast.makeText(context, "Battery settings are unavailable", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
