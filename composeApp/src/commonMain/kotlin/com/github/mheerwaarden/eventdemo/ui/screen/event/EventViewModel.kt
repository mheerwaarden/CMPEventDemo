/*
 * Copyright (c) 2025. Marcel Van Heerwaarden
 * @Author Marcel van Heerwaarden
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.github.mheerwaarden.eventdemo.ui.screen.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mheerwaarden.eventdemo.EventDemoApplication.TIMEOUT_MILLIS
import com.github.mheerwaarden.eventdemo.data.database.EventRepository
import com.github.mheerwaarden.eventdemo.data.model.Event
import com.github.mheerwaarden.eventdemo.data.preferences.UserPreferences
import com.github.mheerwaarden.eventdemo.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class EventViewModel(
    private val eventRepository: EventRepository,
    userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    val eventUiState: StateFlow<List<Event>> =
            eventRepository.getAllEvents()
                .map { it.values.toList() }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                    initialValue = listOf()
                )

    val preferencesState: StateFlow<UserPreferences> =
            userPreferencesRepository.preferences.stateIn(
                scope = viewModelScope,
                // Flow is set to emits value for when app is on the foreground
                // The 5 seconds stop delay is added to ensure it flows continuously
                // for cases such as configuration change
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = UserPreferences()
            )

    fun deleteEvent(id: String) {
        eventRepository.deleteEvent(id)
    }

}

