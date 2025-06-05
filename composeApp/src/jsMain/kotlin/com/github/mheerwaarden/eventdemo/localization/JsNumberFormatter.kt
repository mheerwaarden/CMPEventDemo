package com.github.mheerwaarden.eventdemo.localization

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class JsNumberFormatter : NumberFormatter, KoinComponent {
    private val platformLocaleProvider: PlatformLocaleProvider by inject()

    private fun getLocaleForFormatting(): dynamic {
        return platformLocaleProvider.getCurrentLocaleTag() ?: jsUndefined
    }

    @Suppress("UNUSED_VARIABLE")
    override fun formatDecimal(
        number: Double,
        minIntegerDigits: Int,
        minFractionDigits: Int,
        maxFractionDigits: Int
    ): String {
        val locale = getLocaleForFormatting()
        val options = localeOptions {
            style("decimal")
            minimumIntegerDigits(minIntegerDigits)
            minimumFractionDigits(minFractionDigits)
            maximumFractionDigits(maxFractionDigits)
        }
        return js("Number(number).toLocaleString(locale, options)") as String
    }

}