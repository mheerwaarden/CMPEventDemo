/*
 * Copyright (c) 2025. Marcel Van Heerwaarden
 * @Author Marcel van Heerwaarden
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.github.mheerwaarden.eventdemo.data.preferences

data class UserPreferences(
    val isReadOnly: Boolean = false,
    /** Prefer keyboard input for the date picker */
    val datePickerUsesKeyboard: Boolean = false,
    /** Prefer keyboard input for the time picker */
    val timePickerUsesKeyboard: Boolean = false,
    /** Expanded state of the Event calendar */
    val isCalendarExpanded: Boolean = true,
)