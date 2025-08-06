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

sealed interface UiState {
    data class EventState(
        val event: Event = Event(),
        // UI fields
        val isEntryValid: Boolean = false,
    ) : UiState {
        fun toEvent(): Event = event
    }

    data object Loading : UiState
    data object NotFound : UiState
}

fun Event.toEventUiState(isEntryValid: Boolean = false): UiState.EventState =
    UiState.EventState(
        event = this,
        isEntryValid = isEntryValid,
    )
