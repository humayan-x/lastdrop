package io.github.humayanx.lastdrop.ui.screens.settings.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.humayanx.lastdrop.ui.theme.MonoHeader
import io.github.humayanx.lastdrop.ui.theme.MonoTimestamp

@Composable
fun PermissionHealthCard(
    isListenerGranted: Boolean,
    isServiceConnected: Boolean,
    isBatteryOptimizedIgnored: Boolean,
    onRequestListener: () -> Unit,
    onReconnectService: () -> Unit,
    onRequestBatteryWhitelist: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("PERMISSIONS", style = MonoHeader, color = MaterialTheme.colorScheme.onSurface)
            PermissionTile(
                title = "Notification access",
                description = "Capture incoming notifications.",
                actionText = when {
                    !isListenerGranted -> "GRANT"
                    !isServiceConnected -> "RECONNECT"
                    else -> "ACTIVE"
                },
                actionEnabled = !isListenerGranted || !isServiceConnected,
                onAction = if (isListenerGranted && !isServiceConnected) onReconnectService else onRequestListener
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            PermissionTile(
                title = "Battery protection",
                description = "Allow uninterrupted background capture.",
                actionText = if (isBatteryOptimizedIgnored) "ENABLED" else "ALLOW",
                actionEnabled = !isBatteryOptimizedIgnored,
                onAction = onRequestBatteryWhitelist
            )
        }
    }
}

@Composable
private fun PermissionTile(
    title: String,
    description: String,
    actionText: String,
    actionEnabled: Boolean,
    onAction: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (!actionEnabled) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        actionText,
                        style = MonoTimestamp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            } else {
                Button(
                    onClick = onAction,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(actionText, style = MonoTimestamp)
                }
            }
        }
        Text(description, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
