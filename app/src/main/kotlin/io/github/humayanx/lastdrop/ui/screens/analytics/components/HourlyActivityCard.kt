package io.github.humayanx.lastdrop.ui.screens.analytics.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.github.humayanx.lastdrop.ui.theme.MonoHeader
import io.github.humayanx.lastdrop.ui.theme.MonoTimestamp
import java.util.Locale

/**
 * 24-hour bar chart displaying notification activity per hour for today.
 * Native zero-dependency Compose Canvas/Box rendering with interactive hour inspection.
 */
@Composable
fun HourlyActivityCard(
    hourlyDistribution: List<Int>,
    maxCount: Int,
    currentHour: Int,
    modifier: Modifier = Modifier
) {
    var selectedHour by remember { mutableStateOf<Int?>(null) }

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
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "HOURLY ACTIVITY",
                    style = MonoHeader,
                    color = MaterialTheme.colorScheme.onSurface
                )

                val selected = selectedHour
                if (selected != null) {
                    val count = hourlyDistribution.getOrElse(selected) { 0 }
                    Text(
                        text = String.format(Locale.getDefault(), "%02d:00 · %d ALERTS", selected, count),
                        style = MonoTimestamp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        text = "24 HOURS",
                        style = MonoTimestamp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 24-Hour Bar Chart Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                for (hour in 0..23) {
                    val count = hourlyDistribution.getOrElse(hour) { 0 }
                    val isSelected = selectedHour == hour
                    val isCurrent = hour == currentHour
                    val heightFraction = if (maxCount > 0 && count > 0) {
                        (count.toFloat() / maxCount.toFloat()).coerceIn(0.12f, 1f)
                    } else {
                        0.06f
                    }

                    val barColor = when {
                        isSelected -> MaterialTheme.colorScheme.secondary
                        count > 0 -> MaterialTheme.colorScheme.primary
                        isCurrent -> MaterialTheme.colorScheme.outline
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable {
                                selectedHour = if (selectedHour == hour) null else hour
                            },
                        verticalArrangement = Arrangement.Bottom,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(heightFraction)
                                .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                                .background(barColor)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Baseline Hour Markers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "00h", style = MonoTimestamp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "06h", style = MonoTimestamp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "12h", style = MonoTimestamp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "18h", style = MonoTimestamp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "23h", style = MonoTimestamp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
