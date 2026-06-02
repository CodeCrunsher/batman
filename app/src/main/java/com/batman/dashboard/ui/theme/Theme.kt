package com.batman.dashboard.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val GothamColorScheme = darkColorScheme(
    primary          = BatGold,
    onPrimary        = BatBlack,
    primaryContainer = BatGoldDark,
    onPrimaryContainer = BatGoldLight,

    secondary        = BatCyan,
    onSecondary      = BatBlack,
    secondaryContainer = BatCyanDark,
    onSecondaryContainer = BatCyan,

    background       = BatBlack,
    onBackground     = TextPrimary,

    surface          = BatSurface,
    onSurface        = TextPrimary,
    surfaceVariant   = BatSurfaceVar,
    onSurfaceVariant = TextSecondary,

    error            = BatRed,
    onError          = BatBlack,
    errorContainer   = BatRedDark,
    onErrorContainer = BatRed,

    outline          = BatBorder,
    outlineVariant   = BatBorder,
)

@Composable
fun BatmanDashboardTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = GothamColorScheme,
        typography  = AppTypography,
        content     = content
    )
}
