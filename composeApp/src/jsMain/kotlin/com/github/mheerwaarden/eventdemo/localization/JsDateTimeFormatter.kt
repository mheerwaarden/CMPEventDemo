package com.github.mheerwaarden.eventdemo.localization

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toInstant
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class JsDateTimeFormatter : DateTimeFormatter, KoinComponent {
    private val platformLocaleProvider: PlatformLocaleProvider by inject()

    /**
     * Returns the locale to use for formatting. Returns JavaScript 'undefined' if not locale is
     * set, since that implies the system default in the formatting functions.
     */
    private fun getLocaleForFormatting(): dynamic {
        return platformLocaleProvider.getCurrentLocaleTag() ?: jsUndefined
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
        val locale = getLocaleForFormatting()
        val optionsForHourCycleCheck = localeOptions { hour("numeric") }
        val resolvedOptions =
            js("new Intl.DateTimeFormat(locale, optionsForHourCycleCheck).resolvedOptions()")

        // Access hourCycle from the Kotlin dynamic variable
        // Convert to String? in Kotlin for safer comparison
        val hourCycle = js("resolvedOptsDynamic.hourCycle") as? String

        return when (hourCycle) {
            "h23", "h24" -> true  // Explicitly 24-hour
            "h11", "h12" -> false // Explicitly 12-hour
            null -> true          // Undefined/null defaults to 24-hour
            else -> true          // For any other unexpected hourCycle string, default to 24-hour
        }
    }


}