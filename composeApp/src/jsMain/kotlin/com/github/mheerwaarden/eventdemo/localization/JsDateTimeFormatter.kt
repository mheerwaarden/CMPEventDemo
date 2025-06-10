package com.github.mheerwaarden.eventdemo.localization

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.js.Date


class JsDateTimeFormatter : DateTimeFormatter, KoinComponent {
    private val platformLocaleProvider: PlatformLocaleProvider by inject()

    /**
     * Returns the locale to use for formatting. Returns JavaScript 'undefined' if not locale is
     * set, since that implies the system default in the formatting functions.
     */
    private fun getLocaleForFormatting(): String =
        platformLocaleProvider.getPlatformLocaleTag() ?: undefined.toString()

    override fun formatDateTime(dateTime: LocalDateTime): String {
        val dateJs = Date(
            dateTime.year,
            dateTime.monthNumber,
            dateTime.dayOfMonth,
            dateTime.hour,
            dateTime.minute
        )
        return dateJs.toLocaleDateString(
            getLocaleForFormatting(),
            dateLocaleOptions {
                year = "numeric"; month = "long"; day = "numeric"
                hour = "numeric"; minute = "2-digit"
            }
        )
    }

    override fun formatDate(date: LocalDate): String {
        val dateJs = Date(date.year, date.monthNumber, date.dayOfMonth)
        return dateJs.toLocaleDateString(
            getLocaleForFormatting(),
            dateLocaleOptions { year = "numeric"; month = "long"; day = "numeric" }
        )
    }

    override fun formatTime(time: LocalTime): String {
        val date = Date(1970, 1, 1, time.hour, time.minute)
        return date.toLocaleTimeString(
            getLocaleForFormatting(),
            dateLocaleOptions { hour = "numeric"; minute = "2-digit" }
        )
    }

    override fun localizedMonthNames(style: NameStyle): List<String> {
        try {
            val locale = getLocaleForFormatting()
            val isFullOption = if (style == NameStyle.FULL) "long" else "short"
            val monthOption = dateLocaleOptions { month = isFullOption }
            val monthNames = mutableListOf<String>()
            for (monthNumber in 0..11) {
                val dateJs = Date(1970, monthNumber, 1)
                monthNames.add(
                    dateJs.toLocaleDateString(locale, monthOption)
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
        // Format the hour of 23h with the current locale and find out if it contains "23".
        val locale = getLocaleForFormatting()
        val options = dateLocaleOptions { hour = "numeric" }

        val formattedHourString = Date(1970, 0, 1, 23, 0)
            .toLocaleTimeString(locale, options)
        // Heuristic: If the formatted string for the 23rd hour contains "23",
        // For 12-hour format, "23:00" would typically be formatted as "11 PM", "11", etc.
        return formattedHourString.contains("23")
    }

}