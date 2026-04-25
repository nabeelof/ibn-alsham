package com.ibnalsham.app.ui.design.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val IbnAlshamRed = Color(0xFFC62828)
val IbnAlshamGold = Color(0xFFFFD54F)
val IbnAlshamBlack = Color(0xFF1A1A1A)

private val LightColorScheme = lightColorScheme(
    primary = IbnAlshamRed,
    secondary = IbnAlshamGold,
    tertiary = Color(0xFF8D6E63),
    background = Color(0xFFFAFAFA),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = IbnAlshamBlack,
    onSurface = IbnAlshamBlack
)

private val DarkColorScheme = darkColorScheme(
    primary = IbnAlshamRed,
    secondary = IbnAlshamGold,
    tertiary = Color(0xFF8D6E63),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun IbnAlshamTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
