package com.github.mheerwaarden.eventdemo.localization

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WasmJsNumberFormatter : NumberFormatter, KoinComponent {
    private val platformLocaleProvider: PlatformLocaleProvider by inject()

    private fun getLocaleForFormatting(): String? = platformLocaleProvider.getPlatformLocaleTag()

    override fun formatDecimal(
        number: Double,
        minIntegerDigits: Int,
        minFractionDigits: Int,
        maxFractionDigits: Int
    ): String {
        val locale = getLocaleForFormatting()
        val options = localeOptions {
            style = "decimal"
            minimumIntegerDigits = minIntegerDigits
            minimumFractionDigits = minFractionDigits
            maximumFractionDigits = maxFractionDigits
        }
        return toLocaleNumberString(number, locale, options)
    }

    override fun formatCurrency(
        amount: Double,
        currencyCode: String,
        minIntegerDigits: Int,
        minFractionDigits: Int,
        maxFractionDigits: Int
    ): String {
        val locale = getLocaleForFormatting()
        val options = localeOptions {
            style = "currency"
            currency = currencyCode
            minimumIntegerDigits = minIntegerDigits
            minimumFractionDigits = minFractionDigits
            maximumFractionDigits = maxFractionDigits
        }
        return toLocaleCurrencyString(amount, locale, options)
    }

}

/* js functions mut be top-level */

@Suppress("UNUSED_PARAMETER")
private fun toLocaleNumberString(number: Double, locale: String?, options: LocaleOptions?): String =
    js("Number(number).toLocaleString(locale, options)")

@Suppress("UNUSED_PARAMETER")
private fun toLocaleCurrencyString(amount: Double, locale: String?, options: LocaleOptions?): String =
    js("Intl.NumberFormat(locale, options).format(amount)")
