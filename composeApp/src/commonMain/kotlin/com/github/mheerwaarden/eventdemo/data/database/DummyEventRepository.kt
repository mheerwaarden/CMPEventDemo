/*
 * Copyright (c) 2025. Marcel Van Heerwaarden
 * @Author Marcel van Heerwaarden
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.github.mheerwaarden.eventdemo.data.database

import com.github.mheerwaarden.eventdemo.data.model.Event
import com.github.mheerwaarden.eventdemo.data.model.EventCategory
import com.github.mheerwaarden.eventdemo.data.model.EventType
import com.github.mheerwaarden.eventdemo.ui.screen.event.EventFilter
import com.github.mheerwaarden.eventdemo.util.daysInMonth
import com.github.mheerwaarden.eventdemo.util.endOfMonthInstant
import com.github.mheerwaarden.eventdemo.util.now
import com.github.mheerwaarden.eventdemo.util.plus
import com.github.mheerwaarden.eventdemo.util.startOfMonthInstant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant

class DummyEventRepository : EventRepository {
    private var key = 0L

    private val defaultEvents = initDefaultEvents()
    private val _events = MutableStateFlow<Map<Long, Event>>(defaultEvents)
    private val events: Flow<Map<Long, Event>> = _events

    private var storedStart: Instant = startOfMonthInstant()
    private var storedEnd: Instant = endOfMonthInstant()
    private var storedFilter: EventFilter = EventFilter.GENERAL

    private val _eventForPeriod = MutableStateFlow(getEvents(storedStart, storedEnd))
    private val eventForPeriod: Flow<List<Event>> = _eventForPeriod

    override fun getEventsForPeriod(): Flow<List<Event>> {
        return eventForPeriod
    }

    override fun getAllEvents(): Flow<Map<Long, Event>> {
        return events
    }

    override fun getEvents(start: Instant, end: Instant): List<Event> {
        return _events.value.values.filter { event ->
            event.startInstant >= start && event.endInstant < end
        }
    }

    override fun getEvent(id: Long): Event? = _events.value[id]

    override fun updateEventsForPeriod(
        start: Instant,
        end: Instant,
        filter: EventFilter,
    ) {
        storedStart = start
        storedEnd = end
        storedFilter = filter
        _eventForPeriod.value = if (filter == EventFilter.GENERAL) {
            getEvents(start, end)
        } else {
            getEvents(start, end).filter { event ->
                event.eventCategory.text == filter.text
            }
        }
    }

    private fun inPeriod(event: Event): Boolean {
        return event.startInstant >= storedStart && event.endInstant < storedEnd &&
                (storedFilter == EventFilter.GENERAL || event.eventCategory.text == storedFilter.text)
    }

    override fun addEvent(event: Event): Long {
        val newEvent = event.copy(id = ++key)
        _events.value = (_events.value + (key to newEvent))
            .toList().sortedBy { it.second.startInstant }.toMap()
        if (inPeriod(newEvent)) {
            _eventForPeriod.value += newEvent
        }
        return key
    }

    override fun updateEvent(event: Event) {
        _events.value = ((_events.value - event.id) + (event.id to event))
            .toList().sortedBy { it.second.startInstant }.toMap()
        if (inPeriod(event)) {
            _eventForPeriod.value -= _eventForPeriod.value.first { it.id == event.id }
            _eventForPeriod.value += event
        }
    }

    override fun deleteEvent(id: Long) {
        val event = getEvent(id)
        if (event != null) {
            _events.value -= id
            if (inPeriod(event)) {
                _eventForPeriod.value -= event
            }
        }
    }

    fun getDefaultEvents(count: Int): List<Event> = defaultEvents.values.take(count)

    private fun initDefaultEvents(): MutableMap<Long, Event> {
        val result = mutableMapOf<Long, Event>()
        val timeZone = TimeZone.currentSystemDefault()
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
        val previousDay = LocalDateTime(previousMonth.year, previousMonth.month, previousMonth.daysInMonth(), 13, 0).toInstant(timeZone)
        result[++key] = createDummyEvent(type, previousDay, corporateTypes.contains(type))

        // Four events on the first day of this month
        for (i in 0..< 3) {
            type = EventType.entries[i % EventType.entries.size]
            val at = LocalDateTime(now.year, now.month, 1, i, 0).toInstant(timeZone)
            result[++key] = createDummyEvent(type, at, corporateTypes.contains(type))
        }

        // One event on every other day of the month
        for (i in 0..< now.daysInMonth()) {
            type = EventType.entries[i % EventType.entries.size]
            val at = firstDayOfMonth.plus(i, DateTimeUnit.DAY).toInstant(timeZone)
            result[++key] = createDummyEvent(type, at, corporateTypes.contains(type))
        }

        // One event on the first day of the next month
        type = EventType.entries[1]
        val nextMonth = now.plus(1, DateTimeUnit.MONTH)
        val nextDay = LocalDateTime(nextMonth.year, nextMonth.month, 1, 13, 0).toInstant(timeZone)
        result[++key] = createDummyEvent(type, nextDay, corporateTypes.contains(type))

        return result
    }

    private fun createDummyEvent(
        type: EventType,
        at: Instant,
        isCorporate: Boolean,
    ) = Event(
        id = key,
        description = "Description of ${type.name.lowercase()}",
        startInstant = at,
        endInstant = at.plus(1, DateTimeUnit.HOUR),
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