package io.github.humayanx.lastdrop.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Primary notification entity storing captured status bar events.
 * Indexed by packageName and timestamp for fast chronological and package-based retrieval.
 */
@Entity(
    tableName = "notifications",
    indices = [
        Index(value = ["packageName"]),
        Index(value = ["timestamp"]),
        Index(value = ["notificationKey", "timestamp"])
    ]
)
data class NotificationRecord(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,

    @ColumnInfo(name = "packageName")
    val packageName: String,

    @ColumnInfo(name = "appName")
    val appName: String,

    @ColumnInfo(name = "title")
    val title: String?,

    @ColumnInfo(name = "content")
    val content: String?,

    @ColumnInfo(name = "bigText")
    val bigText: String? = null,

    @ColumnInfo(name = "subText")
    val subText: String? = null,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @ColumnInfo(name = "isRead")
    val isRead: Boolean = false,

    @ColumnInfo(name = "isStarred")
    val isStarred: Boolean = false,

    // Null for records captured before notification identity was persisted.
    val notificationKey: String? = null
)
