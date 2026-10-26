package io.github.humayanx.lastdrop.ui.theme

import androidx.compose.ui.graphics.vector.ImageVector
import io.github.humayanx.lastdrop.ui.theme.icons.ActionIcons
import io.github.humayanx.lastdrop.ui.theme.icons.NavigationIcons
import io.github.humayanx.lastdrop.ui.theme.icons.SettingsIcons
import io.github.humayanx.lastdrop.ui.theme.icons.ThemeIcons

/**
 * Central icon registry delegating to modular icon groups in [io.github.humayanx.lastdrop.ui.theme.icons].
 */
object AppIcons {

    // Action & Navigation icons
    val ArrowBack: ImageVector get() = ActionIcons.ArrowBack
    val ChevronRight: ImageVector get() = ActionIcons.ChevronRight
    val ContentCopy: ImageVector get() = ActionIcons.ContentCopy
    val DeleteOutline: ImageVector get() = ActionIcons.DeleteOutline
    val SearchOff: ImageVector get() = ActionIcons.SearchOff
    val Timer: ImageVector get() = ActionIcons.Timer

    // Navigation & Bookmark icons
    val Analytics: ImageVector get() = NavigationIcons.Analytics
    val AnalyticsOutlined: ImageVector get() = NavigationIcons.AnalyticsOutlined
    val Bookmarks: ImageVector get() = NavigationIcons.Bookmarks
    val BookmarksOutlined: ImageVector get() = NavigationIcons.BookmarksOutlined
    val Bookmark: ImageVector get() = NavigationIcons.Bookmark
    val BookmarkBorder: ImageVector get() = NavigationIcons.BookmarkBorder

    // Theme Mode icons
    val BrightnessAuto: ImageVector get() = ThemeIcons.BrightnessAuto
    val DarkMode: ImageVector get() = ThemeIcons.DarkMode
    val LightMode: ImageVector get() = ThemeIcons.LightMode

    // Settings & System icons
    val BugReport: ImageVector get() = SettingsIcons.BugReport
    val Code: ImageVector get() = SettingsIcons.Code
    val Gavel: ImageVector get() = SettingsIcons.Gavel
    val SecurityUpdateWarning: ImageVector get() = SettingsIcons.SecurityUpdateWarning
    val WarningAmber: ImageVector get() = SettingsIcons.WarningAmber
}
