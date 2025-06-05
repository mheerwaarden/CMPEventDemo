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
