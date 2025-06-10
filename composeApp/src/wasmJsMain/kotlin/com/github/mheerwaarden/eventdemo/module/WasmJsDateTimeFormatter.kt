package com.github.mheerwaarden.eventdemo.module

import com.github.mheerwaarden.eventdemo.localization.NameStyle
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toInstant

// Properties defined by js() must be top-level
private val browserLocale: String = js("navigator.language")

class WasmJsDateTimeFormatter : DateTimeFormatter {

    override fun format(dateTime: LocalDateTime, pattern: String): String {
        // Transform the LocalDateTime to millis
        val millis = dateTime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        return formatDateJs(millis, browserLocale)
    }

    override fun getCurrentLocale(): String {
        return browserLocale
    }
}

// Functions defined by js() must be top-level
fun formatDateJs(millis: Long, locale: String): String = js(
    """
    {
        const options = { dateStyle: 'full' };
        // or define explicit:
        // const options = { weekday: 'long', year: 'numeric', month: 'short', day: 'numeric' };
        
        // If you want the browser to automatically use the user’s locale, you can pass “undefined” as the first parameter.
        return new Date(Number(millis)).toLocaleDateString(locale, options);
    }
    """
)

actual fun toLocalizedDateTimeString(dateTime: LocalDateTime): String {
    println(">toLocalizedDateTimeString for dateTime: $dateTime")
    val result =
        formatDateTimeJs(dateTime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds())
    println("<toLocalizedDateTimeString result: $result")
    return result
}

actual fun toLocalizedDateString(date: LocalDate): String {
    println(">toLocalizedDateString for date: $date")
    val result = formatDateJs(date.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds())
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

/*
1. Functions defined by js() must be top-level
2. When you pass a Kotlin Long to a JavaScript function via js(...), the Kotlin/Wasm runtime
   converts it to a JavaScript BigInt if the number is large enough to potentially exceed
   JavaScript's Number.MAX_SAFE_INTEGER. However, the new Date(value) constructor in JavaScript
   expects its value argument to be a standard JavaScript Number, therefore an explicit conversion
   of the BigInt back to a Number is necessary.
3. For sensible error messages, add a try-catch around the JS call with logging in the catch:
    `console.error("[JS] Error in formatDateJs:", e_js.message, e_js.stack);`
 */

private fun formatDateTimeJs(millis: Long): String = js(
    """{
        const locale = window.navigator.languages[0] || undefined;
        const options = {
            year: "numeric",
            month: "long",
            day: "numeric",
            hour: "numeric",
            minute: "2-digit"
        };
        return new Date(Number(millis)).toLocaleString(locale, options);
    }"""
)

private fun formatDateJs(millis: Long): String = js(
    """{
        const locale = window.navigator.languages[0] || undefined;
        const options = {       
            year: "numeric",
            month: "long",
            day: "numeric"
        };
        return new Date(Number(millis)).toLocaleDateString(locale, options);
    }"""
)

private fun formatTimeJs(millis: Long): String = js(
    """{        
        const locale = window.navigator.languages[0] || undefined;
        const options = {
            hour: "numeric",
            minute: "2-digit"
        };
        return new Date(Number(millis)).toLocaleTimeString(locale, options);
    }"""
)

private fun getMonthName(monthNumber: Int, isFull: Boolean): String = js(
    """{
        const locale = window.navigator.languages[0] || undefined;
        let options;
        if (isFull) {
            options = { month: "long" };
        } else {
            options = { month: "medium" };
        }
        const formatter = Intl.DateTimeFormat(locale, options);
        return formatter.format(new Date(2003, monthNumber - 1, 12));
    }"""
)