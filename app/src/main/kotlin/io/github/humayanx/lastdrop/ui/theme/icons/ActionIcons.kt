package io.github.humayanx.lastdrop.ui.theme.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object ActionIcons {

    val ArrowBack: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
        ImageVector.Builder(
            name = "ArrowBack",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(fill = SolidColor(Color.Black)) {
            moveTo(20.0f, 11.0f)
            horizontalLineTo(7.83f)
            lineToRelative(5.59f, -5.59f)
            lineTo(12.0f, 4.0f)
            lineToRelative(-8.0f, 8.0f)
            lineToRelative(8.0f, 8.0f)
            lineToRelative(1.41f, -1.41f)
            lineTo(7.83f, 13.0f)
            horizontalLineTo(20.0f)
            verticalLineToRelative(-2.0f)
            close()
        }.build()
    }

    val ChevronRight: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
        ImageVector.Builder(
            name = "ChevronRight",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(fill = SolidColor(Color.Black)) {
            moveTo(10.0f, 6.0f)
            lineTo(8.59f, 7.41f)
            lineTo(13.17f, 12.0f)
            lineToRelative(-4.58f, 4.59f)
            lineTo(10.0f, 18.0f)
            lineToRelative(6.0f, -6.0f)
            lineToRelative(-6.0f, -6.0f)
            close()
        }.build()
    }

    val ContentCopy: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
        ImageVector.Builder(
            name = "ContentCopy",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(fill = SolidColor(Color.Black)) {
            moveTo(16.0f, 1.0f)
            lineTo(4.0f, 1.0f)
            curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
            verticalLineToRelative(14.0f)
            horizontalLineToRelative(2.0f)
            lineTo(4.0f, 3.0f)
            horizontalLineToRelative(12.0f)
            lineTo(16.0f, 1.0f)
            close()
            moveTo(19.0f, 5.0f)
            lineTo(8.0f, 5.0f)
            curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
            verticalLineToRelative(14.0f)
            curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
            horizontalLineToRelative(11.0f)
            curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
            lineTo(21.0f, 7.0f)
            curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
            close()
            moveTo(19.0f, 21.0f)
            lineTo(8.0f, 21.0f)
            lineTo(8.0f, 7.0f)
            horizontalLineToRelative(11.0f)
            verticalLineToRelative(14.0f)
            close()
        }.build()
    }

    val DeleteOutline: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
        ImageVector.Builder(
            name = "DeleteOutline",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(fill = SolidColor(Color.Black)) {
            moveTo(6.0f, 19.0f)
            curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
            horizontalLineToRelative(8.0f)
            curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
            lineTo(18.0f, 7.0f)
            lineTo(6.0f, 7.0f)
            verticalLineToRelative(12.0f)
            close()
            moveTo(8.0f, 9.0f)
            horizontalLineToRelative(8.0f)
            verticalLineToRelative(10.0f)
            lineTo(8.0f, 19.0f)
            lineTo(8.0f, 9.0f)
            close()
            moveTo(15.5f, 4.0f)
            lineToRelative(-1.0f, -1.0f)
            horizontalLineToRelative(-5.0f)
            lineToRelative(-1.0f, 1.0f)
            lineTo(5.0f, 4.0f)
            verticalLineToRelative(2.0f)
            horizontalLineToRelative(14.0f)
            lineTo(19.0f, 4.0f)
            horizontalLineToRelative(-3.5f)
            close()
        }.build()
    }

    val SearchOff: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
        ImageVector.Builder(
            name = "SearchOff",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(fill = SolidColor(Color.Black)) {
            moveTo(15.5f, 14.0f)
            horizontalLineToRelative(-0.79f)
            lineToRelative(-0.28f, -0.27f)
            curveToRelative(1.2f, -1.4f, 1.82f, -3.31f, 1.48f, -5.34f)
            curveToRelative(-0.47f, -2.78f, -2.79f, -4.99f, -5.58f, -5.34f)
            curveTo(6.54f, 2.58f, 3.3f, 5.38f, 3.03f, 9.0f)
            horizontalLineToRelative(2.02f)
            curveToRelative(0.24f, -2.12f, 1.92f, -3.8f, 4.06f, -3.98f)
            curveTo(11.65f, 4.8f, 14.0f, 6.95f, 14.0f, 9.5f)
            curveToRelative(0.0f, 2.49f, -2.01f, 4.5f, -4.5f, 4.5f)
            curveToRelative(-0.17f, 0.0f, -0.33f, -0.03f, -0.5f, -0.05f)
            lineToRelative(0.0f, 2.02f)
            curveToRelative(0.0f, 0.0f, 0.0f, 0.0f, 0.01f, 0.01f)
            curveToRelative(1.8f, 0.13f, 3.47f, -0.47f, 4.72f, -1.55f)
            lineTo(14.0f, 14.71f)
            verticalLineToRelative(0.79f)
            lineToRelative(4.25f, 4.25f)
            curveToRelative(0.41f, 0.41f, 1.08f, 0.41f, 1.49f, 0.0f)
            lineToRelative(0.0f, 0.0f)
            curveToRelative(0.41f, -0.41f, 0.41f, -1.08f, 0.0f, -1.49f)
            lineTo(15.5f, 14.0f)
            close()
            moveTo(6.12f, 11.17f)
            lineTo(4.0f, 13.29f)
            lineToRelative(-2.12f, -2.12f)
            curveToRelative(-0.2f, -0.2f, -0.51f, -0.2f, -0.71f, 0.0f)
            lineToRelative(0.0f, 0.0f)
            curveToRelative(-0.2f, 0.2f, -0.2f, 0.51f, 0.0f, 0.71f)
            lineTo(3.29f, 14.0f)
            lineToRelative(-2.12f, 2.12f)
            curveToRelative(-0.2f, 0.2f, -0.2f, 0.51f, 0.0f, 0.71f)
            lineToRelative(0.0f, 0.0f)
            curveToRelative(0.2f, 0.2f, 0.51f, 0.2f, 0.71f, 0.0f)
            lineTo(4.0f, 14.71f)
            lineToRelative(2.12f, 2.12f)
            curveToRelative(0.2f, 0.2f, 0.51f, 0.2f, 0.71f, 0.0f)
            lineToRelative(0.0f, 0.0f)
            curveToRelative(0.2f, -0.2f, 0.2f, -0.51f, 0.0f, -0.71f)
            lineTo(4.71f, 14.0f)
            lineToRelative(2.12f, -2.12f)
            curveToRelative(0.2f, -0.2f, 0.2f, -0.51f, 0.0f, -0.71f)
            lineToRelative(0.0f, 0.0f)
            curveTo(6.63f, 10.98f, 6.32f, 10.98f, 6.12f, 11.17f)
            close()
        }.build()
    }

    val Timer: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
        ImageVector.Builder(
            name = "Timer",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(fill = SolidColor(Color.Black)) {
            moveTo(15.0f, 1.0f)
            horizontalLineTo(9.0f)
            verticalLineToRelative(2.0f)
            horizontalLineToRelative(6.0f)
            verticalLineTo(1.0f)
            close()
            moveTo(19.03f, 7.39f)
            lineToRelative(1.42f, -1.42f)
            curveToRelative(-0.43f, -0.51f, -0.9f, -0.99f, -1.41f, -1.41f)
            lineToRelative(-1.42f, 1.42f)
            curveTo(16.07f, 4.74f, 14.12f, 4.0f, 12.0f, 4.0f)
            curveToRelative(-4.97f, 0.0f, -9.0f, 4.03f, -9.0f, 9.0f)
            reflectiveCurveToRelative(4.02f, 9.0f, 9.0f, 9.0f)
            reflectiveCurveToRelative(9.0f, -4.03f, 9.0f, -9.0f)
            curveToRelative(0.0f, -2.12f, -0.74f, -4.07f, -1.97f, -5.61f)
            close()
            moveTo(12.0f, 20.0f)
            curveToRelative(-3.87f, 0.0f, -7.0f, -3.13f, -7.0f, -7.0f)
            reflectiveCurveToRelative(3.13f, -7.0f, 7.0f, -7.0f)
            reflectiveCurveToRelative(7.0f, 3.13f, 7.0f, 7.0f)
            reflectiveCurveToRelative(-3.13f, 7.0f, -7.0f, 7.0f)
            close()
            moveTo(13.0f, 9.0f)
            horizontalLineToRelative(-2.0f)
            verticalLineToRelative(5.0f)
            horizontalLineToRelative(4.0f)
            verticalLineToRelative(-1.5f)
            horizontalLineTo(13.0f)
            verticalLineTo(9.0f)
            close()
        }.build()
    }
}
