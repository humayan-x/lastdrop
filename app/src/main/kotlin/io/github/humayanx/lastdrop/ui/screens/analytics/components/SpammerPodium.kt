package io.github.humayanx.lastdrop.ui.screens.analytics.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.humayanx.lastdrop.data.local.model.AppNotificationCount
import io.github.humayanx.lastdrop.ui.components.NativeAppIcon

@Composable
fun SpammerPodium(
    apps: List<AppNotificationCount>,
    onAppClick: (AppNotificationCount) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            // Traditional medal order: silver, gold, bronze, with a shared baseline.
            listOf(1, 0, 2).forEach { index ->
                val app = apps.getOrNull(index)
                if (app == null) {
                    Spacer(Modifier.weight(1f))
                } else {
                    val deepMedalColor = when (index) {
                        0 -> Color(0xFFB45309) // Deep Rich Gold
                        1 -> Color(0xFF475569) // Deep Steel Silver
                        else -> Color(0xFF9A3412) // Deep Solid Bronze
                    }
                    val rank = when (index) {
                        0 -> "1st"
                        1 -> "2nd"
                        else -> "3rd"
                    }
                    Column(
                        modifier = Modifier.weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(
                                role = Role.Button,
                                onClickLabel = "Open notification settings",
                                onClick = { onAppClick(app) }
                            )
                            .semantics(mergeDescendants = true) {
                                contentDescription = "$rank, ${app.appName}, ${app.count} notifications today"
                            },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${app.count} ALERTS",
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Box(Modifier.size(52.dp), contentAlignment = Alignment.Center) {
                            NativeAppIcon(app.packageName, Modifier.size(40.dp))
                        }
                        Spacer(Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(when (index) { 0 -> 100.dp; 1 -> 76.dp; else -> 56.dp })
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                .background(deepMedalColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = rank,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
