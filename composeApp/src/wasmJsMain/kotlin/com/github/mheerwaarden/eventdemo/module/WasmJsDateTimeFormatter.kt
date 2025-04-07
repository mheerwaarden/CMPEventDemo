package com.github.mheerwaarden.eventdemo.module

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

// Properties defined by js() must be top-level
private val browserLocale: String = js("navigator.language")

class WasmJsDateTimeFormatter : DateTimeFormatter {

    override fun format(dateTime: LocalDateTime, pattern: String): String {
        // Transform the LocalDateTime to millis
        val millis = dateTime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        return formatDateJs(millis, browserLocale)
    }

    override fun getCurrentLocale(): String {
        return browserLocale
    }
}

// Functions defined by js() must be top-level
private fun formatDateJs(millis: Long, locale: String): String = js(
    """
    (millis, locale) => {
        const options = { dateStyle: 'full' };
        return new Date(millis).toLocaleDateString(locale, options);
    }
    """
)