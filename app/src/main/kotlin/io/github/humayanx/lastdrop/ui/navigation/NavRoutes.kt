package io.github.humayanx.lastdrop.ui.navigation

import io.github.humayanx.lastdrop.ui.components.BottomTab

sealed class Screen(val tab: BottomTab) {
    data object Timeline : Screen(BottomTab.TIMELINE)
    data object Starred : Screen(BottomTab.STARRED)
    data object Analytics : Screen(BottomTab.ANALYTICS)
    data object Settings : Screen(BottomTab.SETTINGS)

    companion object {
        fun fromTab(tab: BottomTab): Screen = when (tab) {
            BottomTab.TIMELINE -> Timeline
            BottomTab.STARRED -> Starred
            BottomTab.ANALYTICS -> Analytics
            BottomTab.SETTINGS -> Settings
        }
    }
}
