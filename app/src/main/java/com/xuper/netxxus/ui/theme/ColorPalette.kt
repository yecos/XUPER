package com.xuper.netxxus.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Paleta de colores material 3 completa para Compose.
 * Generada desde ThemeConfig.ColorPaletteConfig.
 */
data class ColorPalette(
    // Brand
    val brandPrimary: Color,
    val brandPrimaryDark: Color,
    val brandPrimaryLight: Color,
    val brandSecondary: Color,
    val brandAccent: Color,

    // Surface
    val surface: Color,
    val surfaceVariant: Color,
    val surfaceContainer: Color,
    val surfaceContainerHigh: Color,
    val surfaceContainerHighest: Color,

    // Background
    val background: Color,
    val backgroundSecondary: Color,

    // On colors
    val onPrimary: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val onBackground: Color,
    val onError: Color,

    // Semantic
    val error: Color,
    val errorContainer: Color,
    val success: Color,
    val successContainer: Color,
    val warning: Color,
    val warningContainer: Color,
    val info: Color,
    val infoContainer: Color,

    // Outline
    val outline: Color,
    val outlineVariant: Color,

    // Scrim
    val scrim: Color,
    val shadow: Color,

    // Inverse
    val inverseSurface: Color,
    val inverseOnSurface: Color,
    val inversePrimary: Color
) {
    /** Convierte a Material3 darkColorScheme */
    fun toDarkColorScheme() = androidx.compose.material3.darkColorScheme(
        primary = brandPrimary,
        onPrimary = onPrimary,
        primaryContainer = brandPrimaryDark,
        onPrimaryContainer = onPrimary,
        secondary = brandSecondary,
        onSecondary = onSurface,
        tertiary = brandAccent,
        onTertiary = onSurface,
        error = error,
        onError = onError,
        errorContainer = errorContainer,
        onErrorContainer = onSurface,
        background = background,
        onBackground = onBackground,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant,
        outline = outline,
        outlineVariant = outlineVariant,
        shadow = shadow,
        scrim = scrim,
        inverseSurface = inverseSurface,
        inverseOnSurface = inverseOnSurface,
        inversePrimary = inversePrimary,
        surfaceTint = surfaceContainerHigh
    )
}