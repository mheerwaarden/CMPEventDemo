package com.github.mheerwaarden.eventdemo.module

import com.github.mheerwaarden.eventdemo.util.toEpochMilli
import kotlinx.datetime.LocalDateTime
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
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