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
import com.github.mheerwaarden.eventdemo.data.model.Event
import com.github.mheerwaarden.eventdemo.ui.screen.LoadingViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class EventOverviewViewModel(
    private val eventRepository: EventRepository,
) : LoadingViewModel(eventRepository) {

    val eventUiState: StateFlow<List<Event>> =
            eventRepository.getAllEvents()
                .map { it.values.toList() }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                    initialValue = listOf()
                )

    fun deleteEvent(id: String) {
        eventRepository.deleteEvent(id)
    }

}

