package com.ibnalsham.app.ui.design.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ibnalsham.app.library.data.cache.ThemeMode

/** Corner radii. Generous by design — the direction calls for soft, edible shapes. */
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(34.dp),
)

/** Spacing scale. Screens use these instead of loose dp literals. */
object Spacing {
    val xxs = 2.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val xxxl = 32.dp
    val huge = 48.dp

    /** Horizontal page gutter — every screen aligns to this. */
    val gutter = 20.dp

    /** Bottom padding that clears the floating nav bar. */
    val navBarClearance = 108.dp
}

private val LocalAppColors = staticCompositionLocalOf { LightAppColors }

/**
 * Resolves the user's stored preference against the device setting.
 *
 * [ThemeMode.System] is the default, so the app follows the device until the
 * user makes an explicit choice in Settings.
 */
@Composable
fun ThemeMode.isDark(): Boolean = when (this) {
    ThemeMode.Light -> false
    ThemeMode.Dark -> true
    ThemeMode.System -> isSystemInDarkTheme()
}

/**
 * Design-system entry point.
 *
 * Exposes the semantic palette through [AppTheme.colors] alongside the Material
 * scheme, so components can reach tokens Material has no slot for (image scrims,
 * the shimmer ramp, the accent gradient) without reaching for raw hex.
 */
@Composable
fun IbnAlshamTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val appColors = if (darkTheme) DarkAppColors else LightAppColors

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = appColors.brand,
            onPrimary = appColors.onBrand,
            primaryContainer = appColors.brandDeep,
            onPrimaryContainer = Color.White,
            secondary = appColors.accent,
            onSecondary = Charcoal,
            secondaryContainer = appColors.accentSoft,
            tertiary = appColors.success,
            background = appColors.background,
            onBackground = appColors.textPrimary,
            surface = appColors.surface,
            onSurface = appColors.textPrimary,
            surfaceVariant = appColors.surfaceRaised,
            onSurfaceVariant = appColors.textSecondary,
            outline = appColors.outline,
            error = appColors.danger,
            onError = Color.White,
        )
    } else {
        lightColorScheme(
            primary = appColors.brand,
            onPrimary = appColors.onBrand,
            primaryContainer = Color(0xFFFFE3E1),
            onPrimaryContainer = appColors.brandDeep,
            secondary = appColors.accent,
            onSecondary = Charcoal,
            secondaryContainer = appColors.accentSoft,
            tertiary = appColors.success,
            background = appColors.background,
            onBackground = appColors.textPrimary,
            surface = appColors.surface,
            onSurface = appColors.textPrimary,
            surfaceVariant = appColors.surfaceSunken,
            onSurfaceVariant = appColors.textSecondary,
            outline = appColors.outline,
            error = appColors.danger,
            onError = Color.White,
        )
    }

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}

object AppTheme {
    val colors: AppColors
        @Composable @ReadOnlyComposable
        get() = LocalAppColors.current

    val spacing: Spacing = Spacing
}
