package io.github.humayanx.lastdrop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.material3.Surface
import io.github.humayanx.lastdrop.service.LastDropListenerService
import io.github.humayanx.lastdrop.ui.components.LoadingContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.flow.first
import androidx.compose.runtime.getValue
import io.github.humayanx.lastdrop.data.repository.ThemeMode
import io.github.humayanx.lastdrop.service.PermissionManager
import io.github.humayanx.lastdrop.ui.screens.main.MainScaffold
import io.github.humayanx.lastdrop.ui.screens.setup.SetupGate
import io.github.humayanx.lastdrop.ui.theme.LastDropTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val preferencesRepository = LastDropApp.instance.userPreferencesRepository
            val preferences by preferencesRepository.userPreferencesFlow.collectAsStateWithLifecycle(
                initialValue = null
            )

            var startupReady by rememberSaveable { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                if (!startupReady) {
                    // Let the loading UI draw before checking system settings.
                    withFrameNanos { }
                    val granted = withContext(Dispatchers.IO) {
                        runCatching {
                            PermissionManager.ensureServiceBound(applicationContext)
                            PermissionManager.isNotificationListenerEnabled(applicationContext)
                        }.getOrDefault(false)
                    }
                    if (granted) {
                        // Binding is asynchronous; never trap the user on a spinner.
                        withTimeoutOrNull(1_500L) {
                            LastDropListenerService.isServiceConnected.first { it }
                        }
                    }
                    startupReady = true
                }
            }

            val systemInDark = isSystemInDarkTheme()
            val isDark = when (preferences?.themeMode) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                else -> systemInDark
            }

            LastDropTheme(darkTheme = isDark) {
                Surface {
                    if (!startupReady || preferences == null) LoadingContent() else SetupGate { MainScaffold() }
                }
            }
        }
    }

}
