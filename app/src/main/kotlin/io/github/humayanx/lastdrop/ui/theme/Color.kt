package io.github.humayanx.lastdrop.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Primary Accents (Zero-Gradient Solids)
val EmeraldAccent = Color(0xFF10B981)
val EmeraldDark = Color(0xFF059669)
val CyanAccent = Color(0xFF38BDF8)
val CyanDark = Color(0xFF0284C7)
val CoralError = Color(0xFFF43F5E)
val CoralErrorDark = Color(0xFFE11D48)

// Dark Theme Surfaces (Terminal Obsidian / Slate)
val DarkBackground = Color(0xFF0B0F14)
val DarkSurface = Color(0xFF121820)
val DarkSurfaceVariant = Color(0xFF1A222D)
val DarkBorder = Color(0xFF232F3E)
val DarkBorderSubtle = Color(0xFF18222C)
val DarkTextPrimary = Color(0xFFF1F5F9)
val DarkTextSecondary = Color(0xFF94A3B8)
val DarkTextMuted = Color(0xFF64748B)

// Light Theme Surfaces (Clean Crisp Chalk / Alabaster)
val LightBackground = Color(0xFFF8FAFC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF1F5F9)
val LightBorder = Color(0xFFE2E8F0)
val LightBorderSubtle = Color(0xFFCBD5E1)
val LightTextPrimary = Color(0xFF0F172A)
val LightTextSecondary = Color(0xFF475569)
val LightTextMuted = Color(0xFF94A3B8)

// Status Dot Indicators
val StatusActive = Color(0xFF10B981)
val StatusWarning = Color(0xFFF59E0B)
val StatusDisabled = Color(0xFF64748B)

val LastDropDarkColorScheme = darkColorScheme(
    primary = EmeraldAccent,
    onPrimary = Color(0xFF021E14),
    primaryContainer = Color(0xFF064E3B),
    onPrimaryContainer = Color(0xFFA7F3D0),
    secondary = CyanAccent,
    onSecondary = Color(0xFF082F49),
    secondaryContainer = Color(0xFF0C4A6E),
    onSecondaryContainer = Color(0xFFBAE6FD),
    tertiary = Color(0xFFA78BFA),
    onTertiary = Color(0xFF2E1065),
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    outlineVariant = DarkBorderSubtle,
    error = CoralError,
    onError = Color(0xFF4C0519)
)

val LastDropLightColorScheme = lightColorScheme(
    primary = EmeraldDark,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD1FAE5),
    onPrimaryContainer = Color(0xFF064E3B),
    secondary = CyanDark,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF0369A1),
    tertiary = Color(0xFF7C3AED),
    onTertiary = Color(0xFFFFFFFF),
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    outlineVariant = LightBorderSubtle,
    error = CoralErrorDark,
    onError = Color(0xFFFFFFFF)
)
