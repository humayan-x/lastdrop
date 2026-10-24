package io.github.humayanx.lastdrop.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4

/**
 * Full-Text Search (FTS4) virtual table backing title and content columns.
 * Automatically synchronized with the primary `notifications` table for instant indexing.
 */
@Entity(tableName = "notifications_fts")
@Fts4(contentEntity = NotificationRecord::class)
data class NotificationFts(
    @ColumnInfo(name = "title")
    val title: String?,

    @ColumnInfo(name = "content")
    val content: String?
)
