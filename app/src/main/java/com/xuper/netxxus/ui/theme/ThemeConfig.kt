package com.xuper.netxxus.ui.theme

import android.content.Context
import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * Configuración centralizada del tema personalizable.
 * Permite cambiar la "cara" de la app sin tocar código de lógica.
 */
@Serializable
data class ThemeConfig(
    val brand: BrandConfig = BrandConfig(),
    val colors: ColorPaletteConfig = ColorPaletteConfig(),
    val typography: TypographyConfig = TypographyConfig(),
    val shapes: ShapesConfig = ShapesConfig(),
    val layout: LayoutConfig = LayoutConfig(),
    val features: FeatureFlagsConfig = FeatureFlagsConfig()
) {
    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        /** Carga la configuración desde un JSON string */
        fun fromJson(jsonString: String): ThemeConfig = json.decodeFromString(jsonString)

        /** Carga la configuración desde un archivo en assets */
        fun fromAssets(context: android.content.Context, fileName: String = "theme-config.json"): ThemeConfig {
            val inputStream = context.assets.open(fileName)
            val jsonString = inputStream.readText()
            inputStream.close()
            return fromJson(jsonString)
        }

        /** Configuración por defecto (branding Xuper Hydra) */
        fun default(): ThemeConfig = ThemeConfig()
    }
}

/** Configuración de marca/branding */
@Serializable
data class BrandConfig(
    val appName: String = "XUPER HYDRA",
    val shortName: String = "XUPER",
    val tagline: String = "Streaming TV",
    val logoAsset: String = "ic_launcher",
    val packageName: String = "com.xuper.netxxus",
    val versionSuffix: String = "-custom"
)

/** Paleta de colores completa personalizable */
@Serializable
data class ColorPaletteConfig(
    val brandPrimary: String = "#E50914",
    val brandPrimaryDark: String = "#B80710",
    val brandPrimaryLight: String = "#FF3D3D",
    val brandSecondary: String = "#FFFFFF",
    val brandAccent: String = "#FFD700",
    val surface: String = "#0A0A0A",
    val surfaceVariant: String = "#141414",
    val surfaceContainer: String = "#1F1F1F",
    val surfaceContainerHigh: String = "#2A2A2A",
    val surfaceContainerHighest: String = "#363636",
    val background: String = "#0A0A0A",
    val backgroundSecondary: String = "#111111",
    val onPrimary: String = "#FFFFFF",
    val onSurface: String = "#FFFFFF",
    val onSurfaceVariant: String = "#B3B3B3",
    val onBackground: String = "#FFFFFF",
    val onError: String = "#FFFFFF",
    val error: String = "#E50914",
    val errorContainer: String = "#3D0A0A",
    val success: String = "#46D369",
    val successContainer: String = "#0A2D14",
    val warning: String = "#FFB300",
    val warningContainer: String = "#3D2B00",
    val info: String = "#2196F3",
    val infoContainer: String = "#0A1F3D",
    val outline: String = "#333333",
    val outlineVariant: String = "#2A2A2A",
    val scrim: String = "#000000",
    val shadow: String = "#000000",
    val inverseSurface: String = "#E6E6E6",
    val inverseOnSurface: String = "#1A1A1A",
    val inversePrimary: String = "#B80710"
) {
    fun toColorPalette(): ColorPalette = ColorPalette(
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
}

/** Tipografía personalizable */
@Serializable
data class TypographyConfig(
    val fontFamily: String = "sans-serif",
    val fontFamilyBold: String = "sans-serif-medium",
    val fontFamilyBlack: String = "sans-serif-black",
    val displayLargeSize: Float = 57f,
    val displayLargeWeight: String = "Black",
    val displayMediumSize: Float = 45f,
    val displayMediumWeight: String = "Black",
    val displaySmallSize: Float = 36f,
    val displaySmallWeight: String = "Bold",
    val headlineLargeSize: Float = 32f,
    val headlineLargeWeight: String = "Bold",
    val headlineMediumSize: Float = 28f,
    val headlineMediumWeight: String = "Bold",
    val headlineSmallSize: Float = 24f,
    val headlineSmallWeight: String = "SemiBold",
    val titleLargeSize: Float = 22f,
    val titleLargeWeight: String = "SemiBold",
    val titleMediumSize: Float = 16f,
    val titleMediumWeight: String = "Medium",
    val titleSmallSize: Float = 14f,
    val titleSmallWeight: String = "Medium",
    val bodyLargeSize: Float = 16f,
    val bodyLargeWeight: String = "Normal",
    val bodyMediumSize: Float = 14f,
    val bodyMediumWeight: String = "Normal",
    val bodySmallSize: Float = 12f,
    val bodySmallWeight: String = "Normal",
    val labelLargeSize: Float = 14f,
    val labelLargeWeight: String = "Medium",
    val labelMediumSize: Float = 12f,
    val labelMediumWeight: String = "Medium",
    val labelSmallSize: Float = 11f,
    val labelSmallWeight: String = "Normal"
)

/** Formas/bordes redondeados */
@Serializable
data class ShapesConfig(
    val cornerExtraSmall: Float = 4f,
    val cornerSmall: Float = 8f,
    val cornerMedium: Float = 12f,
    val cornerLarge: Float = 16f,
    val cornerExtraLarge: Float = 28f,
    val cornerFull: Float = 9999f
)

/** Layout/espaciado */
@Serializable
data class LayoutConfig(
    val sidebarWidth: Int = 140,
    val contentPadding: Int = 24,
    val cardAspectRatio: Float = 160f / 220f,
    val featuredBannerHeight: Int = 280,
    val itemSpacing: Int = 12,
    val sectionSpacing: Int = 24
)

/** Feature flags para activar/desactivar funcionalidades */
@Serializable
data class FeatureFlagsConfig(
    val enableLiveTv: Boolean = true,
    val enableMovies: Boolean = true,
    val enableSeries: Boolean = true,
    val enableSearch: Boolean = true,
    val enableUserProfile: Boolean = true,
    val enableSettings: Boolean = true,
    val enableAbout: Boolean = true,
    val enableLogin: Boolean = true,
    val enableOfflineMode: Boolean = false,
    val enableDownload: Boolean = false,
    val enableCast: Boolean = false,
    val enableDrm: Boolean = false,
    val enableAnalytics: Boolean = false,
    val enableCrashlytics: Boolean = false
)