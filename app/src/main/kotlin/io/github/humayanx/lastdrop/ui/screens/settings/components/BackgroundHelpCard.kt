package io.github.humayanx.lastdrop.ui.screens.settings.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.humayanx.lastdrop.ui.theme.MonoHeader
import io.github.humayanx.lastdrop.ui.theme.MonoBody

@Composable
fun BackgroundHelpCard(onOpenAppSettings: () -> Unit, onOpenAutostartSettings: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("BACKGROUND ACTIVITY", style = MonoHeader)
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Allow autostart and background running to prevent the system from terminating capture.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(14.dp))
            OutlinedButton(
                onClick = onOpenAutostartSettings,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("AUTOSTART SETTINGS", style = MonoBody)
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = onOpenAppSettings,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("RESTRICTED SETTINGS", style = MonoBody)
            }
        }
    }
}
