package io.github.humayanx.lastdrop.data.repository

import io.github.humayanx.lastdrop.data.local.model.HourlyNotificationCount
import java.time.Instant
import java.time.ZoneId

/** Repeated fall-back hours share a bar; skipped spring-forward hours remain empty. */
internal fun hourlyCounts(timestamps: List<Long>, zone: ZoneId): List<HourlyNotificationCount> {
    val counts = IntArray(24)
    timestamps.forEach { counts[Instant.ofEpochMilli(it).atZone(zone).hour]++ }
    return counts.mapIndexed { hour, count -> HourlyNotificationCount(hour, count) }
}
