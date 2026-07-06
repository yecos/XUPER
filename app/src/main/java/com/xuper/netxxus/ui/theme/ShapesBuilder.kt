package com.xuper.netxxus.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Formas Material 3 construidas desde ThemeConfig.ShapesConfig.
 */
object ShapesBuilder {

    fun build(config: ShapesConfig): Shapes {
        return Shapes(
            extraSmall = RoundedCornerShape(config.cornerExtraSmall.dp),
            small = RoundedCornerShape(config.cornerSmall.dp),
            medium = RoundedCornerShape(config.cornerMedium.dp),
            large = RoundedCornerShape(config.cornerLarge.dp),
            extraLarge = RoundedCornerShape(config.cornerExtraLarge.dp)
        )
    }
}