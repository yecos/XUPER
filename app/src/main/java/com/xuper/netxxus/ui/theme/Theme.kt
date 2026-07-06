package com.xuper.netxxus.ui.theme

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ============================================================
// XUPER HYDRA — Tema Compose Material 3 (Configurable)
// Streaming oscuro — carga configuración desde assets/theme-config.json
// ============================================================

/**
 * Holder del tema actual (singleton para acceso rápido)
 */
object XuperThemeHolder {
    @Volatile
    private var _config: ThemeConfig? = null
    @Volatile
    private var _colorPalette: ColorPalette? = null
    @Volatile
    private var _typography: Typography? = null
    @Volatile
    private var _shapes: Shapes? = null

    val config: ThemeConfig
        get() = _config ?: ThemeConfig.default()

    val colorPalette: ColorPalette
        get() = _colorPalette ?: config.colors.toColorPalette()

    val typography: Typography
        get() = _typography ?: TypographyBuilder.build(config.typography)

    val shapes: Shapes
        get() = _shapes ?: ShapesBuilder.build(config.shapes)

    /** Inicializa el tema desde assets (llamar en Application.onCreate) */
    fun initialize(context: Context, assetName: String = "theme-config.json") {
        _config = ThemeConfig.fromAssets(context, assetName)
        _colorPalette = _config?.colors?.toColorPalette()
        _typography = _config?.typography?.let { TypographyBuilder.build(it) }
        _shapes = _config?.shapes?.let { ShapesBuilder.build(it) }
    }

    /** Permite hot-reload del tema en tiempo de ejecución */
    fun reload(context: Context, assetName: String = "theme-config.json") {
        initialize(context, assetName)
    }
}

/**
 * Convierte ColorPaletteConfig a ColorPalette
 */
fun ColorPaletteConfig.toColorPalette(): ColorPalette = ColorPalette(
    brandPrimary = Color(android.graphics.Color.parseColor(brandPrimary)),
    brandPrimaryDark = Color(android.graphics.Color.parseColor(brandPrimaryDark)),
    brandPrimaryLight = Color(android.graphics.Color.parseColor(brandPrimaryLight)),
    brandSecondary = Color(android.graphics.Color.parseColor(brandSecondary)),
    brandAccent = Color(android.graphics.Color.parseColor(brandAccent)),
    surface = Color(android.graphics.Color.parseColor(surface)),
    surfaceVariant = Color(android.graphics.Color.parseColor(surfaceVariant)),
    surfaceContainer = Color(android.graphics.Color.parseColor(surfaceContainer)),
    surfaceContainerHigh = Color(android.graphics.Color.parseColor(surfaceContainerHigh)),
    surfaceContainerHighest = Color(android.graphics.Color.parseColor(surfaceContainerHighest)),
    background = Color(android.graphics.Color.parseColor(background)),
    backgroundSecondary = Color(android.graphics.Color.parseColor(backgroundSecondary)),
    onPrimary = Color(android.graphics.Color.parseColor(onPrimary)),
    onSurface = Color(android.graphics.Color.parseColor(onSurface)),
    onSurfaceVariant = Color(android.graphics.Color.parseColor(onSurfaceVariant)),
    onBackground = Color(android.graphics.Color.parseColor(onBackground)),
    onError = Color(android.graphics.Color.parseColor(onError)),
    error = Color(android.graphics.Color.parseColor(error)),
    errorContainer = Color(android.graphics.Color.parseColor(errorContainer)),
    success = Color(android.graphics.Color.parseColor(success)),
    successContainer = Color(android.graphics.Color.parseColor(successContainer)),
    warning = Color(android.graphics.Color.parseColor(warning)),
    warningContainer = Color(android.graphics.Color.parseColor(warningContainer)),
    info = Color(android.graphics.Color.parseColor(info)),
    infoContainer = Color(android.graphics.Color.parseColor(infoContainer)),
    outline = Color(android.graphics.Color.parseColor(outline)),
    outlineVariant = Color(android.graphics.Color.parseColor(outlineVariant)),
    scrim = Color(android.graphics.Color.parseColor(scrim)),
    shadow = Color(android.graphics.Color.parseColor(shadow)),
    inverseSurface = Color(android.graphics.Color.parseColor(inverseSurface)),
    inverseOnSurface = Color(android.graphics.Color.parseColor(inverseOnSurface)),
    inversePrimary = Color(android.graphics.Color.parseColor(inversePrimary))
)

/** Colores legacy para compatibilidad con código existente */
val XuperRed = Color(0xFFE50914)        // Rojo brillante
val XuperRedPressed = Color(0xFFB20710)
val XuperRedDim = Color(0x80E50914)

// Superficies oscuras
val BgPrimary = Color(0xFF0A0A0A)
val BgSecondary = Color(0xFF141414)
val BgTertiary = Color(0xFF1F1F1F)
val BgElevated = Color(0xFF2A2A2A)

// Texto
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFB3B3B3)
val TextDisabled = Color(0xFF707070)

// Estado
val Success = Color(0xFF46D369)
val Warning = Color(0xFFFFB700)
val Error = Color(0xFFE50914)
val Info = Color(0xFF2196F3)

// Bordes
val Divider = Color(0xFF2A2A2A)
val BorderFocus = XuperRed

/**
 * Tema principal Compose - usa la configuración cargada
 */
@Composable
fun XuperHydraTheme(
    context: Context? = null,
    darkTheme: Boolean = true, // Forzamos dark - streaming oscuro por diseño
    content: @Composable () -> Unit
) {
    // Si se pasa contexto, intenta cargar el tema desde assets
    // Si no, usa el holder (que debe haber sido inicializado)
    val colorScheme = if (context != null) {
        val cfg = ThemeConfig.fromAssets(context)
        cfg.colors.toColorPalette().toDarkColorScheme()
    } else {
        XuperThemeHolder.colorPalette.toDarkColorScheme()
    }
    val typography = XuperThemeHolder.typography
    val shapes = XuperThemeHolder.shapes

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = shapes,
        content = content
    )
}

/**
 * Tema de conveniencia sin contexto (usa defaults o holder)
 * Útil para previews o tests
 */
@Composable
fun XuperHydraTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) = XuperHydraTheme(context = null, darkTheme = darkTheme, content = content)

/**
 * Convierte ColorPalette a Material3 darkColorScheme
 */
fun ColorPalette.toDarkColorScheme() = androidx.compose.material3.darkColorScheme(
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

/**
 * Acceso rápido a colores del tema actual
 */
@Composable
fun xuperColors() = MaterialTheme.colorScheme

@Composable
fun xuperTypography() = MaterialTheme.typography

@Composable
fun xuperShapes() = MaterialTheme.shapes