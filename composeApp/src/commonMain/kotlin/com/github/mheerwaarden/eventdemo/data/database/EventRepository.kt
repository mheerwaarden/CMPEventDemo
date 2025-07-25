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
import com.github.mheerwaarden.eventdemo.ui.screen.event.EventFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime

interface EventRepository {
    fun getAllEvents(): Flow<Map<String, Event>>

    /** Return events from start up to end date */
    fun getEvents(start: LocalDateTime, end: LocalDateTime): List<Event>
    fun getEvent(id: String): Event?
    fun getEventsForPeriod(): Flow<List<Event>>
    fun addEvent(event: Event): String
    fun updateEvent(event: Event)
    /** Set period for selected events from start up to end date */
    fun updateEventsForPeriod(
        start: LocalDateTime,
        end: LocalDateTime,
        filter: EventFilter
    )
    fun deleteEvent(id: String)
}