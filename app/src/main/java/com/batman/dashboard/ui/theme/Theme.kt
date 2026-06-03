package com.batman.dashboard.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color

val LocalStealthMode = compositionLocalOf { false }
val LocalThreatLevel = compositionLocalOf { 0f }

private val DefaultScheme = darkColorScheme(
    primary              = BatGold,
    onPrimary            = BatBlack,
    primaryContainer     = BatGoldDark,
    onPrimaryContainer   = BatGoldLight,
    secondary            = BatCyan,
    onSecondary          = BatBlack,
    secondaryContainer   = BatCyanDark,
    onSecondaryContainer = BatCyan,
    background           = BatBlack,
    onBackground         = TextPrimary,
    surface              = BatSurface,
    onSurface            = TextPrimary,
    surfaceVariant       = BatSurfaceVar,
    onSurfaceVariant     = TextSecondary,
    error                = BatRed,
    onError              = BatBlack,
    errorContainer       = BatRedDark,
    onErrorContainer     = BatRed,
    outline              = BatBorder,
    outlineVariant       = BatBorder,
)

private val StealthScheme = darkColorScheme(
    primary              = Color(0xFFCC2200),
    onPrimary            = Color(0xFF080000),
    primaryContainer     = Color(0xFF8B1500),
    onPrimaryContainer   = Color(0xFFCC2200),
    secondary            = Color(0xFF8B1500),
    onSecondary          = Color(0xFF080000),
    secondaryContainer   = Color(0xFF3D0A00),
    onSecondaryContainer = Color(0xFFCC2200),
    background           = Color(0xFF080000),
    onBackground         = Color(0xFFCC2200),
    surface              = Color(0xFF100500),
    onSurface            = Color(0xFFCC2200),
    surfaceVariant       = Color(0xFF180800),
    onSurfaceVariant     = Color(0xFF8B1500),
    error                = Color(0xFFCC2200),
    onError              = Color(0xFF080000),
    outline              = Color(0xFF2A0800),
    outlineVariant       = Color(0xFF2A0800),
)

private val AlertScheme = darkColorScheme(
    primary              = BatRed,
    onPrimary            = BatBlack,
    primaryContainer     = BatRedDark,
    onPrimaryContainer   = BatRed,
    secondary            = BatCyan,
    onSecondary          = BatBlack,
    background           = BatBlack,
    onBackground         = TextPrimary,
    surface              = BatSurface,
    onSurface            = TextPrimary,
    surfaceVariant       = BatSurfaceVar,
    onSurfaceVariant     = TextSecondary,
    error                = BatRed,
    onError              = BatBlack,
    outline              = BatBorder,
    outlineVariant       = BatBorder,
)

@Composable
fun BatmanDashboardTheme(
    isStealthMode: Boolean = false,
    threatLevel: Float = 0f,
    content: @Composable () -> Unit
) {
    val target = when {
        isStealthMode        -> StealthScheme
        threatLevel >= 0.75f -> AlertScheme
        else                 -> DefaultScheme
    }

    val primary    by animateColorAsState(target.primary,    tween(600), label = "primary")
    val background by animateColorAsState(target.background, tween(600), label = "bg")
    val surface    by animateColorAsState(target.surface,    tween(600), label = "surface")
    val onBg       by animateColorAsState(target.onBackground, tween(600), label = "onBg")

    val scheme = target.copy(
        primary      = primary,
        background   = background,
        surface      = surface,
        onBackground = onBg,
        onSurface    = onBg,
    )

    CompositionLocalProvider(
        LocalStealthMode provides isStealthMode,
        LocalThreatLevel provides threatLevel,
    ) {
        MaterialTheme(
            colorScheme = scheme,
            typography  = AppTypography,
            content     = content,
        )
    }
}
