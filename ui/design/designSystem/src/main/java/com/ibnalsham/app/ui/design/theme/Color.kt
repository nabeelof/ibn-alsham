package com.ibnalsham.app.ui.design.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Appetite-driven palette.
 *
 * The brand red and gold carried over from the old theme, but darkened and
 * desaturated into a set that survives sitting on top of food photography —
 * the previous flat `0xFFC62828` on white washed out over bright images.
 */

// Brand
val Ember = Color(0xFFC1272D) // primary — deeper than the old red, holds on photos
val EmberBright = Color(0xFFE63946) // pressed / highlight
val EmberDeep = Color(0xFF7A1519) // gradient anchor
val Saffron = Color(0xFFF2B441) // secondary — price accents, ratings
val SaffronSoft = Color(0xFFFFF0D0)

// Neutrals — warm-tinted so they sit next to food imagery without going clinical
val Charcoal = Color(0xFF15100F)
val Espresso = Color(0xFF2A211F)
val Slate = Color(0xFF6B5F5C)
val Ash = Color(0xFF9C918E)
val Linen = Color(0xFFF7F3F0)
val Cream = Color(0xFFFDFAF8)
val Porcelain = Color(0xFFFFFFFF)

// Dark surfaces
val NightBase = Color(0xFF120E0D)
val NightSurface = Color(0xFF1E1817)
val NightRaised = Color(0xFF2A2321)

// Status
val Basil = Color(0xFF2E9E5B)
val BasilSoft = Color(0xFFE3F5EA)
val Chili = Color(0xFFD93025)
val ChiliSoft = Color(0xFFFDE8E6)
val Amber = Color(0xFFE8A317)
val AmberSoft = Color(0xFFFFF4DE)

/**
 * Semantic tokens resolved per theme.
 *
 * Screens read these instead of hardcoding hex values — the old screens each
 * carried their own copy of `Color(0xFFC62828)`, which is why nothing could be
 * restyled in one place.
 */
data class AppColors(
    val brand: Color,
    val brandBright: Color,
    val brandDeep: Color,
    val onBrand: Color,
    val accent: Color,
    val accentSoft: Color,
    val background: Color,
    val surface: Color,
    val surfaceRaised: Color,
    val surfaceSunken: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val onImage: Color,
    val outline: Color,
    val success: Color,
    val successSoft: Color,
    val danger: Color,
    val dangerSoft: Color,
    val warning: Color,
    val warningSoft: Color,
    val scrim: Color,
    val isDark: Boolean,
) {
    /** Warm wash used behind hero sections and the splash. */
    val heroGradient: Brush
        get() = Brush.linearGradient(listOf(brandDeep, brand, brandBright))

    /** Top-to-bottom scrim that keeps white text legible over food photos. */
    val imageScrim: Brush
        get() = Brush.verticalGradient(
            0.0f to Color.Transparent,
            0.45f to Color.Black.copy(alpha = 0.10f),
            1.0f to Color.Black.copy(alpha = 0.78f),
        )

    /** Softer scrim for cards where the image is small. */
    val cardScrim: Brush
        get() = Brush.verticalGradient(
            0.35f to Color.Transparent,
            1.0f to Color.Black.copy(alpha = 0.62f),
        )

    val accentGradient: Brush
        get() = Brush.horizontalGradient(listOf(accent, Color(0xFFF7CE73)))

    /** Base for the shimmer sweep on skeleton placeholders. */
    val shimmerColors: List<Color>
        get() = if (isDark) {
            listOf(NightSurface, NightRaised, NightSurface)
        } else {
            listOf(Linen, Porcelain, Linen)
        }
}

internal val LightAppColors = AppColors(
    brand = Ember,
    brandBright = EmberBright,
    brandDeep = EmberDeep,
    onBrand = Color.White,
    accent = Saffron,
    accentSoft = SaffronSoft,
    background = Cream,
    surface = Porcelain,
    surfaceRaised = Porcelain,
    surfaceSunken = Linen,
    textPrimary = Charcoal,
    textSecondary = Slate,
    textTertiary = Ash,
    onImage = Color.White,
    outline = Color(0xFFE8E0DB),
    success = Basil,
    successSoft = BasilSoft,
    danger = Chili,
    dangerSoft = ChiliSoft,
    warning = Amber,
    warningSoft = AmberSoft,
    scrim = Color.Black.copy(alpha = 0.45f),
    isDark = false,
)

internal val DarkAppColors = AppColors(
    brand = EmberBright,
    brandBright = Color(0xFFFF5A67),
    brandDeep = EmberDeep,
    onBrand = Color.White,
    accent = Saffron,
    accentSoft = Color(0xFF3D311F),
    background = NightBase,
    surface = NightSurface,
    surfaceRaised = NightRaised,
    surfaceSunken = Color(0xFF0C0908),
    textPrimary = Color(0xFFF6F1EF),
    textSecondary = Color(0xFFB5A9A5),
    textTertiary = Color(0xFF8A7D79),
    onImage = Color.White,
    outline = Color(0xFF3A312F),
    success = Color(0xFF4CC47E),
    successSoft = Color(0xFF16301F),
    danger = Color(0xFFFF6B5E),
    dangerSoft = Color(0xFF34191A),
    warning = Color(0xFFFFC048),
    warningSoft = Color(0xFF33280F),
    scrim = Color.Black.copy(alpha = 0.65f),
    isDark = true,
)
