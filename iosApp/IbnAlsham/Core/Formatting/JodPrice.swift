import Foundation
import shared

/// Money formatting for the Jordanian dinar.
///
/// The dinar has 1000 fils, so every price carries three decimals. This wraps
/// the shared `JodPriceFormatter` so Swift and Kotlin never disagree on a total.
enum JodPrice {
    /// "12.500 د.أ" in Arabic, "12.500 JOD" otherwise.
    static func format(_ amount: Double) -> String {
        JodPriceFormatter.shared.format(amount: amount, arabic: isArabic)
    }

    /// "+0.500 د.أ" / "-2.000 د.أ" — used for add-ons and discounts.
    static func formatSigned(_ amount: Double) -> String {
        JodPriceFormatter.shared.formatSigned(amount: amount, arabic: isArabic)
    }

    /// The bare number, no currency symbol.
    static func amount(_ amount: Double) -> String {
        JodPriceFormatter.shared.formatAmount(amount: amount)
    }

    static var symbol: String {
        JodPriceFormatter.shared.symbol(arabic: isArabic)
    }

    private static var isArabic: Bool {
        Locale.current.language.languageCode?.identifier == "ar"
    }
}
