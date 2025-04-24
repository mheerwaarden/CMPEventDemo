/*
 * Copyright (c) 2025. Marcel Van Heerwaarden
 * @Author Marcel van Heerwaarden
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.github.mheerwaarden.eventdemo.ui.screen.event

import com.github.mheerwaarden.eventdemo.data.model.Event
import com.github.mheerwaarden.eventdemo.data.model.EventCategory
import com.github.mheerwaarden.eventdemo.data.model.EventType
import com.github.mheerwaarden.eventdemo.ui.util.HtmlColors
import com.github.mheerwaarden.eventdemo.util.now
import com.github.mheerwaarden.eventdemo.util.plus
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

data class EventUiState(
    val id: Long = 0,
    val description: String = "",
    val startDateTime: LocalDateTime = now(),
    val endDateTime: LocalDateTime = startDateTime.plus(1, DateTimeUnit.HOUR),
    val location: String? = null,
    val contact: String? = null,
    val notes: String? = null,
    val isOnline: Boolean = false,
    val eventType: EventType = EventType.ACTIVITY,
    val eventCategory: EventCategory = EventCategory.PRIVATE,
    val htmlColor: HtmlColors = HtmlColors.OLIVE_DRAB,
    val isEntryValid: Boolean = false,
) {
    fun toEvent(): Event {
        val timeZone = TimeZone.currentSystemDefault()
        return Event(
            id = id,
            description = description,
            startInstant = startDateTime.toInstant(timeZone),
            endInstant = endDateTime.toInstant(timeZone),
            location = location,
            contact = contact,
            notes = notes,
            isOnline = isOnline,
            eventType = eventType,
            eventCategory = eventCategory,
            htmlColor = htmlColor,
        )
    }

}

fun Event.toEventUiState(isEntryValid: Boolean = false): EventUiState {
    val timeZone = TimeZone.currentSystemDefault()
    return EventUiState(
        id = id,
        description = description,
        startDateTime = startInstant.toLocalDateTime(timeZone),
        endDateTime = endInstant.toLocalDateTime(timeZone),
        location = location,
        contact = contact,
        notes = notes,
        isOnline = isOnline,
        eventType = eventType,
        eventCategory = eventCategory,
        htmlColor = htmlColor,
        isEntryValid = isEntryValid,
    )
}