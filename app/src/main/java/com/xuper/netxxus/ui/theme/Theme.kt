package com.xuper.netxxus.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

// ============================================================
// XUPER HYDRA — Tema Compose Material 3
// Streaming oscuro (siempre dark, no respeta sistema)
// ============================================================

private val XuperColorScheme = darkColorScheme(
    primary = XuperRed,
    onPrimary = TextPrimary,
    primaryContainer = XuperRedDim,
    onPrimaryContainer = TextPrimary,

    secondary = TextSecondary,
    onSecondary = TextPrimary,

    background = BgPrimary,
    onBackground = TextPrimary,
    surface = BgSecondary,
    onSurface = TextPrimary,
    surfaceVariant = BgTertiary,
    onSurfaceVariant = TextSecondary,
    surfaceTint = BgElevated,

    error = Error,
    onError = TextPrimary,

    outline = Divider,
    outlineVariant = Divider
)

@Composable
fun XuperHydraTheme(
    // Forzamos dark — la app es streaming oscuro por diseño
    @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = XuperColorScheme,
        typography = XuperTypography,
        content = content
    )
}
