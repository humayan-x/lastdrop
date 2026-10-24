package io.github.humayanx.lastdrop.ui.screens.main

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.humayanx.lastdrop.ui.components.BottomTab
import io.github.humayanx.lastdrop.ui.components.FloatingBottomNavBar
import io.github.humayanx.lastdrop.ui.navigation.Screen
import io.github.humayanx.lastdrop.ui.screens.analytics.AnalyticsScreen
import io.github.humayanx.lastdrop.ui.screens.settings.SettingsScreen
import io.github.humayanx.lastdrop.ui.screens.starred.StarredScreen
import io.github.humayanx.lastdrop.ui.screens.timeline.TimelineScreen

/**
 * Main application scaffold hosting the 4 primary tabs and the floating bottom navigation bar.
 * Screen content extends underneath, while the floating nav bar floats cleanly above.
 */
@Composable
fun MainScaffold(
    modifier: Modifier = Modifier
) {
    var selectedTab by rememberSaveable { mutableStateOf(BottomTab.ANALYTICS) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Tab Screen Content (full screen, scrolls smoothly behind the floating bar)
        Crossfade(
            targetState = selectedTab,
            label = "tabCrossfade",
            modifier = Modifier.fillMaxSize()
        ) { tab ->
            when (Screen.fromTab(tab)) {
                Screen.Timeline -> TimelineScreen()
                Screen.Starred -> StarredScreen()
                Screen.Analytics -> AnalyticsScreen()
                Screen.Settings -> SettingsScreen()
            }
        }

        // Floating Bottom Navigation Bar (compact island centered at bottom)
        FloatingBottomNavBar(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
