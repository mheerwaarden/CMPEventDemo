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
import androidx.lifecycle.viewModelScope
import com.github.mheerwaarden.eventdemo.data.database.EventRepository
import com.github.mheerwaarden.eventdemo.ui.screen.LoadingViewModel
import com.github.mheerwaarden.eventdemo.util.now
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime

class EventEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val eventRepository: EventRepository,
) : LoadingViewModel(eventRepository) {
    private val eventId: String = checkNotNull(savedStateHandle[EventEditDestination.eventIdArg])

    var eventUiState by mutableStateOf(UiState.EventState())
        private set

    // Just get the event. Do not create a flow, no dynamic updates during edit. When Room creates
    // a flow, it automatically does the query in the background. Here we need to launch a
    // coroutine ourselves.
    init {
        viewModelScope.launch {
            val event = eventRepository.getEvent(eventId)
            eventUiState =
                event?.toEventUiState(
                    validateInput(
                        newStartDateTime = event.startDateTime,
                        newDescription = event.description
                    )
                ) ?: throw NoSuchElementException("Event $eventId not found")
        }
    }

    /** Independent fields can be updated in the [newEventUiState] and a call to this function. This method also triggers validation */
    fun updateState(newEventUiState: UiState.EventState) {
        eventUiState =
            newEventUiState.copy(isEntryValid = validateInput(newDescription = newEventUiState.event.description))
    }

    fun updateEventDate(selectedStartDate: LocalDate?, selectedEndDate: LocalDate? = null) {
        if (selectedStartDate == null) return

        val newStartDate = LocalDateTime(
            date = selectedStartDate,
            time = eventUiState.event.startDateTime.time
        )
        eventUiState = eventUiState.copy(
            event = eventUiState.event.copy(
                startDateTime = newStartDate,
                endDateTime = LocalDateTime(
                    date = selectedEndDate ?: selectedStartDate,
                    time = eventUiState.event.endDateTime.time
                )
            ),
            isEntryValid = validateInput(newStartDate)
        )
    }

    fun updateEventStartTime(time: LocalTime) {
        val newStartDateTime = LocalDateTime(
            date = eventUiState.event.startDateTime.date,
            time = time
        )
        eventUiState = eventUiState.copy(
            event = eventUiState.event.copy(startDateTime = newStartDateTime),
            isEntryValid = validateInput(newStartDateTime = newStartDateTime)
        )
    }

    fun updateEventEndTime(time: LocalTime) {
        eventUiState = eventUiState.copy(
            event = eventUiState.event.copy(endDateTime = LocalDateTime(
                date = eventUiState.event.endDateTime.date,
                time = time
            )),
            isEntryValid = validateInput()
        )
    }

    fun updateEvent() {
        if (validateInput()) {
            eventRepository.updateEvent(eventUiState.toEvent())
        }
    }

    fun deleteEvent(id: String) {
        eventRepository.deleteEvent(id)
    }

    private fun validateInput(
        newStartDateTime: LocalDateTime = eventUiState.event.startDateTime,
        newDescription: String = eventUiState.event.description
    ): Boolean {
        return newStartDateTime > now() && newDescription.isNotBlank()
    }

}

