package com.ibnalsham.app.core.priceFormatter

import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * Money formatting for the Jordanian dinar.
 *
 * The dinar is subdivided into 1000 fils, so prices carry three decimal places
 * everywhere — the API stores `decimal(10,3)` and the UI must not round to two.
 */
object JodPriceFormatter {

    const val CURRENCY_CODE: String = "JOD"
    const val SYMBOL_AR: String = "د.أ"
    const val SYMBOL_EN: String = "JOD"

    /** Fils per dinar. */
    private const val FILS_PER_DINAR = 1_000L
    private const val DECIMALS = 3

    /**
     * "12.500" — the bare number, always with three decimals and no symbol.
     */
    fun formatAmount(amount: Double): String {
        val negative = amount < 0
        val fils = (abs(amount) * FILS_PER_DINAR).roundToLong()
        val dinars = fils / FILS_PER_DINAR
        val remainder = fils % FILS_PER_DINAR
        val fraction = remainder.toString().padStart(DECIMALS, '0')
        val sign = if (negative && fils != 0L) "-" else ""
        return "$sign$dinars.$fraction"
    }

    /**
     * "12.500 د.أ" in Arabic, "12.500 JOD" in English.
     */
    fun format(amount: Double, arabic: Boolean = true): String =
        "${formatAmount(amount)} ${symbol(arabic)}"

    /**
     * Signed form used for discounts and add-on prices: "-2.000 د.أ", "+0.500 د.أ".
     */
    fun formatSigned(amount: Double, arabic: Boolean = true): String {
        val prefix = when {
            amount > 0 -> "+"
            amount < 0 -> "-"
            else -> ""
        }
        return "$prefix${formatAmount(abs(amount))} ${symbol(arabic)}"
    }

    fun symbol(arabic: Boolean = true): String = if (arabic) SYMBOL_AR else SYMBOL_EN

    /**
     * Parse user input such as "12.5" or "١٢٫٥" into dinars, or null when unparseable.
     */
    fun parse(input: String): Double? {
        val normalized = buildString {
            for (char in input.trim()) {
                when {
                    char.isDigit() -> append(char)
                    // Arabic-Indic digits
                    char in '٠'..'٩' -> append(char - '٠')
                    char in '۰'..'۹' -> append(char - '۰')
                    char == '.' || char == ',' || char == '٫' -> append('.')
                    char == '-' && isEmpty() -> append(char)
                }
            }
        }
        return normalized.toDoubleOrNull()
    }

    /**
     * Round to the nearest fils. Use before sending money to the API so client
     * and server totals agree to the last decimal.
     */
    fun roundToFils(amount: Double): Double =
        (amount * FILS_PER_DINAR).roundToLong() / FILS_PER_DINAR.toDouble()
}

/** Convenience extension so call sites read as `price.toJod()`. */
fun Double.toJod(arabic: Boolean = true): String = JodPriceFormatter.format(this, arabic)

fun Double.toJodAmount(): String = JodPriceFormatter.formatAmount(this)
