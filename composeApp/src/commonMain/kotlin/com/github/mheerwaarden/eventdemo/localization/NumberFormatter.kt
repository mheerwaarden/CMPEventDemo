package com.github.mheerwaarden.eventdemo.localization

/**
 * Interface for locale-aware number formatting services.
 * Implementations should use the locale provided by [PlatformLocaleProvider].
 */
interface NumberFormatter {
    fun formatDecimal(
        number: Double,
        minIntegerDigits: Int = 1,
        minFractionDigits: Int = 0,
        maxFractionDigits: Int = 3 // Default sensible max for general decimals
    ): String

//    fun parseDecimal(formattedString: String): Double?

    // This default implementation requires the 'this' instance of NumberFormatter.
    fun parseDecimal(formattedString: String): Double? =
        CommonNumberParser.parseDecimalCommon(formattedString, this)

    // Could add formatInt, formatPercentage, formatCurrency etc. later
    // fun formatCurrency(amount: Double, currencyCode: String): String
}