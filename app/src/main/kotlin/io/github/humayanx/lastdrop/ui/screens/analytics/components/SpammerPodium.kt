package io.github.humayanx.lastdrop.ui.screens.analytics.components

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
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface
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
                    val medal = when (index) {
                        0 -> Color(0xFFFFD54F)
                        1 -> Color(0xFFBFC9D4)
                        else -> Color(0xFFCD8B54)
                    }
                    val rank = when (index) {
                        0 -> "1st"
                        1 -> "2nd"
                        else -> "3rd"
                    }
                    Column(
                        modifier = Modifier.weight(1f)
                            .clip(RoundedCornerShape(4.dp))
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
                            text = app.count.toString(),
                            modifier = Modifier.align(Alignment.End)
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Box(Modifier.size(56.dp), contentAlignment = Alignment.Center) {
                            NativeAppIcon(app.packageName, Modifier.size(40.dp))
                        }
                        Spacer(Modifier.height(8.dp))
                        Box(
                            modifier = Modifier.fillMaxWidth()
                                .height(when (index) { 0 -> 100.dp; 1 -> 76.dp; else -> 56.dp })
                                .background(medal.copy(alpha = 0.16f), RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .padding(top = 12.dp),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Box(
                                modifier = Modifier.background(medal, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    rank,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF241C14)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
