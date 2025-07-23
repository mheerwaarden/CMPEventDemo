package com.github.mheerwaarden.eventdemo.ui.screen.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mheerwaarden.eventdemo.data.model.Event
import com.github.mheerwaarden.eventdemo.data.pocketbase.PocketBaseClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant

class EventsViewModel(private val pocketBase: PocketBaseClient) : ViewModel() {
    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events = _events.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    init {
        loadEvents()
        subscribeToRealtimeUpdates()
    }

    private fun loadEvents() {
        viewModelScope.launch {
            _isLoading.value = true
            pocketBase.getEvents().fold(
                onSuccess = { _events.value = it },
                onFailure = { _error.value = it.message }
            )
            _isLoading.value = false
        }
    }

    // Will receive the events through both the callback parameter and the Flow collection, giving
    // the flexibility in how to handle the realtime updates.
    private fun subscribeToRealtimeUpdates(onEvent: ((Event) -> Unit)? = null) {
        viewModelScope.launch {
            pocketBase.subscribeToEvents { updatedEvent ->
                val currentEvents = _events.value.toMutableList()
                val existingIndex = currentEvents.indexOfFirst { it.id == updatedEvent.id }

                if (existingIndex >= 0) {
                    currentEvents[existingIndex] = updatedEvent
                } else {
                    currentEvents.add(0, updatedEvent)
                }

                _events.value = currentEvents
            }.collect { event -> if (onEvent != null) onEvent(event) }
        }
    }

    fun createEvent(title: String, description: String, startDate: Instant, endDate: Instant) {
        viewModelScope.launch {
            val newEvent = Event(
                title = title,
                description = description,
                startInstant = startDate,
                endInstant = endDate,
                owner = 0 // Will be set by PocketBase client
            )

            pocketBase.createEvent(newEvent).fold(
                onSuccess = { /* Realtime will update the list */ },
                onFailure = { _error.value = it.message }
            )
        }
    }

    fun updateEvent(event: Event) {
        viewModelScope.launch {
            pocketBase.updateEvent(event).fold(
                onSuccess = { /* Realtime will update the list */ },
                onFailure = { _error.value = it.message }
            )
        }
    }

    fun deleteEvent(eventId: Long) {
        viewModelScope.launch {
            pocketBase.deleteEvent(eventId).fold(
                onSuccess = {
                    _events.value = _events.value.filter { it.id != eventId }
                },
                onFailure = { _error.value = it.message }
            )
        }
    }
}
