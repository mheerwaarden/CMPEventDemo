package com.github.mheerwaarden.eventdemo.module

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import java.time.LocalDateTime as JavaLocalDateTime


actual fun toLocalizedDateTimeString(dateTime: LocalDateTime): String =
    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG, FormatStyle.SHORT)
        .format(toJavaDateTime(dateTime.date, dateTime.time))

actual fun toLocalizedDateString(date: LocalDate): String =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
        .format(toJavaDateTime(date, null))

actual fun toLocalizedTimeString(time: LocalTime): String =
    DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
        .format(toJavaDateTime(null, time))

actual fun localizedMonthNames(style: NameStyle): List<String> {
    val pattern = if (style == NameStyle.ABBREVIATED) "LL" else "LLLL"
    val formatter = DateTimeFormatter.ofPattern(pattern, Locale.getDefault())
    val monthNames = mutableListOf<String>()
    val javaDateTime = JavaLocalDateTime.of(1970, 1, 1, 0, 0)
    for (monthNumber in 1..12) {
        monthNames.add(formatter.format(javaDateTime.withMonth(monthNumber)))
    }
    return monthNames
}

private fun toJavaDateTime(date: LocalDate?, time: LocalTime?): JavaLocalDateTime {
    val year = date?.year ?: 1970
    val monthNumber = date?.monthNumber ?: 1
    val dayOfMonth = date?.dayOfMonth ?: 1
    val hour = time?.hour ?: 0
    val minute = time?.minute ?: 0
    val second = time?.second ?: 0
    val nanosecond = time?.nanosecond ?: 0
    return JavaLocalDateTime.of(year, monthNumber, dayOfMonth, hour, minute, second, nanosecond)
}

