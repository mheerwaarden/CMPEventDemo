package com.github.mheerwaarden.eventdemo

import com.github.mheerwaarden.eventdemo.localization.JsDateTimeFormatter
import com.github.mheerwaarden.eventdemo.localization.PlatformLocaleManager
import kotlinx.datetime.LocalDateTime
import org.koin.core.component.inject
import org.koin.test.KoinTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class JsDateTimeFormatterTest: KoinTest {

    init {
        KoinJsTestInitializer.startKoinOnce()
    }

    @Test
    fun test_formatDateTime_returns24HourFormattedString() {
        // Arrange: Set a locale for 24-hour format
        val platformLocaleManager: PlatformLocaleManager by inject()
        platformLocaleManager.setPlatformLocale("nl")

        val now = LocalDateTime(2025, 12, 31, 23, 59, 59)
        val result = JsDateTimeFormatter().formatDateTime(now)
        assertEquals("31 december 2025 om 23:59", result)
    }

    @Test
    fun test_formatDateTime_returns12HourFormattedString() {
        // Arrange: Set a locale for 12-hour format
        val platformLocaleManager: PlatformLocaleManager by inject()
        platformLocaleManager.setPlatformLocale("en-US")

        val now = LocalDateTime(2025, 12, 31, 23, 59, 59)
        val result = JsDateTimeFormatter().formatDateTime(now)
        assertEquals("December 31, 2025 at 11:59 PM", result)
    }

    @Test
    fun test_formatDateTime_unknownLocale_throwsException() {
        // Arrange: Set a locale to an unknown format
        val platformLocaleManager: PlatformLocaleManager by inject()
        platformLocaleManager.setPlatformLocale("test")

        val now = LocalDateTime(2025, 12, 31, 23, 59, 59)
        assertFailsWith<Throwable>("Formatting with unknown locale 'test' should throw exception") {
            JsDateTimeFormatter().formatDateTime(now)
        }
    }

    @Test
    fun test_is24HourFormat_returnsTrueFor24HourLocale() {
        // Arrange: Set a locale for 24-hour format
        val platformLocaleManager: PlatformLocaleManager by inject()
        platformLocaleManager.setPlatformLocale("nl")

        val result = JsDateTimeFormatter().is24HourFormat()
        assertTrue(result, "Expected true for a 24-hour locale")
    }

    @Test
    fun test_is24HourFormat_returnsFalseFor12HourLocale() {
        // Arrange: Set a locale for 12-hour format
        val platformLocaleManager: PlatformLocaleManager by inject()
        platformLocaleManager.setPlatformLocale("en-US")

        val result = JsDateTimeFormatter().is24HourFormat()
        assertFalse(result, "Expected false for a 12-hour locale")
    }

    @Test
    fun test_is24HourFormat_returnsFalseForNullLocale() {
        // Arrange: Set a null locale for default format.
        val platformLocaleManager: PlatformLocaleManager by inject()
        platformLocaleManager.setPlatformLocale(null)

        val result = JsDateTimeFormatter().is24HourFormat()
        assertFalse(result, "Expected false for an null locale")
    }

    @Test
    fun test_is24HourFormat_returnsTrueForUnknownLocale() {
        // Arrange: Set an undefined locale.
        val platformLocaleManager: PlatformLocaleManager by inject()
        platformLocaleManager.setPlatformLocale("test")

        assertFailsWith<Throwable>("Formatting with unknown locale 'test' should throw exception") {
            JsDateTimeFormatter().is24HourFormat()
        }
    }

}