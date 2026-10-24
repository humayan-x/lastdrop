package io.github.humayanx.lastdrop.ui.screens.setup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import io.github.humayanx.lastdrop.LastDropApp
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.Role
import kotlinx.coroutines.launch
import java.io.IOException
import android.widget.Toast
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.humayanx.lastdrop.service.PermissionManager
import io.github.humayanx.lastdrop.ui.components.LastDropHeader
import io.github.humayanx.lastdrop.ui.screens.settings.SettingsViewModel
import io.github.humayanx.lastdrop.ui.screens.settings.components.BackgroundHelpCard
import io.github.humayanx.lastdrop.ui.screens.settings.components.PermissionHealthCard
import io.github.humayanx.lastdrop.ui.theme.MonoBody
import kotlinx.coroutines.delay

/** Lives outside the tab scaffold so no app destination can bypass setup. */
@Composable
fun SetupGate(
    viewModel: SettingsViewModel = viewModel(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var backgroundConfirmed by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var browsingHistory by rememberSaveable { mutableStateOf(false) }
    val historyCount by LastDropApp.instance.notificationRepository.totalCount
        .collectAsStateWithLifecycle(initialValue = 0)
    val batteryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { viewModel.refreshPermissions(context) }

    LaunchedEffect(owner, context) {
        owner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while (true) {
                viewModel.refreshPermissions(context)
                delay(2_000L)
            }
        }
    }

    if (state.setupCompleted || browsingHistory) {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.weight(1f)) { content() }
            if (!state.setupCompleted) {
                TextButton(onClick = { browsingHistory = false }) { Text("Return to setup") }
            }
            if (!state.isNotificationListenerGranted || !state.isServiceConnected ||
                !state.isBatteryOptimizedIgnored) {
                Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(12.dp)) {
                    Text(if (!state.isNotificationListenerGranted || !state.isServiceConnected)
                        "Capture is paused. Your saved history is still available."
                    else "Battery restrictions may interrupt capture. Your saved history is still available.")
                    TextButton(onClick = {
                        when {
                            !state.isNotificationListenerGranted -> PermissionManager.openNotificationListenerSettings(context)
                            !state.isServiceConnected -> viewModel.reconnectService(context)
                            else -> PermissionManager.requestIgnoreBatteryOptimizations(context) { batteryLauncher.launch(it) }
                        }
                    }) { Text("Restore capture") }
                }
            }
        }
    } else {
        Column(Modifier.fillMaxSize()) {
            LastDropHeader(title = "Set up LastDrop")
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Complete setup to enable background notification capture.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                item {
                    PermissionHealthCard(
                        isListenerGranted = state.isNotificationListenerGranted,
                        isServiceConnected = state.isServiceConnected,
                        isBatteryOptimizedIgnored = state.isBatteryOptimizedIgnored,
                        onRequestListener = { PermissionManager.openNotificationListenerSettings(context) },
                        onReconnectService = { viewModel.reconnectService(context) },
                        onRequestBatteryWhitelist = {
                            PermissionManager.requestIgnoreBatteryOptimizations(context) { batteryLauncher.launch(it) }
                        }
                    )
                }
                if (historyCount > 0) {
                    item {
                        TextButton(onClick = { browsingHistory = true }) { Text("View saved history") }
                    }
                }
                item {
                    BackgroundHelpCard(
                        onOpenAppSettings = { PermissionManager.openAppDetailsSettings(context) },
                        onOpenAutostartSettings = { PermissionManager.openAutostartSettings(context) }
                    )
                }
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .toggleable(
                                value = backgroundConfirmed,
                                role = Role.Checkbox,
                                onValueChange = { backgroundConfirmed = it }
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = backgroundConfirmed, onCheckedChange = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "I’ve enabled background settings on my phone",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                item {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        enabled = canFinishSetup(state.isNotificationListenerGranted,
                            state.isBatteryOptimizedIgnored, state.isServiceConnected, backgroundConfirmed),
                        onClick = {
                            scope.launch {
                                try {
                                    if (!viewModel.completeSetup(context)) {
                                        Toast.makeText(context, "Check notification access and battery settings.", Toast.LENGTH_LONG).show()
                                    }
                                } catch (_: IOException) {
                                    Toast.makeText(context, "Couldn’t save setup. Please try again.", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    ) { Text("FINISH SETUP", style = MonoBody) }
                }
            }
        }
    }
}

internal fun canFinishSetup(
    listenerGranted: Boolean, batteryExempt: Boolean, connected: Boolean, backgroundConfirmed: Boolean
): Boolean = listenerGranted && batteryExempt && connected && backgroundConfirmed
