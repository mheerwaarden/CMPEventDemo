package com.github.mheerwaarden.eventdemo.module

import android.os.Build
import androidx.annotation.RequiresApi
import com.github.mheerwaarden.eventdemo.localization.NameStyle
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import java.time.ZoneId
import java.time.format.FormatStyle
import java.util.Locale
import java.time.LocalDateTime as JavaLocalDateTime
import java.time.format.DateTimeFormatter as JavaDateTimeFormatter

class AndroidDateTimeFormatter : DateTimeFormatter {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun format(dateTime: LocalDateTime, pattern: String): String {
        val zoneId = ZoneId.systemDefault()
        // Convert kotlinx LocalDateTime to java ZonedDateTime
        val zonedDateTime = java.time.LocalDateTime.of(
            dateTime.year,
            dateTime.monthNumber,
            dateTime.dayOfMonth,
            dateTime.hour,
            dateTime.minute,
            dateTime.second,
            dateTime.nanosecond
        ).atZone(zoneId)
        val formatter = java.time.format.DateTimeFormatter.ofPattern(pattern, Locale.getDefault())
        return zonedDateTime.format(formatter)
    }

    override fun getCurrentLocale(): String {
        return Locale.getDefault().toString()
    }
}

@RequiresApi(Build.VERSION_CODES.O)
actual fun toLocalizedDateTimeString(dateTime: LocalDateTime): String =
    JavaDateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG, FormatStyle.SHORT)
        .format(toJavaDateTime(dateTime.date, dateTime.time))

@RequiresApi(Build.VERSION_CODES.O)
actual fun toLocalizedDateString(date: LocalDate): String =
    JavaDateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
        .format(toJavaDateTime(date, null))

@RequiresApi(Build.VERSION_CODES.O)
actual fun toLocalizedTimeString(time: LocalTime): String =
    JavaDateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
        .format(toJavaDateTime(null, time))

@RequiresApi(Build.VERSION_CODES.O)
actual fun localizedMonthNames(style: NameStyle): List<String> {
    val pattern = if (style == NameStyle.ABBREVIATED) "LL" else "LLLL"
    val formatter = JavaDateTimeFormatter.ofPattern(pattern, Locale.getDefault())
    val monthNames = mutableListOf<String>()
    val javaDateTime = JavaLocalDateTime.of(1970, 1, 1, 0, 0)
    for (monthNumber in 1..12) {
        monthNames.add(formatter.format(javaDateTime.withMonth(monthNumber)))
    }
    return monthNames
}

@RequiresApi(Build.VERSION_CODES.O)
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
