package io.github.humayanx.lastdrop.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.github.humayanx.lastdrop.ui.theme.MonoBody
import io.github.humayanx.lastdrop.ui.theme.MonoHeader
import io.github.humayanx.lastdrop.ui.theme.MonoTimestamp
import io.github.humayanx.lastdrop.ui.theme.StatusActive
import io.github.humayanx.lastdrop.ui.theme.StatusWarning

/**
 * Service Durability health card displaying Notification Access and Battery Whitelist permissions.
 * Matches the exact button and row layout used on the Settings page.
 */
@Composable
fun PermissionPromptDialog(
    isListenerGranted: Boolean,
    isServiceConnected: Boolean,
    isBatteryOptimizedIgnored: Boolean,
    onRequestListener: () -> Unit,
    onReconnectService: () -> Unit,
    onRequestBatteryWhitelist: () -> Unit,
    onOpenAppInfo: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isListenerHealthy = isListenerGranted && isServiceConnected
    val listenerActionText = when {
        !isListenerGranted -> "GRANT ACCESS"
        !isServiceConnected -> "RECONNECT"
        else -> "ACTIVE"
    }
    val onListenerAction = when {
        !isListenerGranted -> onRequestListener
        !isServiceConnected -> onReconnectService
        else -> onRequestListener
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = "SERVICE DURABILITY",
                style = MonoHeader,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            HealthRow(
                title = if (isListenerGranted && !isServiceConnected) "LISTENER (UNBOUND)" else "NOTIFICATION ACCESS",
                isActive = isListenerHealthy,
                actionText = listenerActionText,
                onAction = onListenerAction
            )

            Spacer(modifier = Modifier.height(14.dp))

            HealthRow(
                title = "BATTERY WHITELIST",
                isActive = isBatteryOptimizedIgnored,
                actionText = if (isBatteryOptimizedIgnored) "OPTIMAL" else "WHITELIST",
                onAction = onRequestBatteryWhitelist
            )

            if ((!isListenerHealthy || !isBatteryOptimizedIgnored) && onOpenAppInfo != null) {
                Spacer(modifier = Modifier.height(14.dp))
                HealthRow(
                    title = "RESTRICTED SETTINGS",
                    isActive = false,
                    actionText = "APP INFO",
                    onAction = onOpenAppInfo
                )
            }
        }
    }
}

@Composable
private fun HealthRow(
    title: String,
    isActive: Boolean,
    actionText: String,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isActive) StatusActive else StatusWarning)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MonoBody,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        OutlinedButton(
            onClick = onAction,
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = actionText,
                style = MonoTimestamp
            )
        }
    }
}
