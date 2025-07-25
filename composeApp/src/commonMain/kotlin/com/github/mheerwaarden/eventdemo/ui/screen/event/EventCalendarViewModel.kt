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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mheerwaarden.eventdemo.EventDemoApplication.TIMEOUT_MILLIS
import com.github.mheerwaarden.eventdemo.data.database.EventRepository
import com.github.mheerwaarden.eventdemo.data.model.Event
import com.github.mheerwaarden.eventdemo.util.endOfMonth
import com.github.mheerwaarden.eventdemo.util.startOfMonth
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime

class EventCalendarViewModel(
    private val eventRepository: EventRepository,
) : ViewModel() {

    var calendarUiState by mutableStateOf(CalendarState())
        private set

    val eventUiState: StateFlow<List<Event>> =
        eventRepository.getEventsForPeriod()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = listOf()
            )

    /** Set period for selected events from start up to and including end date */
    fun setPeriod(start: LocalDate, end: LocalDate) {
        if (calendarUiState.start != start || calendarUiState.end != end) {
            calendarUiState = calendarUiState.copy(start = start, end = end)
            eventRepository.updateEventsForPeriod(
                start = LocalDateTime(start, LocalTime(0, 0)),
                end = LocalDateTime(end, LocalTime(0, 0)),
                filter = calendarUiState.eventFilter
            )
        }
    }

    fun setFilter(filter: EventFilter) {
        if (calendarUiState.eventFilter != filter) {
            calendarUiState = calendarUiState.copy(eventFilter = filter)
            eventRepository.updateEventsForPeriod(
                start = LocalDateTime(calendarUiState.start, LocalTime(0, 0)),
                end = LocalDateTime(calendarUiState.end, LocalTime(0, 0)),
                filter = filter
            )
        }
    }
}

data class CalendarState(
    val start: LocalDate = startOfMonth(),
    val end: LocalDate = endOfMonth(),
    val eventFilter: EventFilter = EventFilter.GENERAL,
)
