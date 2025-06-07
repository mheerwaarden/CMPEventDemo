package com.github.mheerwaarden.eventdemo.localization

import java.text.DecimalFormat
import java.util.Locale

class DesktopNumberFormatter : NumberFormatter {
    override fun formatDecimal(
        number: Double,
        minIntegerDigits: Int,
        minFractionDigits: Int,
        maxFractionDigits: Int
    ): String {
        val decimalFormat = DecimalFormat.getInstance(Locale.getDefault()) as DecimalFormat
        decimalFormat.minimumIntegerDigits = minIntegerDigits
        decimalFormat.minimumFractionDigits = minFractionDigits
        decimalFormat.maximumFractionDigits = maxFractionDigits
        return decimalFormat.format(number)
    }
}