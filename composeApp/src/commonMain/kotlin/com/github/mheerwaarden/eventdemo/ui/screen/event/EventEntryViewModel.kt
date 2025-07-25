/*
 * Copyright (c) 2025. Marcel Van Heerwaarden
 * @Author Marcel van Heerwaarden
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.github.mheerwaarden.eventdemo.ui.screen.event

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.github.mheerwaarden.eventdemo.data.database.EventRepository
import com.github.mheerwaarden.eventdemo.util.now
import com.github.mheerwaarden.eventdemo.util.parseDate
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime

class EventEntryViewModel(
    savedStateHandle: SavedStateHandle,
    private val eventRepository: EventRepository,
) : ViewModel() {
    private val initialDateString: String =
        checkNotNull(savedStateHandle[EventEntryDestination.startDateArg])
    val initialDate = initialDateString.parseDate()

    var eventUiState: EventUiState by mutableStateOf(EventUiState())
        private set

    init {
        val now = now()
        val startDate = if (initialDate >= now.date) {
            initialDate
        } else {
            now.date
        }
        eventUiState = eventUiState.copy(
            event = eventUiState.event.copy(startDateTime = LocalDateTime(startDate, now.time)),
            isEntryValid = validateInput()
        )
    }

    /** Independent fields can be updated in the [newEventUiState] and a call to this function. This method also triggers validation */
    fun updateState(newEventUiState: EventUiState) {
        eventUiState =
            newEventUiState.copy(isEntryValid = validateInput(newDescription = newEventUiState.event.description))
    }

    fun updateEventDate(selectedStartDate: LocalDate?, selectedEndDate: LocalDate? = null) {
        if (selectedStartDate == null) return

        eventUiState = eventUiState.copy(
            event = eventUiState.event.copy(
                startDateTime = LocalDateTime(
                    date = selectedStartDate,
                    time = eventUiState.event.startDateTime.time
                ),
                endDateTime = LocalDateTime(
                    date = selectedEndDate ?: selectedStartDate,
                    time = eventUiState.event.endDateTime.time
                )
            ),
            isEntryValid = validateInput()
        )
    }

    fun updateEventStartTime(time: LocalTime) {
        eventUiState = eventUiState.copy(
            event = eventUiState.event.copy(
                startDateTime = LocalDateTime(
                    date = eventUiState.event.startDateTime.date,
                    time = time
                )
            ),
            isEntryValid = validateInput()
        )
    }

    fun updateEventEndTime(time: LocalTime) {
        eventUiState = eventUiState.copy(
            event = eventUiState.event.copy(
                endDateTime = LocalDateTime(
                    date = eventUiState.event.endDateTime.date,
                    time = time
                )
            ),
            isEntryValid = validateInput()
        )
    }

    fun addEvent() {
        eventRepository.addEvent(eventUiState.toEvent())
    }

    private fun validateInput(newDescription: String = eventUiState.event.description): Boolean {
        return newDescription.isNotBlank()
    }

}

