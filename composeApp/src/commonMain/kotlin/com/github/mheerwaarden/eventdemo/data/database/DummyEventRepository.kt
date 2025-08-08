/*
 * Copyright (c) 2025. Marcel Van Heerwaarden
 * @Author Marcel van Heerwaarden
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.github.mheerwaarden.eventdemo.data.database

import com.github.mheerwaarden.eventdemo.data.DataLoadingState
import com.github.mheerwaarden.eventdemo.data.model.Event
import com.github.mheerwaarden.eventdemo.data.model.EventCategory
import com.github.mheerwaarden.eventdemo.data.model.EventType
import com.github.mheerwaarden.eventdemo.ui.screen.event.EventFilter
import com.github.mheerwaarden.eventdemo.util.daysInMonth
import com.github.mheerwaarden.eventdemo.util.endOfMonth
import com.github.mheerwaarden.eventdemo.util.now
import com.github.mheerwaarden.eventdemo.util.plus
import com.github.mheerwaarden.eventdemo.util.startOfMonth
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime

class DummyEventRepository(defaultDispatcher: CoroutineDispatcher = Dispatchers.Default) :
    EventRepository {
    private var key = 0L

    private val events = MutableStateFlow(mapOf("0" to Event()))

    private val defaultEvents by lazy { initDefaultEvents() }

    private val currentSelectionCriteria = MutableStateFlow(
        EventSelectionCriteria(
            LocalDateTime(startOfMonth(), LocalTime(0, 0)),
            LocalDateTime(endOfMonth(), LocalTime(0, 0)),
            EventFilter.GENERAL
        )
    )
    override fun getCurrentSelectionCriteria() = currentSelectionCriteria.asStateFlow()

    private var isLoaded = false
    override val loadingState: Flow<DataLoadingState> = flow {
        try {
            // Once the data is loaded, it should not be reloaded unless explicitly requested.
            if (isLoaded) {
                println("Event loadingState: Already loaded. Emit Success")
                emit(DataLoadingState.Success)
                return@flow
            }
            println("Event loadingState: Emit Loading...")
            emit(DataLoadingState.Loading)
            println("Event loadingState: Loading initial events...")
            events.value = initDefaultEvents()
            println("Event loadingState: Emit Success")
            emit(DataLoadingState.Success)
            println("Event loadingState: Done emitting")
            isLoaded = true
        } catch (e: Exception) {
            println("Event loadingState: Emit Error")
            emit(DataLoadingState.Error(e))
        }
    }.flowOn(defaultDispatcher)

    override fun prepareReload() {
        isLoaded = false
    }

    private val selectedEvents: Flow<List<Event>> = combine(
        events, // Flow 1: The base data
        currentSelectionCriteria // Flow 2: Combined criteria for period and filter
    ) { eventsMap, periodAndFilter ->
        // This transformation block is called whenever ANY of the combined flows emit a new value
        println("DummyEventRepository: Combining events for period. Start: ${periodAndFilter.start}, End: ${periodAndFilter.end}, Filter: ${periodAndFilter.filter}, EventCount: ${eventsMap.size}")
        eventsMap.values.filter { event ->
            val inPeriod = event.startDateTime >= periodAndFilter.start && event.endDateTime < periodAndFilter.end
            val matchesFilter = (periodAndFilter.filter == EventFilter.GENERAL || event.eventCategory.text == periodAndFilter.filter.text)
            inPeriod && matchesFilter
        }.sortedBy { it.startDateTime } // Optional: sort here if needed by consumers
    }.flowOn(defaultDispatcher)
    override fun getSelectedEvents(): Flow<List<Event>> = selectedEvents


    override fun getAllEvents(): Flow<Map<String, Event>> = events

    override fun getEvent(id: String): Event? {
        val event = events.value[id]
        if (event == null) {
            println("Get: Event $id not found")
        } else {
            println("Get event ${event.id}")
        }
        return event
    }

    override fun getEventStream(eventId: String): Flow<Event?> = events.map { eventsMap ->
        val event = eventsMap[eventId]
        if (event == null) {
            println("GetStream: Event $eventId not found in map")
        } else {
            println("GetStream: Event ${event.id}")
        }
        event
    }

    override fun updateSelectionCriteria(newSelectionCriteria: EventSelectionCriteria) {
        println("DummyEventRepository: Updating period/filter. " +
                "New Start: ${newSelectionCriteria.start}, " +
                "New End: ${newSelectionCriteria.end}, " +
                "New Filter: ${newSelectionCriteria.filter}")
        // Update the StateFlow, which will trigger the combine operator in getEventsForPeriodStream
        currentSelectionCriteria.value = newSelectionCriteria
    }

    override fun addEvent(event: Event): String {
        val newEvent = event.copy(id = (++key).toString())
        events.value = (events.value + (key.toString() to newEvent))
            .toList().sortedBy { it.second.startDateTime }.toMap()
        return newEvent.id
    }

    override fun updateEvent(event: Event) {
        if (events.value.containsKey(event.id)) {
            println("Updating event ${event.id}")
            events.value = (events.value + (event.id to event))
                .toList().sortedBy { it.second.startDateTime }.toMap()
        } else {
            println("Event ${event.id} not found")
        }
    }

    override fun deleteEvent(id: String) {
        events.value -= id
    }

    fun getDefaultEvents(count: Int): List<Event> = defaultEvents.values.take(count)

    private fun initDefaultEvents(): MutableMap<String, Event> {
        val result = mutableMapOf<String, Event>()
        val now = now()
        val firstDayOfMonth = LocalDateTime(now.year, now.month, 1, now.hour, now.minute)
        val corporateTypes = arrayOf(
            EventType.COMPANY_PARTY,
            EventType.CORPORATE_HACKATHON,
            EventType.CORPORATE_OFF_SITE,
            EventType.EXECUTIVE_MEETING,
            EventType.EXPERIMENTAL_MARKETING_ACTIVATION,
            EventType.INCENTIVE,
            EventType.PRODUCT_LAUNCH,
            EventType.TEAM_BUILDING_ACTIVITY,
            EventType.TRADE_SHOW,
            EventType.VIRTUAL_RECRUITING_EVENT,
        )

        // One event on the last day of the previous month
        var type = EventType.entries[0]
        val previousMonth = now.plus(-1, DateTimeUnit.MONTH)
        val previousDay =
            LocalDateTime(
                previousMonth.year,
                previousMonth.month,
                previousMonth.daysInMonth(),
                13,
                0
            )
        result[(++key).toString()] =
            createDummyEvent(type, previousDay, corporateTypes.contains(type))

        // Four events on the first day of this month
        for (i in 0..<3) {
            type = EventType.entries[i % EventType.entries.size]
            val at = LocalDateTime(now.year, now.month, 1, i, 0)
            result[(++key).toString()] = createDummyEvent(type, at, corporateTypes.contains(type))
        }

        // One event on every other day of the month
        for (i in 0..<now.daysInMonth()) {
            type = EventType.entries[i % EventType.entries.size]
            val at = firstDayOfMonth.plus(i, DateTimeUnit.DAY)
            result[(++key).toString()] = createDummyEvent(type, at, corporateTypes.contains(type))
        }

        // One event on the first day of the next month
        type = EventType.entries[1]
        val nextMonth = now.plus(1, DateTimeUnit.MONTH)
        val nextDay = LocalDateTime(nextMonth.year, nextMonth.month, 1, 13, 0)
        result[(++key).toString()] = createDummyEvent(type, nextDay, corporateTypes.contains(type))

        return result
    }

    private fun createDummyEvent(
        type: EventType,
        at: LocalDateTime,
        isCorporate: Boolean,
    ) = Event(
        id = key.toString(),
        description = "Description of ${type.name.lowercase()}",
        startDateTime = at,
        endDateTime = at.plus(1, DateTimeUnit.HOUR),
        location = if (isCorporate) "Work" else "Home",
        contact = "Phone: ${getRandomPhoneNr()}",
        notes = "Note on ${type.name}",
        eventType = type,
        eventCategory = if (isCorporate) EventCategory.CORPORATE
        else EventCategory.PRIVATE,
        isOnline = type.name.contains(other = "online", ignoreCase = true)
                || type.name.contains(other = "virtual", ignoreCase = true),
        htmlColor = type.htmlColor,
        amount = if (isCorporate) 100.0 else null,
        price = if (isCorporate) 250.0 else null
    )

    private fun getRandomPhoneNr(): String {
        val result = StringBuilder("0")
        for (i in 1..9) {
            result.append((0..9).random())
        }
        return result.toString()
    }
}