package com.xuper.netxxus.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Tipografía Material 3 construida desde ThemeConfig.TypographyConfig.
 */
object TypographyBuilder {

    fun build(config: TypographyConfig): Typography {
        return Typography(
            displayLarge = textStyle(config.displayLargeSize, config.displayLargeWeight),
            displayMedium = textStyle(config.displayMediumSize, config.displayMediumWeight),
            displaySmall = textStyle(config.displaySmallSize, config.displaySmallWeight),
            headlineLarge = textStyle(config.headlineLargeSize, config.headlineLargeWeight),
            headlineMedium = textStyle(config.headlineMediumSize, config.headlineMediumWeight),
            headlineSmall = textStyle(config.headlineSmallSize, config.headlineSmallWeight),
            titleLarge = textStyle(config.titleLargeSize, config.titleLargeWeight),
            titleMedium = textStyle(config.titleMediumSize, config.titleMediumWeight),
            titleSmall = textStyle(config.titleSmallSize, config.titleSmallWeight),
            bodyLarge = textStyle(config.bodyLargeSize, config.bodyLargeWeight),
            bodyMedium = textStyle(config.bodyMediumSize, config.bodyMediumWeight),
            bodySmall = textStyle(config.bodySmallSize, config.bodySmallWeight),
            labelLarge = textStyle(config.labelLargeSize, config.labelLargeWeight),
            labelMedium = textStyle(config.labelMediumSize, config.labelMediumWeight),
            labelSmall = textStyle(config.labelSmallSize, config.labelSmallWeight)
        )
    }

    private fun textStyle(size: Float, weight: String): TextStyle {
        return TextStyle(
            fontSize = size.sp,
            fontWeight = parseFontWeight(weight),
            fontFamily = FontFamily.Default,
            lineHeight = (size * 1.2).sp,
            letterSpacing = 0.sp
        )
    }

    private fun parseFontWeight(weight: String): FontWeight {
        return when (weight.lowercase()) {
            "thin" -> FontWeight.Thin
            "extralight" -> FontWeight.ExtraLight
            "light" -> FontWeight.Light
            "normal", "regular" -> FontWeight.Normal
            "medium" -> FontWeight.Medium
            "semibold" -> FontWeight.SemiBold
            "bold" -> FontWeight.Bold
            "extrabold" -> FontWeight.ExtraBold
            "black" -> FontWeight.Black
            else -> FontWeight.Normal
        }
    }
}