package com.github.mheerwaarden.eventdemo.localization

external interface LocaleOptions : JsAny {
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

    // DateTimeFormat Options
    /** Valid dateStyle values: "full", "long", "medium", "short". */
    var dateStyle: String?
    /** Valid timeStyle values: "full", "long", "medium", "short". */
    var timeStyle: String?
    /** The time zone to use. Common values: "UTC", "America/New_York". Default is the runtime's default time zone. */
    var timeZone: String?
    /** Valid hourCycle values: "h11", "h12", "h23", "h24". Default depends on locale. */
    var hourCycle: String?
    /** Valid timeZoneName values: "short", "long", "shortOffset", "longOffset", "shortGeneric", "longGeneric". */
    var timeZoneName: String?
    /** Valid year values: "numeric", "2-digit". */
    var year: String?
    /** Valid month values: "numeric", "2-digit", "long", "short", "narrow". */
    var month: String?
    /** Valid day values: "numeric", "2-digit". */
    var day: String?
    /** Valid hour values: "numeric", "2-digit". */
    var hour: String?
    /** Valid minute values: "numeric", "2-digit". */
    var minute: String?
    /** Valid second values: "numeric", "2-digit". */
    var second: String?
    /** Valid weekday values: "long", "short", "narrow". */
    var weekday: String?
    /** Valid era values: "long", "short", "narrow". */
    var era: String?
    /** Valid dayPeriod values: "narrow", "short", "long". Used with hc: "h11" or "h12". */
    var dayPeriod: String?
    /** Valid fractionalSecondDigits values: 1, 2, or 3. */
    var fractionalSecondDigits: Int?
}

/**
 * Convenience function to create Intl options using a lambda with receiver, similar to
 * Date.dateLocaleOptions() in Kotlin.js.
 *
 * Example:
 * val options = localeOptions {
 *    year= "numeric"
 *    month= "long"
 *    day= "numeric"
 * }
 * fun now(options: LocaleOptions): String = js("new Date().toLocaleDateString(options)")
 *
 * @return options object for usage in JavaScript toLocaleString() functions
 */
fun localeOptions(init: LocaleOptions.() -> Unit): LocaleOptions {
    val result = createLocaleOptionsJs()
    init(result)
    return result
}

private fun createLocaleOptionsJs(): LocaleOptions = js("new Object()")
