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
import androidx.lifecycle.viewModelScope
import com.github.mheerwaarden.eventdemo.data.database.EventRepository
import com.github.mheerwaarden.eventdemo.util.now
import com.github.mheerwaarden.eventdemo.util.toLocalDateTime
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime

class EventEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val eventRepository: EventRepository,
) : ViewModel() {
    private val eventId: Long = checkNotNull(savedStateHandle[EventEditDestination.eventIdArg])

    var eventUiState by mutableStateOf(EventUiState())
        private set

    // Just get the reminder. Do not create a flow, no dynamic updates during edit. When Room creates
    // a flow, it automatically does the query in the background. Here we need to launch a
    // coroutine ourselves.
    init {
        viewModelScope.launch {
            val event = eventRepository.getEvent(eventId)
            eventUiState =
                    event?.toEventUiState(validateInput(
                        newStartDateTime = event.startInstant.toLocalDateTime(),
                        newDescription = event.description
                    )) ?: throw NoSuchElementException("Event $eventId not found")
        }
    }

    /** Independent fields can be updated in the [eventUiState] and a call to this function. This method also triggers validation */
    fun updateState(eventUiState: EventUiState) {
        this.eventUiState = eventUiState.copy(isEntryValid = validateInput())
    }

    fun updateEventDate(selectedDate: LocalDateTime) {
        val newStartDateTime = LocalDateTime(
            date = selectedDate.date,
            time = eventUiState.startDateTime.time
        )
        eventUiState = eventUiState.copy(
            startDateTime = newStartDateTime,
            endDateTime = LocalDateTime(
                date = selectedDate.date,
                time = eventUiState.endDateTime.time
            ),
            isEntryValid = validateInput(newStartDateTime = newStartDateTime)
        )
    }

    fun updateEventStartTime(time: LocalTime) {
        val newStartDateTime = LocalDateTime(
            date = eventUiState.startDateTime.date,
            time = time
        )
        eventUiState = eventUiState.copy(
            startDateTime = newStartDateTime,
            isEntryValid = validateInput(newStartDateTime = newStartDateTime)
        )
    }

    fun updateEventEndTime(time: LocalTime) {
        eventUiState = eventUiState.copy(
            endDateTime = LocalDateTime(
                date = eventUiState.endDateTime.date,
                time = time
            ),
            isEntryValid = validateInput()
        )
    }

    fun updateEvent() {
        if (validateInput()) {
            eventRepository.updateEvent(eventUiState.toEvent())
        }
    }

    fun deleteEvent(id: Long) {
        eventRepository.deleteEvent(id)
    }

    private fun validateInput(
        newStartDateTime: LocalDateTime = eventUiState.startDateTime,
        newDescription: String = eventUiState.description
    ): Boolean {
        return newStartDateTime > now() && newDescription.isNotBlank()
    }

}
