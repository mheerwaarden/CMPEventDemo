package com.github.mheerwaarden.eventdemo.module

import com.github.mheerwaarden.eventdemo.localization.NameStyle
import com.github.mheerwaarden.eventdemo.util.toEpochMilli
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterLongStyle
import platform.Foundation.NSDateFormatterNoStyle
import platform.Foundation.NSDateFormatterShortStyle
import platform.Foundation.NSLocale
import platform.Foundation.NSTimeZone
import platform.Foundation.currentLocale
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.systemTimeZone

class IOSDateTimeFormatter : DateTimeFormatter {
    override fun format(dateTime: LocalDateTime, pattern: String): String {
        val formatter = NSDateFormatter().apply {
            dateFormat = pattern
            locale = NSLocale.currentLocale
            timeZone = NSTimeZone.systemTimeZone()
        }

        val epochSeconds = dateTime.toEpochMilli() * 1000
        val nsDate = NSDate.dateWithTimeIntervalSince1970(epochSeconds.toDouble())

        return formatter.stringFromDate(nsDate)
    }

    override fun getCurrentLocale(): String {
        return NSLocale.currentLocale.toString()
    }

    fun isSystem24HourFormat(): Boolean {
        val formatter = NSDateFormatter()
        formatter.setLocale(NSLocale.currentLocale()) // Use current user's locale
        formatter.setDateStyle(NSDateFormatterNoStyle)
        formatter.setTimeStyle(NSDateFormatterShortStyle)

        // Create an NSDate object representing 11 PM (23:00) on some day
        // The exact date doesn't matter, only the time 23:00.
        // For simplicity, let's format a known "late" time.
        // NSDateComponents could also be used here to construct 23:00 precisely.
        // An alternative: format current time, then check string for AM/PM.
        // However, checking a fixed "23:00" time is more direct for the "23" heuristic.

        // To create a date representing 23:00, it's a bit tricky with just epoch time.
        // It's often easier to format a string and parse it, or use NSDateComponents.
        // For a heuristic similar to JS/Android:
        // Let's create a date representing January 1, 1970, 23:00:00 in the *current system time zone*.
        // This is hard to do directly without NSDateComponents easily from Kotlin/Native.

        // A more common iOS approach is to check if the locale's date format string contains 'a' (for AM/PM marker).
        val dateFormatString = NSDateFormatter.dateFormatFromTemplate("j", 0u, NSLocale.currentLocale())
        // "j" is the preferred hour format skeleton (h, H, K, k).
        // If it contains 'a' (AM/PM marker), it's 12-hour.
        // If it's 'H' or 'k', it's 24-hour. 'h' or 'K' is 12-hour.

        // This is considered a more robust way on iOS:
        return dateFormatString?.contains('a', ignoreCase = true) == false
    }
}

actual fun toLocalizedDateTimeString(dateTime: LocalDateTime): String = getFormattedStringForDate(
    LocalDateTime(
        dateTime.year,
        dateTime.monthNumber,
        dateTime.dayOfMonth,
        dateTime.hour,
        dateTime.minute,
        dateTime.second
    ),
    NSDateFormatterLongStyle,
    NSDateFormatterShortStyle
)

actual fun toLocalizedDateString(date: LocalDate): String = getFormattedStringForDate(
    LocalDateTime(date.year, date.monthNumber, date.dayOfMonth, 0, 0),
    NSDateFormatterLongStyle,
    NSDateFormatterNoStyle
)

actual fun toLocalizedTimeString(time: LocalTime): String = getFormattedStringForDate(
    LocalDateTime(1970, 1, 1, time.hour, time.minute),
    NSDateFormatterNoStyle,
    NSDateFormatterShortStyle
)

actual fun localizedMonthNames(style: NameStyle): List<String> {
    val pattern = if (style == NameStyle.ABBREVIATED) "MM" else "MMMM"
    val formatter = NSDateFormatter().apply {
        locale = NSLocale.currentLocale
        timeZone = NSTimeZone.systemTimeZone()
    }
    formatter.setLocalizedDateFormatFromTemplate(pattern)

    val monthNames = mutableListOf<String>()
    for (monthNumber in 1..12) {
        monthNames.add(
            formatter.stringFromDate(
                LocalDateTime(1970, monthNumber, 1, 0, 0).toNSDate()
            )
        )
    }
    return monthNames
}

private fun LocalDateTime.toNSDate(): NSDate {
    val epochSeconds = this.toEpochMilli() * 1000
    return NSDate.dateWithTimeIntervalSince1970(epochSeconds.toDouble())
}

private fun getFormattedStringForDate(
    localDateTime: LocalDateTime,
    dateStyle: ULong,
    timeStyle: ULong
): String {
    val formatter = NSDateFormatter().apply {
        this.dateStyle = dateStyle
        this.timeStyle = timeStyle
        this.locale = NSLocale.currentLocale
        this.timeZone = NSTimeZone.systemTimeZone()
    }
    return formatter.stringFromDate(localDateTime.toNSDate())

}
