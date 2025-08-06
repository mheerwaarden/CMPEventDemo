package com.github.mheerwaarden.eventdemo.ui.screen.event

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.github.mheerwaarden.eventdemo.data.database.EventRepository
import com.github.mheerwaarden.eventdemo.ui.screen.LoadingViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn


class EventViewModel(
    savedStateHandle: SavedStateHandle,
    private val eventRepository: EventRepository
) : LoadingViewModel(eventRepository) {
    private val eventId: String = checkNotNull(savedStateHandle[EventEditDestination.eventIdArg])

    val eventUiState: StateFlow<UiState> =
        eventRepository.getEventStream(eventId)
            .map { event ->
                println("EventViewModel: Received event from stream: ${event?.id}, online: ${event?.isOnline}")
                event?.toEventUiState(true)
                    ?: UiState.NotFound
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000L),
                initialValue = UiState.Loading
            )

    fun deleteEvent(id: String) {
        eventRepository.deleteEvent(id)
    }
}
