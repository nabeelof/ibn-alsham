package com.ibnalsham.app.ui.design.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp

/**
 * Expressive type scale.
 *
 * The display sizes are deliberately large — the appetite-driven direction leans
 * on big headlines over imagery. Line-height alignment is set explicitly because
 * Arabic glyphs sit taller than Latin ones and the default trimming clipped
 * descenders on the mixed Arabic/English strings this app shows.
 */

private val LineHeightFix = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)

private fun style(
    size: Int,
    lineHeight: Int,
    weight: FontWeight,
    letterSpacing: Double = 0.0,
) = TextStyle(
    fontFamily = FontFamily.Default,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    fontWeight = weight,
    letterSpacing = letterSpacing.sp,
    lineHeightStyle = LineHeightFix,
)

val AppTypography = Typography(
    displayLarge = style(40, 48, FontWeight.Black, (-1.0)),
    displayMedium = style(34, 42, FontWeight.ExtraBold, (-0.7)),
    displaySmall = style(28, 36, FontWeight.ExtraBold, (-0.5)),

    headlineLarge = style(26, 34, FontWeight.Bold, (-0.4)),
    headlineMedium = style(22, 30, FontWeight.Bold, (-0.3)),
    headlineSmall = style(20, 28, FontWeight.Bold, (-0.2)),

    titleLarge = style(18, 26, FontWeight.Bold),
    titleMedium = style(16, 24, FontWeight.SemiBold),
    titleSmall = style(14, 20, FontWeight.SemiBold),

    bodyLarge = style(16, 24, FontWeight.Normal),
    bodyMedium = style(14, 22, FontWeight.Normal),
    bodySmall = style(12, 18, FontWeight.Normal),

    // Label line heights are ~1.6x rather than the Latin-typical 1.3x: Arabic
    // glyphs sit taller and carry marks above and below, and at these sizes a
    // tight line box crops them (the struck-through price lost its descenders).
    labelLarge = style(14, 22, FontWeight.SemiBold, 0.1),
    labelMedium = style(12, 19, FontWeight.SemiBold, 0.2),
    labelSmall = style(11, 18, FontWeight.Medium, 0.4),
)

/** Styles that do not map onto the Material scale. */
object AppTextStyles {
    /** Prices — tabular-feeling weight so digits do not jitter as totals change. */
    val price = style(18, 24, FontWeight.ExtraBold, (-0.3))
    val priceLarge = style(24, 30, FontWeight.Black, (-0.5))
    val priceSmall = style(14, 18, FontWeight.Bold, (-0.2))

    /** All-caps section eyebrows above headlines. */
    val eyebrow = style(11, 17, FontWeight.Bold, 1.4)

    /** Numeric badges on the nav bar and cart. */
    val badge = style(11, 16, FontWeight.Black)
}
