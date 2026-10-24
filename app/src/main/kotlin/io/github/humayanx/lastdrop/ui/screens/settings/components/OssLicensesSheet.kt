package io.github.humayanx.lastdrop.ui.screens.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.github.humayanx.lastdrop.ui.theme.MonoBody
import io.github.humayanx.lastdrop.ui.theme.MonoHeader
import io.github.humayanx.lastdrop.ui.theme.MonoTimestamp

data class OssPackage(
    val name: String,
    val artifact: String,
    val license: String
)

private val OpenSourcePackages = listOf(
    OssPackage("Jetpack Compose", "androidx.compose:compose-bom", "Apache License 2.0"),
    OssPackage("Material 3 Compose", "androidx.compose.material3:material3", "Apache License 2.0"),
    OssPackage("Room Persistence", "androidx.room:room-runtime", "Apache License 2.0"),
    OssPackage("KotlinX Coroutines", "org.jetbrains.kotlinx:kotlinx-coroutines-android", "Apache License 2.0"),
    OssPackage("DataStore Preferences", "androidx.datastore:datastore-preferences", "Apache License 2.0"),
    OssPackage("Navigation Compose", "androidx.navigation:navigation-compose", "Apache License 2.0"),
    OssPackage("AndroidX Core KTX", "androidx.core:core-ktx", "Apache License 2.0"),
    OssPackage("Inter Font", "rsms/inter", "SIL Open Font License 1.1"),
    OssPackage("JetBrains Mono Font", "JetBrains/JetBrainsMono", "SIL Open Font License 1.1")
)

/**
 * Open-Source software licenses sheet (equivalent to Flutter's showLicensePage).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OssLicensesSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    if (!isVisible) return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "OPEN SOURCE LICENSES",
                style = MonoHeader,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(OpenSourcePackages) { pkg ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = pkg.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = pkg.license,
                                    style = MonoTimestamp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = pkg.artifact,
                                style = MonoTimestamp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text(text = "CLOSE", style = MonoBody)
            }
        }
    }
}
