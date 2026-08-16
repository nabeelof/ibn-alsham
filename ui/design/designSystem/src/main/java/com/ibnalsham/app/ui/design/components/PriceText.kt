package com.ibnalsham.app.ui.design.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import com.ibnalsham.app.core.priceFormatter.JodPriceFormatter

/**
 * Formats an amount as Jordanian dinars, e.g. "12.500 د.أ" / "12.500 JOD".
 * Always three decimals — the dinar has 1000 fils.
 */
@Composable
@ReadOnlyComposable
fun jodPrice(amount: Double): String =
    JodPriceFormatter.format(amount, arabic = isArabicLocale())

/** Signed variant for add-ons and discounts: "+0.500 د.أ", "-2.000 د.أ". */
@Composable
@ReadOnlyComposable
fun jodPriceSigned(amount: Double): String =
    JodPriceFormatter.formatSigned(amount, arabic = isArabicLocale())

@Composable
@ReadOnlyComposable
private fun isArabicLocale(): Boolean =
    LocalConfiguration.current.locales[0]?.language == "ar"

@Composable
fun PriceText(
    amount: Double,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = FontWeight.Bold,
    style: TextStyle = LocalTextStyle.current,
) {
    Text(
        text = jodPrice(amount),
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
        style = style,
    )
}

/** The struck-through list price shown next to a discounted price. */
@Composable
fun StrikethroughPriceText(
    amount: Double,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = TextUnit.Unspecified,
) {
    Text(
        text = jodPrice(amount),
        modifier = modifier,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = fontSize,
        textDecoration = TextDecoration.LineThrough,
    )
}
