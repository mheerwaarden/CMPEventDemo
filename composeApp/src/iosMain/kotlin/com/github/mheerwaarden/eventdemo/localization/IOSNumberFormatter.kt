package com.github.mheerwaarden.eventdemo.localization

import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterCurrencyStyle

class IOSNumberFormatter : NumberFormatter {
    override fun formatDecimal(
        number: Double,
        minIntegerDigits: Int,
        minFractionDigits: Int,
        maxFractionDigits: Int
    ): String {
        val formatter = NSNumberFormatter()
        formatter.minimumIntegerDigits = minIntegerDigits.toULong()
        formatter.minimumFractionDigits = minFractionDigits.toULong()
        formatter.maximumFractionDigits = maxFractionDigits.toULong()
        formatter.numberStyle = platform.Foundation.NSNumberFormatterDecimalStyle
        return formatter.stringFromNumber(NSNumber(number)) ?: ""
    }

    override fun formatCurrency(
        amount: Double,
        currencyCode: String,
        minIntegerDigits: Int,
        minFractionDigits: Int,
        maxFractionDigits: Int
    ): String {
        val formatter = NSNumberFormatter()
        formatter.numberStyle = NSNumberFormatterCurrencyStyle
        formatter.currencyCode = currencyCode
        formatter.minimumIntegerDigits = minIntegerDigits.toULong()
        formatter.minimumFractionDigits = minFractionDigits.toULong()
        formatter.maximumFractionDigits = maxFractionDigits.toULong()
        return formatter.stringFromNumber(NSNumber(amount)) ?: ""
    }
}