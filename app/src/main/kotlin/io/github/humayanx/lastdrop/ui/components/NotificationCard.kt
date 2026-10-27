package io.github.humayanx.lastdrop.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.humayanx.lastdrop.data.local.entity.NotificationRecord
import io.github.humayanx.lastdrop.ui.theme.AmberAccent
import io.github.humayanx.lastdrop.ui.theme.AppIcons
import io.github.humayanx.lastdrop.ui.theme.MonoTimestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Ledger row item displaying a single captured notification.
 * Zero gradients, zero emojis, Material 3 vector icons and JetBrains Mono timestamp.
 */
@Composable
fun NotificationCard(
    record: NotificationRecord,
    onClick: () -> Unit,
    onToggleStar: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(record.timestamp))

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = if (record.isStarred) AmberAccent.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    NativeAppIcon(record.packageName)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = record.appName.uppercase(),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "·",
                        style = MonoTimestamp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = formattedDate,
                        style = MonoTimestamp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleStar,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (record.isStarred) AppIcons.Bookmark else AppIcons.BookmarkBorder,
                            contentDescription = if (record.isStarred) "Starred" else "Star",
                            tint = if (record.isStarred) AmberAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = AppIcons.DeleteOutline,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (!record.title.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = record.title.trim(),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 20.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            val bodyPreview = formatNotificationBodyPreview(
                content = record.content,
                bigText = record.bigText,
                title = record.title
            )

            if (bodyPreview.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = bodyPreview,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.5.sp,
                        lineHeight = 19.sp,
                        letterSpacing = 0.15.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Formats messy notification bodies into clean, flowing preview snippets.
 * Strips redundant sender/title prefixes, collapses awkward multi-newlines,
 * and eliminates duplicate content.
 */
internal fun formatNotificationBodyPreview(
    content: String?,
    bigText: String?,
    title: String?
): String {
    val trimmedTitle = title?.trim() ?: ""

    // Pick the most informative body that isn't identical to the title
    val rawText = when {
        !content.isNullOrBlank() && (trimmedTitle.isEmpty() || !content.trim().equals(trimmedTitle, ignoreCase = true)) -> content
        !bigText.isNullOrBlank() && (trimmedTitle.isEmpty() || !bigText.trim().equals(trimmedTitle, ignoreCase = true)) -> bigText
        !content.isNullOrBlank() -> content
        !bigText.isNullOrBlank() -> bigText
        else -> return ""
    }.trim()

    if (rawText.isEmpty()) return ""

    var text = rawText

    // If body text is identical to title, skip repeating it
    if (trimmedTitle.isNotEmpty() && text.equals(trimmedTitle, ignoreCase = true)) {
        return ""
    }

    // Strip redundant sender / title prefix
    if (trimmedTitle.isNotEmpty()) {
        val prefixes = listOf(
            "$trimmedTitle:",
            "$trimmedTitle -",
            "$trimmedTitle ·",
            "$trimmedTitle •",
            "$trimmedTitle—"
        )
        for (prefix in prefixes) {
            if (text.startsWith(prefix, ignoreCase = true)) {
                text = text.substring(prefix.length).trimStart()
                break
            }
        }
    }

    // Also strip sender prefix if title contains channel/sender separator "·"
    if (trimmedTitle.contains("·")) {
        val sender = trimmedTitle.substringAfterLast("·").trim()
        if (sender.isNotEmpty() && text.startsWith("$sender:", ignoreCase = true)) {
            text = text.substring(sender.length + 1).trimStart()
        }
    }

    // Clean up lines and whitespace
    val cleanLines = text.lines()
        .map { it.trim().replace(Regex("\\s+"), " ") }
        .filter { it.isNotEmpty() }

    if (cleanLines.isEmpty()) return ""

    val isChatLog = cleanLines.size > 1 && cleanLines.any { it.contains(":") }
    return if (isChatLog) {
        cleanLines.joinToString("  •  ")
    } else {
        cleanLines.joinToString(" ")
    }
}
