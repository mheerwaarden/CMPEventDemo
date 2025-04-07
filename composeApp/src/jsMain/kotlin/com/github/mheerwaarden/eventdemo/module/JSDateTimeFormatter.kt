package com.github.mheerwaarden.eventdemo.module

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJSDate

/** @return true if the javascript time format is 24 hours, or is not defined or is not available */
object JsDateTimeFormatter : DateTimeFormatter {
    private val locale: String = js("navigator.language") as String

    override fun format(dateTime: LocalDateTime, pattern: String): String {
        // Transform the LocalDateTime to a js Date object
        val jsDate = dateTime.toInstant(TimeZone.currentSystemDefault()).toJSDate()
        return jsDate.toLocaleDateString(locale, dateLocaleOptions {})
    }

    fun is24HourFormat() : Boolean = is24HourFormatJsImpl(locale)

    private fun is24HourFormatJsImpl(locale: String): Boolean {
        val resolvedOptions =
                js("new Intl.DateTimeFormat(locale, { hour: 'numeric' }).resolvedOptions()")
        return resolvedOptions?.hourCycle == null
                || resolvedOptions.hourCycle == "h23" || resolvedOptions.hourCycle == "h24"
    }

    override fun getCurrentLocale(): String {
        return locale.unsafeCast<String>()
    }
}