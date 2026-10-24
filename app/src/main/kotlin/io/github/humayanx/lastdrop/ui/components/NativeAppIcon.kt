package io.github.humayanx.lastdrop.ui.components

import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val appIcons = LruCache<String, ImageBitmap>(64)

/** Load installed app icons off the UI thread; old records may refer to uninstalled apps. */
@Composable
fun NativeAppIcon(packageName: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current.applicationContext
    val pixels = with(LocalDensity.current) { 28.dp.roundToPx() }
    var icon by remember(packageName, pixels) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(packageName, pixels) {
        icon = withContext(Dispatchers.IO) {
            val key = "$packageName:$pixels"
            appIcons.get(key) ?: runCatching {
                context.packageManager.getApplicationIcon(packageName)
                    .toBitmap(pixels, pixels).asImageBitmap()
                    .also { appIcons.put(key, it) }
            }.getOrNull()
        }
    }
    val bitmap = icon
    if (bitmap != null) {
        Image(bitmap = bitmap, contentDescription = null, modifier = modifier.size(28.dp))
    } else {
        Icon(Icons.Outlined.Notifications, contentDescription = null, modifier = modifier.size(28.dp))
    }
}
