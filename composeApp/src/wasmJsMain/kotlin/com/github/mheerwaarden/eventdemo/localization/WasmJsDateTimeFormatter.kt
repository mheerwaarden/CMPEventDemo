package com.github.mheerwaarden.eventdemo.localization

import com.github.mheerwaarden.eventdemo.util.format
import com.github.mheerwaarden.eventdemo.util.formatDateTime
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
3. For sensible error messages, add a try-catch around the JS call with logging in the catch:
    `console.error("[JS] Error in formatDateJs:", e_js.message, e_js.stack);`
*/
class WasmJsDateTimeFormatter : DateTimeFormatter, KoinComponent {
    private val platformLocaleProvider: PlatformLocaleProvider by inject()

    /**
     * Returns the locale to use for formatting. Returns JavaScript 'undefined' if not locale is
     * set, since that implies the system default in the formatting functions.
     */
    private fun getLocaleForFormatting(): String? = platformLocaleProvider.getPlatformLocaleTag()

    override fun formatDateTime(dateTime: LocalDateTime): String {
        try {
            val millis = dateTime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
            val locale = getLocaleForFormatting()
            val options = localeOptions {
                year = "numeric"
                month = "long"
                day = "numeric"
                hour = "numeric"
                minute = "2-digit"
            }
            // Using toLocaleString for both date and time
            return toLocaleStringJs(millis, locale, options)
        } catch (e: Exception) {
            println("formatDateTime caught Exception: ${e.message}")
            return dateTime.formatDateTime()
        }
    }

    override fun formatDate(date: LocalDate): String {
        try {
            val millis = date.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
            val locale = getLocaleForFormatting()
            val options = localeOptions {
                year = "numeric"
                month = "long"
                day = "numeric"
            }
            // Using toLocaleDateString for date-only
            return toLocaleDateStringJs(millis, locale, options)
        } catch (e: Exception) {
            println("formatDate caught Exception: ${e.message}")
            return date.format()
        }
    }

    override fun formatTime(time: LocalTime): String {
        try {
            val dateTimeOnEpochDay = LocalDateTime(LocalDate(1970, 1, 1), time)
            val millis = dateTimeOnEpochDay.toInstant(TimeZone.UTC).toEpochMilliseconds()
            val locale = getLocaleForFormatting()
            val options = localeOptions {
                hour = "numeric"
                minute = "2-digit"
            }
            // Using toLocaleTimeString for time-only
            return toLocaleTimeStringJs(millis, locale, options)
        } catch (e: Exception) {
            println("formatDateTime caught Exception: ${e.message}")
            return time.format()
        }
    }

    override fun localizedMonthNames(style: NameStyle): List<String> {
        try {
            val locale = getLocaleForFormatting()
            val isFullOption = if (style == NameStyle.FULL) "long" else "short"
            val options = localeOptions {
                month = isFullOption
            }
            val monthNames = mutableListOf<String>()
            for (monthNumber in 1..12) {
                monthNames.add(
                    getMonthNameJs(monthNumber, locale, options)
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

    override fun is24HourFormat(): Boolean {
        // Format the hour with the current locale and find out if it contains "23".
        val referenceHour = 23
        val locale = getLocaleForFormatting()
        val options = localeOptions {
            hour = "numeric"
        }
        val formattedHourString: String = getFormattedHourJs(referenceHour, locale, options)

        // Heuristic: If the formatted string for the 23rd hour contains "23",
        // For 12-hour format, "23:00" would typically be formatted as "11 PM", "11", etc.
        return formattedHourString.contains(referenceHour.toString())
    }
}

/* js functions mut be top-level */

@Suppress("UNUSED_PARAMETER")
private fun toLocaleStringJs(millis: Long, locale: String?, options: LocaleOptions?): String {
    js(
        """
    try {
        return new Date(Number(millis)).toLocaleString(locale, options)
    } catch (e_js) {
        console.error("[JS] Error in toLocaleStringJs:", e_js.message, e_js.stack);
        throw e_js
    }
    """
    )
}

@Suppress("UNUSED_PARAMETER")
private fun toLocaleDateStringJs(millis: Long, locale: String?, options: LocaleOptions?): String {
    js(
        """
    try {
        return new Date(Number(millis)).toLocaleDateString(locale, options)
    } catch (e_js) {
        console.error("[JS] Error in toLocaleDateStringJs:", e_js.message, e_js.stack);
        throw e_js
    }
    """
    )
}

@Suppress("UNUSED_PARAMETER")
private fun toLocaleTimeStringJs(millis: Long, locale: String?, options: LocaleOptions?): String {
    js(
        """
    try {
        return new Date(Number(millis)).toLocaleTimeString(locale, options)
    } catch (e_js) {
        console.error("[JS] Error in toLocaleTimeStringJs:", e_js.message, e_js.stack);
        throw e_js
    }
    """
    )
}

@Suppress("UNUSED_PARAMETER")
private fun getMonthNameJs(monthNumber: Int, locale: String?, options: LocaleOptions?): String {
    js(
        """
    try {
        return new Intl.DateTimeFormat(locale, options).format(new Date(2003, monthNumber - 1, 12))
    } catch (e_js) {
        console.error("[JS] Error in getMonthNameJs:", e_js.message, e_js.stack);
        throw e_js
    }
    """
    )
}

@Suppress("UNUSED_PARAMETER")
private fun getFormattedHourJs(hour: Int, locale: String?, options: LocaleOptions?): String {
    js(
        """
    try {
        return new Date(1970, 0, 1, hour, 0).toLocaleTimeString(locale, options)
    } catch (e_js) {
        console.error("[JS] Error in getFormattedHourJs:", e_js.message, e_js.stack);
        throw e_js
    }
    """
    )
}


