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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import io.github.humayanx.lastdrop.ui.theme.IndigoCardDark
import io.github.humayanx.lastdrop.ui.theme.IndigoCardLight
import io.github.humayanx.lastdrop.ui.theme.SolidAmber
import io.github.humayanx.lastdrop.ui.theme.SolidEmerald
import io.github.humayanx.lastdrop.ui.theme.SolidIndigo
import io.github.humayanx.lastdrop.ui.theme.SolidIndigoDark
import io.github.humayanx.lastdrop.ui.theme.MonoHeader
import io.github.humayanx.lastdrop.ui.theme.MonoTimestamp
import java.util.Locale

/**
 * 24-hour bar chart displaying notification activity per hour for today.
 * Solid Indigo Card with vibrant activity bars.
 */
@Composable
fun HourlyActivityCard(
    hourlyDistribution: List<Int>,
    maxCount: Int,
    currentHour: Int,
    modifier: Modifier = Modifier
) {
    var selectedHour by remember { mutableStateOf<Int?>(null) }
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val cardBg = if (isDark) IndigoCardDark else IndigoCardLight
    val cardBorder = if (isDark) SolidIndigoDark else Color(0xFFC7D2FE)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(cardBg)
            .border(
                width = 1.dp,
                color = cardBorder,
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
                    color = if (isDark) Color(0xFFE0E7FF) else SolidIndigoDark
                )

                val selected = selectedHour
                if (selected != null) {
                    val count = hourlyDistribution.getOrElse(selected) { 0 }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SolidEmerald)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = String.format(Locale.getDefault(), "%02d:00 · %d ALERTS", selected, count),
                            style = MonoTimestamp,
                            color = Color.White
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isDark) SolidIndigoDark else Color(0xFFDDD6FE))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "24 HOURS",
                            style = MonoTimestamp,
                            color = if (isDark) Color(0xFFE0E7FF) else SolidIndigoDark
                        )
                    }
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
                        isSelected -> SolidAmber
                        count > 0 -> SolidIndigo
                        isCurrent -> SolidEmerald
                        else -> if (isDark) Color(0xFF2E2A72) else Color(0xFFDDD6FE)
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
                val labelColor = if (isDark) Color(0xFFA5B4FC) else Color(0xFF6366F1)
                Text(text = "00h", style = MonoTimestamp, color = labelColor)
                Text(text = "06h", style = MonoTimestamp, color = labelColor)
                Text(text = "12h", style = MonoTimestamp, color = labelColor)
                Text(text = "18h", style = MonoTimestamp, color = labelColor)
                Text(text = "23h", style = MonoTimestamp, color = labelColor)
            }
        }
    }
}
