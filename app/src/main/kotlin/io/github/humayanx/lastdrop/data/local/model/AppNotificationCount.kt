package io.github.humayanx.lastdrop.data.local.model

import androidx.room.ColumnInfo

/**
 * Data projection for aggregation analytics.
 */
data class AppNotificationCount(
    @ColumnInfo(name = "packageName")
    val packageName: String,

    @ColumnInfo(name = "appName")
    val appName: String,

    @ColumnInfo(name = "count")
    val count: Int
)
