package com.github.mheerwaarden.eventdemo.module

import com.github.mheerwaarden.eventdemo.localization.NameStyle
import kotlinx.browser.window
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toInstant

class JsDateTimeFormatter : DateTimeFormatter {

    override fun format(dateTime: LocalDateTime, pattern: String): String {
        // Transform the LocalDateTime to millis
        val millis = dateTime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        return formatDateJs(millis, getCurrentBrowserLocale())
    }

    fun is24HourFormat(): Boolean = is24HourFormatJsImpl(getCurrentBrowserLocale())

    private fun is24HourFormatJsImpl(locale: dynamic): Boolean {
        val resolvedOptions = getResolvedOptions(locale)
        return resolvedOptions?.hourCycle == null
                || resolvedOptions.hourCycle == "h23" || resolvedOptions.hourCycle == "h24"
    }


    @Suppress("UNUSED_PARAMETER")
    private fun getResolvedOptions(locale: dynamic): dynamic =
        js("new Intl.DateTimeFormat(locale, { hour: 'numeric' }).resolvedOptions()")

    override fun getCurrentLocale(): String {
        return getCurrentBrowserLocale() as String
    }
}

// Functions defined by js() must be top-level

@Suppress("UNUSED_PARAMETER", "UNUSED_VARIABLE")
fun formatDateJs(millis: Long, locale: dynamic): String {
    val options = js("{ dateStyle: 'full' }")
    // or define explicit:
    // options = { weekday: 'long', year: 'numeric', month: 'short', day: 'numeric' };
    return js("new Date(Number(millis)).toLocaleDateString(locale, options)") as String
}

actual fun toLocalizedDateTimeString(dateTime: LocalDateTime): String {
    println(">toLocalizedDateTimeString for dateTime: $dateTime")
    val result =
        formatDateTimeJs(dateTime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds())
    println("<toLocalizedDateTimeString result: $result")
    return result
}

actual fun toLocalizedDateString(date: LocalDate): String {
    println(">toLocalizedDateString for date: $date")
    val result =
        formatDateJs(date.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds())
    println("<toLocalizedDateString result: $result")
    return result
}

actual fun toLocalizedTimeString(time: LocalTime): String {
    println(">toLocalizedTimeString for time: $time")
    val dateTime = LocalDateTime(1970, 1, 1, time.hour, time.minute)
    val result =
        formatTimeJs(dateTime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds())
    println(">toLocalizedTimeString result: $result")
    return result
}

actual fun localizedMonthNames(style: NameStyle): List<String> {
    try {
        val isFull = style == NameStyle.FULL
        val monthNames = mutableListOf<String>()
        for (monthNumber in 1..12) {
            monthNames.add(getMonthName(monthNumber, isFull))
        }
        return monthNames
    } catch (e: Throwable) {
        println("Kotlin caught Throwable: ${e.message}")
        println("Kotlin stack trace:\n${e.stackTraceToString()}")
        return if (style == NameStyle.FULL) {
            listOf(
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
            )
        } else {
            listOf(
                "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
            )
        }
    }
}

private fun getCurrentBrowserLocale(): dynamic {
    return if (window.navigator.languages.isNotEmpty()) {
        window.navigator.languages[0]
    } else {
        undefined
    }
}

/*
1. Functions defined by js() must be top-level
2. When you pass a Kotlin Long to a JavaScript function via js(...), the Kotlin/Wasm runtime
   converts it to a JavaScript BigInt if the number is large enough to potentially exceed
   JavaScript's Number.MAX_SAFE_INTEGER. However, the new Date(value) constructor in JavaScript
   expects its value argument to be a standard JavaScript Number, therefore an explicit conversion
   of the BigInt back to a Number is necessary.
3. Returning a value from a multiline JavaScript block is not supported. Therefore, use kotlin
   statements to provide the values for the final return statement.
4. For sensible error messages, add a try-catch around the JS call with logging in the catch:
    `console.error("[JS] Error in formatDateJs:", e_js.message, e_js.stack);`
 */

private const val yearOption = "numeric"
private const val monthOption = "long"
private const val dayOption = "numeric"
private const val hourOption = "numeric"
private const val minuteOption = "2-digit"
private const val dateOptions = "year: '$yearOption', month: '$monthOption', day: '$dayOption'"
private const val timeOptions = "hour: '$hourOption', minute: '$minuteOption'"

@Suppress("UNUSED_PARAMETER", "UNUSED_VARIABLE")
private fun formatDateTimeJs(millis: Long): String {
    val locale = getCurrentBrowserLocale()
    val options = js("({ $dateOptions, $timeOptions })")
    return js("new Date(Number(millis)).toLocaleString(locale, options)") as String
}

@Suppress("UNUSED_PARAMETER", "UNUSED_VARIABLE")
private fun formatDateJs(millis: Long): String {
    val locale = getCurrentBrowserLocale()
    val options = js("({ $dateOptions })")
    return js("new Date(Number(millis)).toLocaleString(locale, options)") as String
}

@Suppress("UNUSED_PARAMETER", "UNUSED_VARIABLE")
private fun formatTimeJs(millis: Long): String {
    val locale = getCurrentBrowserLocale()
    val options = js("({ $timeOptions })")
    return js("new Date(Number(millis)).toLocaleString(locale, options)") as String
}

@Suppress("UNUSED_PARAMETER", "UNUSED_VARIABLE")
private fun getMonthName(monthNumber: Int, isFull: Boolean): String {
    val locale = getCurrentBrowserLocale()
    val isFullOption = if (isFull) "long" else "short"
    val options = if (isFull) {
        js("({ month: 'long' })")
    } else {
        js("({ month: 'short' })")
    }
    return js("new Intl.DateTimeFormat(locale, options).format(new Date(2003, monthNumber - 1, 12))") as String
}