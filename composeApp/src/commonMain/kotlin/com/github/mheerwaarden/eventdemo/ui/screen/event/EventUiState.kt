/*
 * Copyright (c) 2025. Marcel Van Heerwaarden
 * @Author Marcel van Heerwaarden
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.github.mheerwaarden.eventdemo.ui.screen.event

import com.github.mheerwaarden.eventdemo.data.model.Event

data class EventUiState(
    val event: Event = Event(),
    // UI fields
    val isEntryValid: Boolean = false,
) {
    fun toEvent(): Event = event
}

fun Event.toEventUiState(isEntryValid: Boolean = false): EventUiState = EventUiState(
    event = this,
    isEntryValid = isEntryValid,
)
