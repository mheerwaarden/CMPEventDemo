/*
 * Copyright (c) 2025. Marcel Van Heerwaarden
 * @Author Marcel van Heerwaarden
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.github.mheerwaarden.eventdemo.ui.screen.event

import androidx.lifecycle.viewModelScope
import com.github.mheerwaarden.eventdemo.data.database.EventRepository
import com.github.mheerwaarden.eventdemo.data.database.EventSelectionCriteria
import com.github.mheerwaarden.eventdemo.data.model.Event
import com.github.mheerwaarden.eventdemo.ui.screen.LoadingViewModel
import com.github.mheerwaarden.eventdemo.util.nowMillis
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime

data class EventsInPeriodState(
    val eventsForPeriod: List<Event>,
    val currentSelectionCriteria: EventSelectionCriteria,
    val errorMessage: String? = null
)

sealed interface EventCalendarScreenUiState {
    val eventsInPeriodState: EventsInPeriodState

    // Waiting for repository's filter processing
    data class LoadingFilters(override val eventsInPeriodState: EventsInPeriodState) :
        EventCalendarScreenUiState

    // Success state with data
    data class Success(override val eventsInPeriodState: EventsInPeriodState) : EventCalendarScreenUiState

    // Error state with message
    data class ErrorLoading(override val eventsInPeriodState: EventsInPeriodState) :
        EventCalendarScreenUiState
}


@OptIn(ExperimentalCoroutinesApi::class)
class EventCalendarViewModel(
    private val eventRepository: EventRepository,
) : LoadingViewModel(eventRepository) {

    // Trigger for when setPeriodAndFilter is called. Value is Timestamp of the last period/filter change request.
    // 0L signifies initial data load after repo is ready, or no filter change yet.
    // Positive values signify a user-triggered filter/period change.
    private val periodOrFilterChangeRequest = MutableStateFlow(0L)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<EventCalendarScreenUiState> =
        periodOrFilterChangeRequest.flatMapLatest { requestTimestamp ->
            flow {
                // If requestTimestamp > 0L, it's an explicit filter/period change by the user
                // So, emit LoadingFilters state.
                if (requestTimestamp > 0L) {
                    emit(
                        EventCalendarScreenUiState.LoadingFilters(
                            EventsInPeriodState(
                                emptyList(),
                                eventRepository.getCurrentSelectionCriteria().value,
                            )
                        )
                    )
                }

                val dataStream: Flow<EventCalendarScreenUiState.Success> =
                    eventRepository.getSelectedEvents().map { eventsList ->
                        // When getEventsForPeriod() emits, the repository's currentEventFilter,
                        // currentPeriodStart, and currentPeriodEnd StateFlows will reflect
                        // the criteria that were used to produce this eventsList.
                        EventCalendarScreenUiState.Success(
                            EventsInPeriodState(
                                eventsForPeriod = eventsList,
                                eventRepository.getCurrentSelectionCriteria().value,
                            )
                        )
                    }
                emitAll(dataStream)
            }.onStart {  // This runs for initial (requestTimestamp == 0L) AND for subsequent changes
                if (requestTimestamp == 0L) { // Only for the very first time
                    emit(
                        EventCalendarScreenUiState.LoadingFilters(
                            EventsInPeriodState(
                                emptyList(),
                                eventRepository.getCurrentSelectionCriteria().value,
                            )
                        )
                    )
                }
            }.catch { e -> // Catch errors from the upstream flow (combine or eventRepository flows)
                emit(
                    EventCalendarScreenUiState.ErrorLoading(
                        EventsInPeriodState(
                            emptyList(),
                            eventRepository.getCurrentSelectionCriteria().value,
                            e.message
                        )
                    )
                )
            }
        }.distinctUntilChanged().stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000L),
                initialValue = EventCalendarScreenUiState.LoadingFilters(
                    EventsInPeriodState(
                        eventsForPeriod = emptyList(),
                        eventRepository.getCurrentSelectionCriteria().value,
                    )
                )
            )

    private fun setPeriodAndFilter(start: LocalDateTime, end: LocalDateTime, filter: EventFilter) {
        val currentSelectionCriteria = eventRepository.getCurrentSelectionCriteria().value
        val newSelectionCriteria = EventSelectionCriteria(start, end, filter)
        // Early return if nothing has changed. Necessary since EventCalendar always updates the
        // period when it renders a month.
        if (currentSelectionCriteria == newSelectionCriteria) {
            return
        }
        viewModelScope.launch {
            // Update repository first, so that if `periodOrFilterChangeRequest` triggers
            // the flow immediately, the repository's StateFlows for period/filter are already
            // reflecting the upcoming change for any initial emission from `PreparingCalendar` or `LoadingNewPeriodOrFilter`.
            // However, the `combine` won't get new *events* until `getEventsForPeriod()` updates.
            eventRepository.updateSelectionCriteria(newSelectionCriteria)
            periodOrFilterChangeRequest.value = nowMillis() // Then trigger the UI flow
        }
    }

    fun setFilter(filter: EventFilter) {
        val currentSelectionCriteria = eventRepository.getCurrentSelectionCriteria().value
        setPeriodAndFilter(
            currentSelectionCriteria.start, currentSelectionCriteria.end, filter
        )
    }

    fun setMonth(newMonthStart: LocalDate, newMonthEnd: LocalDate) {
        setPeriodAndFilter(
            start = LocalDateTime(newMonthStart, LocalTime(0, 0)),
            end = LocalDateTime(newMonthEnd, LocalTime(0, 0)),
            filter = eventRepository.getCurrentSelectionCriteria().value.filter
        )
    }

}
