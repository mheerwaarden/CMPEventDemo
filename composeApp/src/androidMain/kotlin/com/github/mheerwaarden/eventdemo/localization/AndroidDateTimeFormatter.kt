package com.github.mheerwaarden.eventdemo.localization

import android.content.Context
import android.os.Build
import android.text.format.DateFormat
import androidx.annotation.RequiresApi
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.format.FormatStyle
import java.util.Locale
import java.time.LocalDateTime as JavaLocalDateTime
import java.time.format.DateTimeFormatter as JavaDateTimeFormatter

class AndroidDateTimeFormatter : DateTimeFormatter, KoinComponent {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun formatDateTime(dateTime: LocalDateTime): String  =
        JavaDateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG, FormatStyle.SHORT)
            .format(toJavaDateTime(dateTime.date, dateTime.time))

    @RequiresApi(Build.VERSION_CODES.O)
    override fun formatDate(date: LocalDate): String =
        JavaDateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
            .format(toJavaDateTime(date, null))

    @RequiresApi(Build.VERSION_CODES.O)
    override fun formatTime(time: LocalTime): String  =
        JavaDateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
            .format(toJavaDateTime(null, time))

    @RequiresApi(Build.VERSION_CODES.O)
    override fun localizedMonthNames(style: NameStyle): List<String> {
        val pattern = if (style == NameStyle.ABBREVIATED) "LL" else "LLLL"
        val formatter = JavaDateTimeFormatter.ofPattern(pattern, Locale.getDefault())
        val monthNames = mutableListOf<String>()
        val javaDateTime = JavaLocalDateTime.of(1970, 1, 1, 0, 0)
        for (monthNumber in 1..12) {
            monthNames.add(formatter.format(javaDateTime.withMonth(monthNumber)))
        }
        return monthNames
    }

    override fun is24HourFormat(): Boolean {
        val context: Context by inject()
        return DateFormat.is24HourFormat(context)
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
}