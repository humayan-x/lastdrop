package io.github.humayanx.lastdrop.data.local.model

import androidx.room.ColumnInfo

/**
 * Data projection for hourly notification volume.
 */
data class HourlyNotificationCount(
    @ColumnInfo(name = "hourOfDay")
    val hourOfDay: Int,

    @ColumnInfo(name = "count")
    val count: Int
)
