package com.github.mheerwaarden.eventdemo.ui.pocketbaseservice.eventscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mheerwaarden.eventdemo.data.model.Event
import com.github.mheerwaarden.eventdemo.data.model.IEvent
import com.github.mheerwaarden.eventdemo.data.pocketbaseservice.PocketBaseResult
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
            when (val result = pocketBase.getEvents()) {
                is PocketBaseResult.Success -> {
                    _uiState.value = _uiState.value.copy(events = result.data, isLoading = false)
                    println("EventsViewModel: Events loaded successfully: ${result.data.size} events")
                }

                is PocketBaseResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Failed to load events: ${result.message}",
                        isLoading = false
                    )
                    println("EventsViewModel: Error loading events: ${result.message} (${result.code})")
                }
            }
        }
    }

    // Called from UI when user logs in or app starts if already logged in
    private fun startListeningToEvents() {
        eventListenerJob?.cancel() // Cancel any previous listener
        println("EventsViewModel: Starting to listen to events from PocketBase...")
        // Start the SSE connection and subscription process
        pocketBase.startListeningToEvents(listOf("events"))

        eventListenerJob = viewModelScope.launch {
            pocketBase.eventSubscriptionFlow
                .catch { e -> // This will catch errors from emit or from handleSubscriptionStateChang
                    println("EventsViewModel: Error in eventSubscriptionFlow: ${e.message}")
                    _uiState.value =
                        _uiState.value.copy(errorMessage = "Realtime connection error: ${e.message}")
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
    private fun handleSubscriptionStateChange(subscriptionState: SubscriptionState<IEvent>) {
        val currentTime = nowMillis()
        if (currentTime - lastTimestamp >= 1000) {
            println("EventsViewModel: Events processed in last second: $eventCountLastSecond")
            eventCountLastSecond = 0
            lastTimestamp = currentTime
        }
        eventCountLastSecond++

        val currentEvents =
            _uiState.value.events.toMutableList() // Make it mutable for easier manipulation
        var updated = false

        val currentId = subscriptionState.dataObject?.id
        when (subscriptionState.action) {
            SubscriptionAction.CREATE -> {
                // Avoid duplicates if initial load races with SSE
                if (currentEvents.none { it.id == currentId }) {
                    currentEvents.add(subscriptionState.dataObject!!.toEvent())
                    updated = true
                    println("EventsViewModel: Event CREATED via SSE: $currentId")
                } else {
                    println("EventsViewModel: Event CREATED via SSE (already exists): $currentId")
                }
            }

            SubscriptionAction.UPDATE -> {
                val index = currentEvents.indexOfFirst { it.id == currentId }
                if (index != -1) {
                    currentEvents[index] = subscriptionState.dataObject!!.toEvent()
                    updated = true
                    println("EventsViewModel: Event UPDATED via SSE: $currentId")
                } else {
                    // It's possible an update comes for an event not yet in the list (e.g., if initial load missed it)
                    // You might choose to add it here, or log it as unexpected.
                    if (subscriptionState.dataObject != null) {
                        currentEvents.add(subscriptionState.dataObject.toEvent()) // Or handle as an error/log
                    }
                    updated = true
                    println("EventsViewModel: Event UPDATED via SSE (was not in list, added): $currentId")
                }
            }

            SubscriptionAction.DELETE -> {
                if (currentEvents.removeAll { it.id == currentId }) {
                    updated = true
                    println("EventsViewModel: Event DELETED via SSE: $currentId")
                } else {
                    println("EventsViewModel: Event DELETED via SSE (was not in list): $currentId")
                }
            }

            SubscriptionAction.NOOP -> {
                println("EventsViewModel: Event NOOP via SSE: $currentId")
            }

            is SubscriptionAction.ERROR -> {
                val message = subscriptionState.action.message
                println("EventsViewModel: Event ERROR via SSE for $currentId: $message")
                _uiState.value = _uiState.value.copy(errorMessage = message)
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
        // Tell the client to tear down its SSE connection
        pocketBase.stopListeningToEvents(listOf("events"))
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
            when (val result = pocketBase.createEvent(newEvent)) {
                is PocketBaseResult.Success -> {
                    println("EventsViewModel: Event created successfully via API, id=: ${result.data.id}")
                    // Realtime should update the list, but you can add it here optimistically
                    // or rely on SSE. If relying on SSE, ensure no duplicate additions.
                    // The handleSubscriptionStateChange should handle it.
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
                is PocketBaseResult.Error -> {
                    println("EventsViewModel: Error creating event: ${result.message}")
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Failed to create event: ${result.message}",
                        isLoading = false
                    )
                }
           }
        }
    }

    fun updateEvent(event: IEvent) {
        viewModelScope.launch {
            when (val result = pocketBase.updateEvent(event)) {
                is PocketBaseResult.Success -> {
                    println("EventsViewModel: Event update request successful for ${event.id} (SSE will update list)")
                    /* Realtime subscription will update the list */
                }
                is PocketBaseResult.Error -> {
                    println("EventsViewModel: Error updating event ${event.id}: ${result.message}")
                    _uiState.value = _uiState.value.copy(errorMessage = "Failed to update event: ${result.message}")
                }
            }
        }
    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            // Optimistically remove from UI or wait for SSE
            // For now, let SSE handle it as per your original code for delete
            when (val result = pocketBase.deleteEvent(eventId)) {
                is PocketBaseResult.Success -> {
                    println("EventsViewModel: Event delete request successful for $eventId (SSE will update list)")
                    // SSE should handle UI update
                }
                is PocketBaseResult.Error -> {
                    println("EventsViewModel: Error deleting event $eventId: ${result.message}")
                    _uiState.value = _uiState.value.copy(errorMessage = "Failed to delete event: ${result.message}")
                }
            }
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
    val events: List<IEvent> = emptyList()
)
