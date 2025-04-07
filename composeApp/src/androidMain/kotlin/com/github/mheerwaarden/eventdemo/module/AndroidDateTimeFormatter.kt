package com.github.mheerwaarden.eventdemo.module

import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.datetime.LocalDateTime
import java.time.ZoneId
import java.util.Locale

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