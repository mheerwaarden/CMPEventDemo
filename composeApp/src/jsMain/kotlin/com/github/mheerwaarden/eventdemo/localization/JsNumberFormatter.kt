package com.github.mheerwaarden.eventdemo.localization

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class JsNumberFormatter : NumberFormatter, KoinComponent {
    private val platformLocaleProvider: PlatformLocaleProvider by inject()

    private fun getLocaleForFormatting(): dynamic {
        return platformLocaleProvider.getPlatformLocaleTag() ?: undefined
    }

    @Suppress("UNUSED_VARIABLE")
    override fun formatDecimal(
        number: Double,
        minIntegerDigits: Int,
        minFractionDigits: Int,
        maxFractionDigits: Int
    ): String {
        val locale = getLocaleForFormatting()
        val options = localeNumberOptions {
            style = "decimal"
            minimumIntegerDigits = minIntegerDigits
            minimumFractionDigits = minFractionDigits
            maximumFractionDigits = maxFractionDigits
        }
        return js("Number(number).toLocaleString(locale, options)") as String
    }

    @Suppress("UNUSED_VARIABLE")
    override fun formatCurrency(
        amount: Double,
        currencyCode: String,
        minIntegerDigits: Int,
        minFractionDigits: Int,
        maxFractionDigits: Int
    ): String {
        val locale = getLocaleForFormatting()
        val options = localeNumberOptions {
            style = "currency"
            currency = currencyCode
            minimumIntegerDigits = minIntegerDigits
            minimumFractionDigits = minFractionDigits
            maximumFractionDigits = maxFractionDigits
        }
        return js("Intl.NumberFormat(locale, options).format(amount)") as String
    }

}

external interface NumberOptions {
    // Common
    /** Valid style values: "decimal", "currency", "percent", "unit". */
    var localeMatcher: String?

    // NumberFormat Options
    /** Valid style values: "decimal", "currency", "percent", "unit". */
    var style: String?

    /** Specifies the currency to use in currency formatting. Valid values are ISO 4217 currency codes, e.g., "USD", "EUR", "JPY". */
    var currency: String?

    /** Valid currencyDisplay values: "symbol", "narrowSymbol", "code", "name". Default is "symbol". */
    var currencyDisplay: String?

    /** Valid currencySign values: "standard", "accounting". Default is "standard". */
    var currencySign: String?

    /** Whether to use grouping separators, such as thousands separators or thousand/lakh/crore separators. Default is true. */
    var useGrouping: Boolean?

    /** The minimum number of integer digits to use. Possible values are from 1 to 21. Default is 1. */
    var minimumIntegerDigits: Int?

    /** The minimum number of fraction digits to use. Possible values are from 0 to 20. Default varies by currency/style. */
    var minimumFractionDigits: Int?

    /** The maximum number of fraction digits to use. Possible values are from 0 to 20. Default varies by currency/style. */
    var maximumFractionDigits: Int?

    /** The minimum number of significant digits to use. Possible values are from 1 to 21. Default is 1. */
    var minimumSignificantDigits: Int?

    /** The maximum number of significant digits to use. Possible values are from 1 to 21. Default is 21. */
    var maximumSignificantDigits: Int?

    /** Valid notation values: "standard", "scientific", "engineering", "compact". Default is "standard". */
    var notation: String?

    /** Used only when notation is "compact". Valid values: "short", "long". Default is "short". */
    var compactDisplay: String?

}

/**
 * Convenience function to create Intl options using a lambda with receiver, similar to
 * Date.dateLocaleOptions() in Kotlin.js.
 *
 * Example:
 * val options = localeOptions {
 *     style= "decimal"
 *     minimumIntegerDigits= 2
 *     minimumFractionDigits= 1
 *     maximumFractionDigits= 3
 * }
 *
 * @return options object for usage in JavaScript toLocaleString() functions
 */
fun localeNumberOptions(init: NumberOptions.() -> Unit): NumberOptions {
    val result = js("new Object()").unsafeCast<NumberOptions>()
    init(result)
    return result
}
