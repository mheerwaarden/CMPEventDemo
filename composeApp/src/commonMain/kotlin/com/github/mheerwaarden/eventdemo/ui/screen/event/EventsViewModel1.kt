package com.github.mheerwaarden.eventdemo.ui.screen.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mheerwaarden.eventdemo.data.model.Event
import com.github.mheerwaarden.eventdemo.data.pocketbase.PocketBaseClient
import com.github.mheerwaarden.eventdemo.data.pocketbase.PocketBaseClient1
import com.github.mheerwaarden.eventdemo.data.pocketbase.SubscriptionAction
import com.github.mheerwaarden.eventdemo.data.pocketbase.SubscriptionState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime

class EventsViewModel1(private val pocketBase: PocketBaseClient1) : ViewModel() {
    private val _uiState = MutableStateFlow(EventsUiState())
    val uiState = _uiState.asStateFlow()

    private var sseJob: Job? = null

    fun initialize() {
        loadEvents()
        startListeningToEvents()
    }

    private fun loadEvents() {
        println("Loading events...")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            pocketBase.getEvents().fold(
                onSuccess = { events ->
                    _uiState.value = _uiState.value.copy(events = events, isLoading = false)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = error.message,
                        isLoading = false
                    )
                }
            )
        }
    }

    fun createEvent(
        title: String,
        description: String,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ) {
        viewModelScope.launch {
            val newEvent = Event(
                title = title,
                description = description,
                startDateTime = startDate,
                endDateTime = endDate,
                owner = "" // Will be set by PocketBase client
            )

            pocketBase.createEvent(newEvent).fold(
                onSuccess = { /* Realtime will update the list */ },
                onFailure = { error ->
                    println("Error creating event: ${error.message}")
                    _uiState.value = _uiState.value.copy(errorMessage = error.message)
                }
            )
        }
    }

    fun updateEvent(event: Event) {
        viewModelScope.launch {
            pocketBase.updateEvent(event).fold(
                onSuccess = { /* Realtime subscription will update the list */ },
                onFailure = { error ->
                    println("Error updating event: ${error.message}")
                    _uiState.value = _uiState.value.copy(errorMessage = error.message)
                }
            )
        }
    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            pocketBase.deleteEvent(eventId).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        events = _uiState.value.events.filter { it.id != eventId }
                    )
                },
                onFailure = { error ->
                    println("Error deleting event: ${error.message}")
                    _uiState.value = _uiState.value.copy(errorMessage = error.message)
                }
            )
        }
    }


    private fun startListeningToEvents1() {
        // Cancel any existing subscription first
        sseJob?.cancel()

        sseJob = viewModelScope.launch {
            try {
                pocketBase.subscribeToEvents()
                    .collect { subscriptionState ->
                        val currentEvents = _uiState.value.events
                        // Update your events list based on the action
                        val updatedEvents =
                            when (subscriptionState.action) { // You'll need to add action to your Event class
                                SubscriptionAction.CREATE -> {
                                    currentEvents + subscriptionState.dataObject
                                }

                                SubscriptionAction.UPDATE -> {
                                    currentEvents.map { if (it.id == subscriptionState.dataObject.id) subscriptionState.dataObject else it }
                                }

                                SubscriptionAction.DELETE -> {
                                    currentEvents.filter { it.id != subscriptionState.dataObject.id }
                                }
                                SubscriptionAction.NOOP -> currentEvents
                            }
                        _uiState.value = _uiState.value.copy(events = updatedEvents)
                    }
            } catch (e: Exception) {
                if (e.message?.contains("login") == true) {
                    // Handle re-login
                    pocketBase.logout()
                }
            }
        }
    }

    private fun startListeningToEvents() {
        sseJob?.cancel() // Cancel any existing subscription
        sseJob = viewModelScope.launch {
            try {
                pocketBase.subscribeToEvents().collect { subscriptionState ->
                    handleSubscriptionStateChange(subscriptionState)
                }
            } catch (e: Exception) {
                if (e.message?.contains("login", ignoreCase = true) == true ||
                    e.message?.contains("authentication", ignoreCase = true) == true
                ) {
                    handleAuthenticationError(e)
                } else {
                    handleConnectionError(e)
                }
            }
        }
    }

    private fun handleSubscriptionStateChange(subscriptionState: SubscriptionState<Event>) {
        // Update your events list based on the action
        val currentEvents = _uiState.value.events
        val updatedEvents =
            when (subscriptionState.action) { // You'll need to add action to your Event class
                SubscriptionAction.CREATE -> {
                    currentEvents + subscriptionState.dataObject
                }

                SubscriptionAction.UPDATE -> {
                    currentEvents.map { if (it.id == subscriptionState.dataObject.id) subscriptionState.dataObject else it }
                }

                SubscriptionAction.DELETE -> {
                    currentEvents.filter { it.id != subscriptionState.dataObject.id }
                }
                SubscriptionAction.NOOP -> currentEvents
            }
        _uiState.value = _uiState.value.copy(events = updatedEvents)
    }

    private fun stopListeningToEvents() {
        sseJob?.cancel()
        sseJob = null
    }

    fun logout(withError: String? = null) {
        viewModelScope.launch {
            // 1. Stop any ongoing SSE connections
            stopListeningToEvents()
            // 2. Clear local authentication state
            pocketBase.logout()
            // 3. Update UI state to show authentication error and clear any cached data
            _uiState.value = _uiState.value.copy(
                isAuthenticated = false,
                errorMessage = withError,
                showLoginRequired = true,
                events = emptyList()
            )
            // 4. TODO: Navigate to login screen
        }
    }

    private fun handleAuthenticationError(error: Throwable) {
        println("Authentication error received: ${error.message}")
        logout("Your session has expired. Please log in again.")
    }

    private fun handleConnectionError(error: Throwable) {
        println("Connection error received: ${error.message}")
        _uiState.value = _uiState.value.copy(
            errorMessage = "Connection error: ${error.message}",
            isLoading = false
        )
    }

    override fun onCleared() {
        super.onCleared()
        // Make sure the SSE connection does not hold references when the view model is cleared
        stopListeningToEvents()
    }

}

data class EventsUiState1(
    val isAuthenticated: Boolean = true,
    val errorMessage: String? = null,
    val showLoginRequired: Boolean = false,
    val isLoading: Boolean = false,
    val events: List<Event> = emptyList()
)
