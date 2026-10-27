package io.github.humayanx.lastdrop.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import android.widget.Toast
import io.github.humayanx.lastdrop.BuildConfig
import io.github.humayanx.lastdrop.LastDropApp
import io.github.humayanx.lastdrop.data.debug.DebugDataSeeder
import kotlinx.coroutines.launch
import io.github.humayanx.lastdrop.ui.components.LastDropHeader
import io.github.humayanx.lastdrop.ui.screens.settings.components.OssLicensesSheet
import io.github.humayanx.lastdrop.ui.screens.settings.components.PurgeConfirmDialog
import io.github.humayanx.lastdrop.ui.screens.settings.components.RetentionSelector
import io.github.humayanx.lastdrop.ui.screens.settings.components.SettingsLinksCard
import io.github.humayanx.lastdrop.ui.screens.settings.components.ThemeSelector
import io.github.humayanx.lastdrop.ui.theme.MonoBody
import io.github.humayanx.lastdrop.ui.theme.MonoHeader

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showLicensesSheet by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Unified Header (No subtext, exact 56.dp baseline)
        LastDropHeader(title = "Settings")

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 112.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Color Theme Section
            item {
                ThemeSelector(
                    selectedTheme = uiState.themeMode,
                    onThemeSelected = viewModel::setThemeMode
                )
            }

            // Retention Cleanup Policy Section
            item {
                RetentionSelector(
                    selectedPeriod = uiState.retentionPeriod,
                    onPeriodSelected = viewModel::setRetentionPeriod
                )
            }

            // Community & Project Links Section (GitHub, Bug Tracker, Licenses, Version)
            item {
                SettingsLinksCard(
                    onOpenLicenses = { showLicensesSheet = true }
                )
            }

            // Database Purge Section at the bottom
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "DATABASE PURGE",
                            style = MonoHeader,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { viewModel.setClearAllDialogVisible(true) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "CLEAR ALL RECORDS", style = MonoBody)
                        }
                    }
                }
            }

            if (BuildConfig.DEBUG) {
                item {
                    val scope = rememberCoroutineScope()
                    val context = LocalContext.current
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        onClick = {
                            scope.launch {
                                DebugDataSeeder.seed(LastDropApp.instance.database.notificationDao())
                                Toast.makeText(context, "Dummy test data added", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Text("RE-SEED TEST DATA (DEBUG)", style = MonoBody)
                    }
                }
            }
        }
    }

    // Redesigned confirmation dialog with 12.dp corner radius and proper styled buttons
    PurgeConfirmDialog(
        isVisible = uiState.showClearAllDialog,
        onConfirm = viewModel::clearAllLogs,
        onDismiss = { viewModel.setClearAllDialogVisible(false) }
    )

    // Open Source Package Licenses Sheet
    OssLicensesSheet(
        isVisible = showLicensesSheet,
        onDismiss = { showLicensesSheet = false }
    )
}
