package com.github.mheerwaarden.eventdemo

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class JsAppContextTest {

    @Test
    fun test_is24HourFormat_returnsTrueFor24HourLocale() {
        // Arrange: Mock the Intl.DateTimeFormat environment for 24-hour format
        mockIntlDateTimeFormat(hourCycle = "h24")

        // Act: Test the public is24HourFormat property with default true
        val result = JsAppContext().is24HourFormat

        // Assert: Verify the expected outcome
        assertTrue(result, "Expected true for a 24-hour locale")
    }

    @Test
    fun test_is24HourFormat_returnsFalseFor12HourLocale() {
        // Arrange: Mock the Intl.DateTimeFormat environment for 12-hour format
        mockIntlDateTimeFormat(hourCycle = "h12")

        // Act: Test the public is24HourFormat property
        val result = JsAppContext().is24HourFormat

        // Assert: Verify the expected outcome
        assertFalse(result, "Expected false for a 12-hour locale")
    }

    @Test
    fun test_is24HourFormat_returnsTrueForUnknownLocale() {
        // Arrange: Mock the Intl.DateTimeFormat environment for unknown format.
        // We define a void behavior, to check that is not considered as a 24 hours locale.
        mockIntlDateTimeFormat(hourCycle = null)

        // Act: Test the public is24HourFormat property
        val result = JsAppContext().is24HourFormat

        // Assert: Verify the expected outcome
        assertTrue(result, "Expected true for an unknown locale")
    }

    private fun mockIntlDateTimeFormat(hourCycle: String?) {
        js(
            """
            Intl.DateTimeFormat = function(locales, options) {
                this.resolvedOptions = function() {
                    if(hourCycle != null){
                        return { hourCycle: hourCycle };
                    }
                    return {};
                }
            }
        """
        )
    }

}