package com.github.mheerwaarden.eventdemo.localization

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toInstant
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

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
class JsDateTimeFormatter : DateTimeFormatter, KoinComponent {
    private val platformLocaleProvider: PlatformLocaleProvider by inject()

    /**
     * Returns the locale to use for formatting. Returns JavaScript 'undefined' if not locale is
     * set, since that implies the system default in the formatting functions.
     */
    private fun getLocaleForFormatting(): dynamic {
        return platformLocaleProvider.getPlatformLocaleTag() ?: undefined
    }

    @Suppress("UNUSED_VARIABLE")
    override fun formatDateTime(dateTime: LocalDateTime): String {
        val millis = dateTime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        val locale = getLocaleForFormatting()
        val options = localeOptions {
            year("numeric")
            month("long")
            day("numeric")
            hour("numeric")
            minute("2-digit")
        }
        return js("new Date(Number(millis)).toLocaleString(locale, options)") as String
    }

    @Suppress("UNUSED_VARIABLE")
    override fun formatDate(date: LocalDate): String {
        val millis = date.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        val locale = getLocaleForFormatting()
        val options = localeOptions {
            year("numeric")
            month("long")
            day("numeric")
        }
        // Using toLocaleDateString for date-only
        return js("new Date(Number(millis)).toLocaleDateString(locale, options)") as String
    }

    @Suppress("UNUSED_VARIABLE")
    override fun formatTime(time: LocalTime): String {
        val dateTimeOnEpochDay = LocalDateTime(LocalDate(1970, 1, 1), time)
        val millis = dateTimeOnEpochDay.toInstant(TimeZone.UTC).toEpochMilliseconds() // Use UTC for fixed date
        val locale = getLocaleForFormatting()
        val options = localeOptions {
            hour("numeric")
            minute("2-digit")
        }
        // Using toLocaleTimeString for time-only
        return js("new Date(Number(millis)).toLocaleTimeString(locale, options)") as String
    }

    override fun localizedMonthNames(style: NameStyle): List<String> {
        try {
            val isFull = style == NameStyle.FULL
            val monthNames = mutableListOf<String>()
            for (monthNumber in 1..12) {
                monthNames.add(
                    getMonthName(monthNumber, isFull)
                )
            }
            return monthNames
        } catch (e: Throwable) {
            println("localizedMonthNames caught Throwable: ${e.message}")
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

    @Suppress("UNUSED_PARAMETER", "UNUSED_VARIABLE")
    private fun getMonthName(monthNumber: Int, isFull: Boolean): String {
        val locale = getLocaleForFormatting()
        val isFullOption = if (isFull) "long" else "short"
        val options = localeOptions {
            month(isFullOption)
        }
        return js("new Intl.DateTimeFormat(locale, options).format(new Date(2003, monthNumber - 1, 12))") as String
    }

    @Suppress("UNUSED_VARIABLE")
    override fun is24HourFormat(): Boolean {
        // Format the hour of 23h with the current locale and find out if it contains "23".
        val locale = getLocaleForFormatting()
        val options: dynamic = localeOptions {
            hour("numeric")
        }

        val formattedHourString: String = js(
            "new Date(1970, 0, 1, 23, 0).toLocaleTimeString(locale, options)"
        ) as String

        // Heuristic: If the formatted string for the 23rd hour contains "23",
        // For 12-hour format, "23:00" would typically be formatted as "11 PM", "11", etc.
        return formattedHourString.contains("23")
    }

}