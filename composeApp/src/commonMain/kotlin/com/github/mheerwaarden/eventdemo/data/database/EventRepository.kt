/*
 * Copyright (c) 2025. Marcel Van Heerwaarden
 * @Author Marcel van Heerwaarden
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.github.mheerwaarden.eventdemo.data.database

import com.github.mheerwaarden.eventdemo.data.DataLoadingRepository
import com.github.mheerwaarden.eventdemo.data.model.Event
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface EventRepository : DataLoadingRepository {
    /** Get all events as a flow */
    fun getAllEvents(): Flow<Map<String, Event>>

    /** Get the selected event by id */
    fun getEvent(id: String): Event?
    /** Get the selected event by id as a flow */
    fun getEventStream(eventId: String): Flow<Event?>

    /** Get the current period and filter for selected events */
    fun getCurrentSelectionCriteria(): StateFlow<EventSelectionCriteria>
    /** Set period and filter for selected events from start up to end date */
    fun updateSelectionCriteria(
        newSelectionCriteria: EventSelectionCriteria
    )
    /** Get the events according to the current selection criteria */
    fun getSelectedEvents(): Flow<List<Event>>

    /** Add a new event */
    fun addEvent(event: Event): String
    /** Update an existing event */
    fun updateEvent(event: Event)
    /** Delete an existing event by id*/
    fun deleteEvent(id: String)

}