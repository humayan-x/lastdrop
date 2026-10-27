package io.github.humayanx.lastdrop.ui.screens.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.humayanx.lastdrop.service.PermissionManager
import io.github.humayanx.lastdrop.ui.components.LastDropHeader
import io.github.humayanx.lastdrop.ui.screens.analytics.components.AppVolumeRow
import io.github.humayanx.lastdrop.ui.screens.analytics.components.HourlyActivityCard
import io.github.humayanx.lastdrop.ui.screens.analytics.components.SpammerPodium
import io.github.humayanx.lastdrop.ui.screens.analytics.components.StatSummaryCard
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import io.github.humayanx.lastdrop.ui.theme.AppIcons
import io.github.humayanx.lastdrop.ui.theme.SolidCrimson
import io.github.humayanx.lastdrop.ui.theme.SolidEmerald
import io.github.humayanx.lastdrop.ui.theme.SolidEmeraldDark
import io.github.humayanx.lastdrop.ui.theme.SolidEmeraldGlow
import io.github.humayanx.lastdrop.ui.theme.SolidIndigo
import io.github.humayanx.lastdrop.ui.theme.SolidIndigoDark
import io.github.humayanx.lastdrop.ui.theme.SolidIndigoGlow
import io.github.humayanx.lastdrop.ui.theme.MonoBody
import io.github.humayanx.lastdrop.ui.theme.MonoHeader

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    Column(modifier = modifier.fillMaxSize()) {
        LastDropHeader(
            title = "LAST DROP",
            actions = {
                IconButton(
                    onClick = { viewModel.toggleTheme(isDark) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (isDark) AppIcons.LightMode else AppIcons.DarkMode,
                        contentDescription = if (isDark) "Switch to light theme" else "Switch to dark theme",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 112.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (uiState.isLoading) {
                item { CircularProgressIndicator() }
            } else {
                item {
                    HourlyActivityCard(
                        hourlyDistribution = uiState.hourlyDistribution,
                        maxCount = uiState.maxHourlyCount,
                        currentHour = uiState.currentHour
                    )
                }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatSummaryCard(
                            title = "Today",
                            value = "${uiState.todayCount}",
                            containerColor = if (isDark) SolidEmeraldDark else SolidEmerald,
                            contentColor = Color.White,
                            valueColor = if (isDark) SolidEmeraldGlow else Color.White,
                            borderColor = Color.Transparent,
                            modifier = Modifier.weight(1f)
                        )
                        StatSummaryCard(
                            title = "Apps",
                            value = "${uiState.appCounts.size}",
                            containerColor = if (isDark) SolidIndigoDark else SolidIndigo,
                            contentColor = Color.White,
                            valueColor = if (isDark) SolidIndigoGlow else Color.White,
                            borderColor = Color.Transparent,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(SolidCrimson)
                        )
                        Text("NOTIFICATION SPAMMERS", style = MonoHeader, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
                item {
                    SpammerPodium(
                        apps = uiState.topSpammerApps,
                        onAppClick = { app ->
                            PermissionManager.openAppNotificationSettings(context, app.packageName)
                        }
                    )
                }

                item { Text("OTHER APPS", style = MonoHeader) }
                if (uiState.otherAppsToday.isEmpty()) {
                    item {
                        Text(
                            text = if (uiState.appCounts.isEmpty()) "No notifications yet" else "No other apps today",
                            style = MonoBody,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                items(uiState.otherAppsToday, key = { it.packageName }) { app ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        AppVolumeRow(
                            item = app,
                            maxCount = uiState.maxAppCount,
                            onClick = { PermissionManager.openAppNotificationSettings(context, app.packageName) },
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}
