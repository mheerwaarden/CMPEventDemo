/*
 * Copyright (c) 2025. Marcel Van Heerwaarden
 * @Author Marcel van Heerwaarden
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.github.mheerwaarden.eventdemo.data.preferences

/* Default: Get the locale from the platform */
const val DEFAULT_LOCALE_FROM_PLATFORM = "System"
// Default: If no locale is set and the platform doesn't provide one, use English
const val DEFAULT_LOCALE = "en"

data class UserPreferences(
    val isReadOnly: Boolean = false,
    /** Prefer keyboard input for the date picker */
    val datePickerUsesKeyboard: Boolean = false,
    /** Prefer keyboard input for the time picker */
    val timePickerUsesKeyboard: Boolean = false,
    /** Expanded state of the Event calendar */
    val isCalendarExpanded: Boolean = true,
    /** Use Crane calendar instead of Event calendar */
    val useCraneCalendar: Boolean = false,
    /** Locale for language, default to English */
    val localeTag: String = DEFAULT_LOCALE_FROM_PLATFORM,
    /** Use PocketBase as backend */
    val usePocketBase: Boolean = false,
    /** PocketBase backend URL */
    val pocketBaseUrl: String = "",
    /** PocketBase client implementation */
    val pocketBaseClientType: PocketBaseClientType = PocketBaseClientType.KTOR_ONLY,
) {
    companion object {
        val DEFAULTS = UserPreferences()
    }
}
