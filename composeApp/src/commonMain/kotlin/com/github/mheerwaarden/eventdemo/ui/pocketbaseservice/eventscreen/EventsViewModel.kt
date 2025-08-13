package com.github.mheerwaarden.eventdemo.ui.pocketbaseservice.eventscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mheerwaarden.eventdemo.data.model.Event
import com.github.mheerwaarden.eventdemo.data.pocketbaseservice.PocketBaseService
import com.github.mheerwaarden.eventdemo.data.pocketbaseservice.SubscriptionAction
import com.github.mheerwaarden.eventdemo.data.pocketbaseservice.SubscriptionState
import com.github.mheerwaarden.eventdemo.util.nowMillis
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime

class EventsViewModel(private val pocketBase: PocketBaseService) : ViewModel() {
     init {
         println("EventsViewModel: init")
     }

    private val _uiState = MutableStateFlow(EventsUiState())
    val uiState = _uiState.asStateFlow()

    private var eventListenerJob: Job? = null

    // Call this after login is successful
    fun initializeAfterLogin() {
        loadEvents() // Initial load
        startListeningToEvents()
    }

    private fun loadEvents() {
        println("EventsViewModel: Loading events...")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            pocketBase.getEvents().fold(
                onSuccess = { events ->
                    _uiState.value = _uiState.value.copy(events = events, isLoading = false)
                    println("EventsViewModel: Events loaded successfully: ${events.size} events")
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Failed to load events: ${error.message}",
                        isLoading = false
                    )
                    println("EventsViewModel: Error loading events: ${error.message}")
                    error.printStackTrace()
                }
            )
        }
    }

    // Called from UI when user logs in or app starts if already logged in
    private fun startListeningToEvents() {
        eventListenerJob?.cancel() // Cancel any previous listener
        println("EventsViewModel: Starting to listen to events from PocketBase...")
        pocketBase.startListeningToEvents(listOf("events")) // Start the SSE connection and subscription process

        eventListenerJob = viewModelScope.launch {
            pocketBase.eventSubscriptionFlow
                .catch { e -> // This will catch errors from emit or from handleSubscriptionStateChang
                    println("EventsViewModel: Error in eventSubscriptionFlow: ${e.message}")
                    _uiState.value = _uiState.value.copy(errorMessage = "Realtime connection error: ${e.message}")
                    e.printStackTrace()
                    // Optionally, you might want to retry starting the listener here after a delay
                    // or signal a persistent error to the UI.
                }.collect { subscriptionState ->
                    println("EventsViewModel: Received SubscriptionState: $subscriptionState")
                    handleSubscriptionStateChange(subscriptionState)
                }
        }
        println("EventsViewModel: Event listener job started: $eventListenerJob")
    }

    private var eventCountLastSecond = 0
    private var lastTimestamp = nowMillis()
    private fun handleSubscriptionStateChange(subscriptionState: SubscriptionState<Event>) {
        val currentTime = nowMillis()
        if (currentTime - lastTimestamp >= 1000) {
            println("EventsViewModel: Events processed in last second: $eventCountLastSecond")
            eventCountLastSecond = 0
            lastTimestamp = currentTime
        }
        eventCountLastSecond++

        val currentEvents = _uiState.value.events.toMutableList() // Make it mutable for easier manipulation
        var updated = false

        when (subscriptionState.action) {
            SubscriptionAction.CREATE -> {
                // Avoid duplicates if initial load races with SSE
                if (currentEvents.none { it.id == subscriptionState.dataObject.id }) {
                    currentEvents.add(subscriptionState.dataObject)
                    updated = true
                    println("EventsViewModel: Event CREATED via SSE: ${subscriptionState.dataObject.id}")
                } else {
                    println("EventsViewModel: Event CREATED via SSE (already exists): ${subscriptionState.dataObject.id}")
                }
            }
            SubscriptionAction.UPDATE -> {
                val index = currentEvents.indexOfFirst { it.id == subscriptionState.dataObject.id }
                if (index != -1) {
                    currentEvents[index] = subscriptionState.dataObject
                    updated = true
                    println("EventsViewModel: Event UPDATED via SSE: ${subscriptionState.dataObject.id}")
                } else {
                    // It's possible an update comes for an event not yet in the list (e.g., if initial load missed it)
                    // You might choose to add it here, or log it as unexpected.
                    currentEvents.add(subscriptionState.dataObject) // Or handle as an error/log
                    updated = true
                    println("EventsViewModel: Event UPDATED via SSE (was not in list, added): ${subscriptionState.dataObject.id}")
                }
            }
            SubscriptionAction.DELETE -> {
                if (currentEvents.removeAll { it.id == subscriptionState.dataObject.id }) {
                    updated = true
                    println("EventsViewModel: Event DELETED via SSE: ${subscriptionState.dataObject.id}")
                } else {
                    println("EventsViewModel: Event DELETED via SSE (was not in list): ${subscriptionState.dataObject.id}")
                }
            }
            SubscriptionAction.NOOP -> {
                println("EventsViewModel: Event NOOP via SSE: ${subscriptionState.dataObject.id}")
            }
        }

        if (updated) {
            // Sort events if order matters, e.g., by date
            // currentEvents.sortByDescending { it.created } // Assuming 'created' field exists
            // Careful: sorting for every update is expensive
            _uiState.value = _uiState.value.copy(events = currentEvents.toList())
        }
    }

    fun stopListeningToEventsOnLogout() {
        println("EventsViewModel: Stopping event listener due to logout/viewmodel clear.")
        eventListenerJob?.cancel()
        eventListenerJob = null
        pocketBase.stopListeningToEvents() // Tell the client to tear down its SSE connection
    }

    // --- Other ViewModel methods (create, update, delete through PocketBaseService) ---
    fun createEvent(
        title: String,
        description: String,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ) {
        viewModelScope.launch {
            val newEvent = Event( // Ensure your Event model matches what PocketBase expects/returns
                title = title,
                description = description,
                startDateTime = startDate, // Make sure your LocalDateTime is correctly serialized
                endDateTime = endDate,
                owner = "" // PocketBase will likely set this based on authenticated user
            )
            _uiState.value = _uiState.value.copy(isLoading = true) // Indicate loading
            pocketBase.createEvent(newEvent).fold(
                onSuccess = { createdEvent ->
                    println("EventsViewModel: Event created successfully via API: ${createdEvent.id}")
                    // Realtime should update the list, but you can add it here optimistically
                    // or rely on SSE. If relying on SSE, ensure no duplicate additions.
                    // The handleSubscriptionStateChange should handle it.
                    _uiState.value = _uiState.value.copy(isLoading = false)

                },
                onFailure = { error ->
                    println("EventsViewModel: Error creating event: ${error.message}")
                    _uiState.value = _uiState.value.copy(errorMessage = "Failed to create event: ${error.message}", isLoading = false)
                    error.printStackTrace()
                }
            )
        }
    }

    fun updateEvent(event: Event) {
        viewModelScope.launch {
            pocketBase.updateEvent(event).fold(
                onSuccess = {
                    println("EventsViewModel: Event update request successful for ${event.id} (SSE will update list)")
                    /* Realtime subscription will update the list */
                },
                onFailure = { error ->
                    println("Error updating event: ${error.message}")
                    _uiState.value = _uiState.value.copy(errorMessage = error.message)
                }
            )
        }
    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            // Optimistically remove from UI or wait for SSE
            // For now, let SSE handle it as per your original code for delete
            pocketBase.deleteEvent(eventId).fold( // Assuming deleteEvent exists in PocketBaseService
                onSuccess = {
                    println("EventsViewModel: Event delete request successful for $eventId (SSE will update list)")
                    // SSE should handle UI update
                },
                onFailure = { error ->
                    println("EventsViewModel: Error deleting event $eventId: ${error.message}")
                    _uiState.value = _uiState.value.copy(errorMessage = "Failed to delete event: ${error.message}")
                    error.printStackTrace()
                }
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopListeningToEventsOnLogout()
        pocketBase.cleanup() // Important: cancel the client's CoroutineScope
        println("EventsViewModel: onCleared")
    }
}

data class EventsUiState(
    val isAuthenticated: Boolean = true, // Manage this based on login state
    val errorMessage: String? = null,
    val showLoginRequired: Boolean = false,
    val isLoading: Boolean = false,
    val events: List<Event> = emptyList()
)
