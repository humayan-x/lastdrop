package io.github.humayanx.lastdrop.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.humayanx.lastdrop.ui.theme.AppIcons
import io.github.humayanx.lastdrop.ui.theme.SolidAmber
import io.github.humayanx.lastdrop.ui.theme.SolidEmerald
import io.github.humayanx.lastdrop.ui.theme.SolidIndigo

enum class BottomTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    ANALYTICS(
        title = "Today",
        selectedIcon = Icons.Rounded.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    TIMELINE(
        title = "History",
        selectedIcon = Icons.Rounded.Notifications,
        unselectedIcon = Icons.Outlined.Notifications
    ),
    STARRED(
        title = "Saved",
        selectedIcon = AppIcons.Bookmarks,
        unselectedIcon = AppIcons.BookmarksOutlined
    ),
    SETTINGS(
        title = "Settings",
        selectedIcon = Icons.Rounded.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )
}

/**
 * Compact floating bottom navigation pill.
 * Floats as a centered island above screen content without obscuring the footer or entire bottom.
 */
@Composable
fun FloatingBottomNavBar(
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = modifier
            .navigationBarsPadding()
            .padding(bottom = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .shadow(elevation = 8.dp, shape = shape)
                .clip(shape)
                .background(MaterialTheme.colorScheme.background)
                .border(1.dp, MaterialTheme.colorScheme.outline, shape)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomTab.entries.forEach { tab ->
                val isSelected = selectedTab == tab
                val activeTabColor = when (tab) {
                    BottomTab.ANALYTICS -> SolidEmerald
                    BottomTab.TIMELINE -> SolidIndigo
                    BottomTab.STARRED -> SolidAmber
                    BottomTab.SETTINGS -> SolidEmerald
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSelected) activeTabColor
                            else Color.Transparent
                        )
                        .clickable(role = Role.Tab) { onTabSelected(tab) }
                        .semantics { selected = isSelected },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                        contentDescription = tab.title,
                        tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
