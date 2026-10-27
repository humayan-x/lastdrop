package io.github.humayanx.lastdrop.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// 4 Core Solid Section Identities
// 1. Solid Emerald (Live capture, Today stats, active tab)
val SolidEmerald = Color(0xFF059669)
val SolidEmeraldDark = Color(0xFF065F46)
val SolidEmeraldGlow = Color(0xFF34D399)

// 2. Solid Electric Indigo (Analytics activity, ledger apps, history)
val SolidIndigo = Color(0xFF4F46E5)
val SolidIndigoDark = Color(0xFF312E81)
val SolidIndigoGlow = Color(0xFF818CF8)
val IndigoCardLight = Color(0xFFEEF2FF)
val IndigoCardDark = Color(0xFF1E1B4B)

// 3. Solid Warm Amber (Starred vault, protected drops, gold podium)
val SolidAmber = Color(0xFFD97706)
val SolidAmberDark = Color(0xFF78350F)
val SolidAmberGlow = Color(0xFFFBBF24)

// 4. Solid Crimson Rose (Noise, notification spammers, database purge)
val SolidCrimson = Color(0xFFE11D48)
val SolidCrimsonDark = Color(0xFF881337)
val SolidCrimsonGlow = Color(0xFFFB7185)

// Primary Accents (Zero-Gradient Solids)
val EmeraldAccent = Color(0xFF10B981)
val EmeraldDark = Color(0xFF059669)
val CyanAccent = Color(0xFF38BDF8)
val CyanDark = Color(0xFF0284C7)
val AmberAccent = Color(0xFFF59E0B)
val AmberDark = Color(0xFFD97706)
val CoralError = Color(0xFFF43F5E)
val CoralErrorDark = Color(0xFFE11D48)

// Semantic Container Accents for Sections
val AmberContainerLight = Color(0xFFFEF3C7)
val AmberOnContainerLight = Color(0xFF92400E)
val AmberContainerDark = Color(0xFF451A03)
val AmberOnContainerDark = Color(0xFFFDE68A)

val RoseContainerLight = Color(0xFFFFE4E6)
val RoseOnContainerLight = Color(0xFF9F1239)
val RoseContainerDark = Color(0xFF4C0519)
val RoseOnContainerDark = Color(0xFFFECDD3)

// Dark Theme Surfaces (Midnight Slate - Comfortable elevation, avoids pitch black void)
val DarkBackground = Color(0xFF0F172A)
val DarkSurface = Color(0xFF1E293B)
val DarkSurfaceVariant = Color(0xFF334155)
val DarkBorder = Color(0xFF475569)
val DarkBorderSubtle = Color(0xFF334155)
val DarkTextPrimary = Color(0xFFF8FAFC)
val DarkTextSecondary = Color(0xFF94A3B8)
val DarkTextMuted = Color(0xFF64748B)

// Light Theme Surfaces (Grounded Slate Canvas + Crisp Elevated Surfaces)
val LightBackground = Color(0xFFF1F5F9)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFE2E8F0)
val LightBorder = Color(0xFFCBD5E1)
val LightBorderSubtle = Color(0xFFE2E8F0)
val LightTextPrimary = Color(0xFF0F172A)
val LightTextSecondary = Color(0xFF475569)
val LightTextMuted = Color(0xFF64748B)

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
    onError = Color(0xFF4C0519),
    errorContainer = RoseContainerDark,
    onErrorContainer = RoseOnContainerDark
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
    onError = Color(0xFFFFFFFF),
    errorContainer = RoseContainerLight,
    onErrorContainer = RoseOnContainerLight
)
