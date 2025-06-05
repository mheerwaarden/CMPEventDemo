package com.github.mheerwaarden.eventdemo.localization

/**
 * A builder for creating JavaScript options objects suitable for `Intl.DateTimeFormat`
 * and `Intl.NumberFormat` constructors.
 *
 * This uses a Builder pattern to allow for a fluent and type-safe way of setting
 * various optional formatting properties.
 */
class LocaleOptionsBuilder {
    private val optionsMap = mutableMapOf<String, Any>()

    /** Valid localeMatcher values: "lookup", "best fit". Default is "best fit". */
    fun localeMatcher(value: String): LocaleOptionsBuilder = apply { optionsMap["localeMatcher"] = value }

    // --- NumberFormat Options ---

    /** Valid style values: "decimal", "currency", "percent", "unit". */
    fun style(value: String): LocaleOptionsBuilder = apply { optionsMap["style"] = value }

    /** Specifies the currency to use in currency formatting. Valid values are ISO 4217 currency codes, e.g., "USD", "EUR", "JPY". */
    fun currency(value: String): LocaleOptionsBuilder = apply { optionsMap["currency"] = value }

    /** Valid currencyDisplay values: "symbol", "narrowSymbol", "code", "name". Default is "symbol". */
    fun currencyDisplay(value: String): LocaleOptionsBuilder = apply { optionsMap["currencyDisplay"] = value }

    /** Valid currencySign values: "standard", "accounting". Default is "standard". */
    fun currencySign(value: String): LocaleOptionsBuilder = apply { optionsMap["currencySign"] = value }

    /** Whether to use grouping separators, such as thousands separators or thousand/lakh/crore separators. Default is true. */
    fun useGrouping(value: Boolean): LocaleOptionsBuilder = apply { optionsMap["useGrouping"] = value }

    /** The minimum number of integer digits to use. Possible values are from 1 to 21. Default is 1. */
    fun minimumIntegerDigits(value: Int): LocaleOptionsBuilder = apply { optionsMap["minimumIntegerDigits"] = value }

    /** The minimum number of fraction digits to use. Possible values are from 0 to 20. Default varies by currency/style. */
    fun minimumFractionDigits(value: Int): LocaleOptionsBuilder = apply { optionsMap["minimumFractionDigits"] = value }

    /** The maximum number of fraction digits to use. Possible values are from 0 to 20. Default varies by currency/style. */
    fun maximumFractionDigits(value: Int): LocaleOptionsBuilder = apply { optionsMap["maximumFractionDigits"] = value }

    /** The minimum number of significant digits to use. Possible values are from 1 to 21. Default is 1. */
    fun minimumSignificantDigits(value: Int): LocaleOptionsBuilder = apply { optionsMap["minimumSignificantDigits"] = value }

    /** The maximum number of significant digits to use. Possible values are from 1 to 21. Default is 21. */
    fun maximumSignificantDigits(value: Int): LocaleOptionsBuilder = apply { optionsMap["maximumSignificantDigits"] = value }

    /** Valid notation values: "standard", "scientific", "engineering", "compact". Default is "standard". */
    fun notation(value: String): LocaleOptionsBuilder = apply { optionsMap["notation"] = value }

    /** Used only when notation is "compact". Valid values: "short", "long". Default is "short". */
    fun compactDisplay(value: String): LocaleOptionsBuilder = apply { optionsMap["compactDisplay"] = value }

    // --- DateTimeFormat Options ---

    /** Valid dateStyle values: "full", "long", "medium", "short". */
    fun dateStyle(value: String): LocaleOptionsBuilder = apply { optionsMap["dateStyle"] = value }

    /** Valid timeStyle values: "full", "long", "medium", "short". */
    fun timeStyle(value: String): LocaleOptionsBuilder = apply { optionsMap["timeStyle"] = value }

    /** The time zone to use. Common values: "UTC", "America/New_York". Default is the runtime's default time zone. */
    fun timeZone(value: String): LocaleOptionsBuilder = apply { optionsMap["timeZone"] = value }

    /** Valid hourCycle values: "h11", "h12", "h23", "h24". Default depends on locale. */
    fun hourCycle(value: String): LocaleOptionsBuilder = apply { optionsMap["hourCycle"] = value }

    /** Valid timeZoneName values: "short", "long", "shortOffset", "longOffset", "shortGeneric", "longGeneric". */
    fun timeZoneName(value: String): LocaleOptionsBuilder = apply { optionsMap["timeZoneName"] = value }

    /** Valid year values: "numeric", "2-digit". */
    fun year(value: String): LocaleOptionsBuilder = apply { optionsMap["year"] = value }

    /** Valid month values: "numeric", "2-digit", "long", "short", "narrow". */
    fun month(value: String): LocaleOptionsBuilder = apply { optionsMap["month"] = value }

    /** Valid day values: "numeric", "2-digit". */
    fun day(value: String): LocaleOptionsBuilder = apply { optionsMap["day"] = value }

    /** Valid hour values: "numeric", "2-digit". */
    fun hour(value: String): LocaleOptionsBuilder = apply { optionsMap["hour"] = value }

    /** Valid minute values: "numeric", "2-digit". */
    fun minute(value: String): LocaleOptionsBuilder = apply { optionsMap["minute"] = value }

    /** Valid second values: "numeric", "2-digit". */
    fun second(value: String): LocaleOptionsBuilder = apply { optionsMap["second"] = value }

    /** Valid weekday values: "long", "short", "narrow". */
    fun weekday(value: String): LocaleOptionsBuilder = apply { optionsMap["weekday"] = value }

    /** Valid era values: "long", "short", "narrow". */
    fun era(value: String): LocaleOptionsBuilder = apply { optionsMap["era"] = value }

    /** Valid dayPeriod values: "narrow", "short", "long". Used with hc: "h11" or "h12". */
    fun dayPeriod(value: String): LocaleOptionsBuilder = apply { optionsMap["dayPeriod"] = value }

    /** Valid fractionalSecondDigits values: 1, 2, or 3. */
    fun fractionalSecondDigits(value: Int): LocaleOptionsBuilder = apply { optionsMap["fractionalSecondDigits"] = value }


    /**
     * Builds the JavaScript options object.
     *
     * @return A dynamic JavaScript object representing the configured options,
     *         or `jsUndefined` if no options were set.
     */
    fun build(): dynamic {
        if (optionsMap.isEmpty()) {
            return jsUndefined
        }

        val jsOptions: dynamic = js("({})") // Create an empty JavaScript object

        @Suppress("UNUSED_VARIABLE") // v is used in the js() call
        for ((key, value) in optionsMap) {
            // Dynamically set properties on the JavaScript object
            // Capture value for JS context
            val v = value
            js("jsOptions[key] = v")
        }
        return jsOptions
    }
}

/**
 * Convenience function to start building Intl options using a lambda with receiver.
 *
 * Example:
 * val options = intlOptions {
 *    year("numeric")
 *    month("long")
 *    day("numeric")}
 */
fun localeOptions(init: LocaleOptionsBuilder.() -> Unit): dynamic {
    return LocaleOptionsBuilder().apply(init).build()
}