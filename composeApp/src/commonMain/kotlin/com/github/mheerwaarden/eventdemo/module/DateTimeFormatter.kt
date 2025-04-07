package com.github.mheerwaarden.eventdemo.module

import kotlinx.datetime.LocalDateTime

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