package com.github.mheerwaarden.eventdemo.module

import com.github.mheerwaarden.eventdemo.localization.NameStyle
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime

interface DateTimeFormatter {
    /**
     * Format a LocalDateTime according to locale settings
     * @param dateTime in the current system time zone, to be formatted
     * @param pattern in java.util. style defining the format
     * @return the formatted dateTime as string
     */
    fun format(dateTime: LocalDateTime, pattern: String): String

    fun getCurrentLocale(): String
}

expect fun toLocalizedDateTimeString(dateTime: LocalDateTime) : String
expect fun toLocalizedDateString(date: LocalDate) : String
expect fun toLocalizedTimeString(time: LocalTime) : String


expect fun localizedMonthNames(style: NameStyle): List<String>
