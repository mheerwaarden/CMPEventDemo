package com.github.mheerwaarden.eventdemo.data.pocketbaseservice

import com.github.mheerwaarden.eventdemo.data.model.Event
import com.github.mheerwaarden.eventdemo.data.model.EventCategory
import com.github.mheerwaarden.eventdemo.data.model.EventType
import com.github.mheerwaarden.eventdemo.data.model.IEvent
import com.github.mheerwaarden.eventdemo.data.model.User
import com.github.mheerwaarden.eventdemo.ui.util.HtmlColors
import com.github.mheerwaarden.eventdemo.util.getExceptionMessage
import com.github.mheerwaarden.eventdemo.util.now
import com.github.mheerwaarden.eventdemo.util.ofEpochMilli
import com.github.mheerwaarden.eventdemo.util.plus
import com.github.mheerwaarden.eventdemo.util.toEpochMilli
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.await
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime

class PocketBaseJsService(baseUrl: String) : PocketBaseService {
    private val pb = PocketBaseJS(baseUrl)
    private val supervisorJob = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Default + supervisorJob)

    // For emitting events from the SSE stream to multiple collectors
    private val _eventSubscriptionFlow = MutableSharedFlow<SubscriptionState<IEvent>>(replay = 0)
    override val eventSubscriptionFlow = _eventSubscriptionFlow.asSharedFlow()

    private var unsubscribeRealtimeGlobal: (() -> Unit)? = null

    private val emptyOptions = jsObject { }

    override suspend fun login(email: String, password: String): PocketBaseResult<AuthResult> {
        return try {

            val response =
                pb.collection("users").authWithPassword(email, password, emptyOptions).await()
            val userRecord = response.record
            console.log("PocketBaseJSService: Login successful. UserToken: ${response.token}")

            val user = userRecord.toUser()
            PocketBaseResult.Success(AuthResult(response.token, user))
        } catch (e: dynamic) {
            console.error("PocketBaseJSService: Login failed", e)
            PocketBaseResult.Error(getExceptionMessage("Login failed", e))
        }
    }

    override suspend fun register(
        email: String, password: String, passwordConfirm: String, name: String
    ): PocketBaseResult<AuthResult> {
        return try {
            val userData = jsObject {
                this.email = email
                this.password = password
                this.passwordConfirm = passwordConfirm
                this.name = name
            }

            console.log("PocketBaseJSService: Creating user with email: $email")
            pb.collection("users").create(userData, emptyOptions).await()

            // Auto-login after registration
            println("PocketBaseJSService: Auto-login user with email: $email")
            val loginResult = login(email, password)
            if (loginResult is PocketBaseResult.Success) {
                loginResult
            } else {
                PocketBaseResult.Error("Registration succeeded but auto-login failed. Reason: ${(loginResult as PocketBaseResult.Error).message}")
            }
        } catch (e: dynamic) {
            console.error("PocketBaseJSService: Registration failed", e)
            PocketBaseResult.Error(getExceptionMessage("Registration failed", e))
        }
    }

    override suspend fun logout(): PocketBaseResult<Unit> {
        return try {
            pb.authStore.clear()
            PocketBaseResult.Success(Unit)
        } catch (e: dynamic) {
            console.error("PocketBaseJSService: Logout failed", e)
            PocketBaseResult.Error(getExceptionMessage("Logout failed", e))
        }
    }

    override suspend fun getEvents(): PocketBaseResult<List<IEvent>> {
        return try {
            console.log("PocketBaseJSService: Getting events")
            val result = pb.collection("events").getList(1, 50).await()
            val events = result.items.map { item -> item.unsafeCast<EventRecordJS>().toEvent() }
            PocketBaseResult.Success(events)
        } catch (e: dynamic) {
            console.error("PocketBaseJSService: Failed to get events", e)
            PocketBaseResult.Error(getExceptionMessage("Failed to get events", e))
        }
    }

    override suspend fun createEvent(event: IEvent): PocketBaseResult<IEvent> {
        val userResult = getCurrentUser()
        if (userResult is PocketBaseResult.Error) return userResult
        val user = (userResult as PocketBaseResult.Success).data
            ?: return PocketBaseResult.Error("No current user")

        return try {
            val eventWithOwner = event.toEvent().copy(owner = user.id)
            val eventDataJs = eventWithOwner.toEventRecordJS()
            console.log("PocketBaseJSService: Creating event")
            val createdRecord = pb.collection("events").create(eventDataJs).await()
            PocketBaseResult.Success(createdRecord.unsafeCast<EventRecordJS>().toEvent())
        } catch (e: dynamic) {
            console.error("PocketBaseJSService: Failed to create event", e)
            PocketBaseResult.Error(getExceptionMessage("Failed to create event", e))
        }
    }

    override suspend fun updateEvent(event: IEvent): PocketBaseResult<IEvent> {
        val eventId = event.id
        return try {
            console.log("PocketBaseJSService: Updating event $eventId")
            val eventDataJs = event.toEventRecordJS()
            val updatedRecord = pb.collection("events").update(eventId, eventDataJs).await()
            PocketBaseResult.Success(updatedRecord.unsafeCast<EventRecordJS>().toEvent())
        } catch (e: dynamic) {
            console.error("PocketBaseJSService: Failed to update event $eventId", e)
            PocketBaseResult.Error(getExceptionMessage("Failed to update event", e))
        }
    }

    override suspend fun deleteEvent(eventId: String): PocketBaseResult<Boolean> {
        return try {
            console.log("PocketBaseJSService: Updating event $eventId")
            val success = pb.collection("events").delete(eventId).await()
            PocketBaseResult.Success(success)
        } catch (e: dynamic) {
            console.error("PocketBaseJSService: Failed to delete event $eventId", e)
            PocketBaseResult.Error(getExceptionMessage("Failed to delete event", e))
        }
    }

    /**
     * Use the returned `UnsubscribeFunc` to remove only a single subscription
     */
    override fun subscribeToEvents(onUpdate: (IEvent) -> Unit): () -> Unit {
        startListeningToEvents(listOf("events"))
        return unsubscribeRealtimeGlobal ?: { }
    }

    override fun startListeningToEvents(collectionNames: List<String>) {
        stopListeningToEvents(listOf("events")) // Ensure any previous subscription is stopped

        // The PocketBase JS SDK's subscribe gives one callback for create, update, delete.
        // We will map these to our SubscriptionState.
        // For simplicity, this example subscribes to the first collection name, typically "events".
        // A more robust implementation would handle multiple collections if needed.
        val targetCollection = collectionNames.firstOrNull() ?: "events"

        try {
            console.log("PocketBaseJSService: Subscribing to realtime events for $targetCollection")
            unsubscribeRealtimeGlobal = pb.realtime.subscribe(
                topic = targetCollection,
                callback = ::handleRealTimeUpdate,
            )
        } catch (e: dynamic) {
            unsubscribeRealtimeGlobal = null
            console.error(
                "PocketBaseJsService: Failed to subscribe to realtime events for $targetCollection",
                e
            )
            scope.launch {
                _eventSubscriptionFlow.emit(
                    SubscriptionState(
                        action = SubscriptionAction.ERROR(
                            getExceptionMessage(
                                "Failed to start listening to events",
                                e
                            )
                        ),
                        dataObject = null
                    )
                )
            }
        }
    }

    private fun handleRealTimeUpdate(eventData: RealtimeDataJS) {
        scope.launch {
            try {
                val actionType = eventData.action as? String // e.g., "create", "update", "delete"
                val realTimeRecord = eventData.record

                console.log("PocketBaseJSService: Realtime update: $actionType - $realTimeRecord")

                // Validate the record has required system fields
                if (realTimeRecord.id.isBlank()) {
                    console.log("PocketBaseJSService: Received realtime event with invalid record (missing ID)")
                    return@launch
                }

                if (actionType != null) {
                    val subscriptionAction = when (actionType) {
                        "create" -> SubscriptionAction.CREATE
                        "update" -> SubscriptionAction.UPDATE
                        "delete" -> SubscriptionAction.DELETE
                        else -> null
                    }
                    if (subscriptionAction != null) {
                        val subscriptionState: SubscriptionState<IEvent> =
                            when (realTimeRecord.collectionName) {
                                "events" -> {
                                    val commonEvent: IEvent =
                                        realTimeRecord.unsafeCast<EventRecordJS>().toEvent()
                                    SubscriptionState(
                                        action = subscriptionAction,
                                        dataObject = commonEvent,
                                        rawRecord = realTimeRecord // Keep the raw JS record
                                    )
                                }
                                // Other collections can be handled here
                                else -> {
                                    console.log("PocketBaseJSService: Received realtime event for unknown collection: ${realTimeRecord.collectionName}")
                                    return@launch
                                }
                            }
                        _eventSubscriptionFlow.emit(subscriptionState)
                    }
                }
            } catch (e: dynamic) {
                _eventSubscriptionFlow.emit(
                    SubscriptionState(
                        action = SubscriptionAction.ERROR(
                            getExceptionMessage(
                                "Received malformed real-time event from JS SDK.",
                                e
                            )
                        ),
                        dataObject = null,
                        rawRecord = eventData // Keep the raw JS record
                    )
                )
                console.error("PocketBaseJsService: Malformed realtime event data", eventData, e)
            }
        }
    }

    override fun stopListeningToEvents(collectionNames: List<String>) {
        try {
            unsubscribeRealtimeGlobal?.invoke()
            unsubscribeRealtimeGlobal = null
            // Since wWe only subscribe to all events, we could also unsubscribe from all
            // pb.collection("events").unsubscribe()
        } catch (e: dynamic) {
            console.error("Failed to unsubscribe", e)
        }
    }

    override fun cleanup() {
        stopListeningToEvents(listOf("events"))
        supervisorJob.cancel()
        pb.realtime.disconnect()
        pb.authStore.clear()
        console.log("PocketBaseJsService: Cleaned up.")
    }

    override suspend fun isAuthenticated(): Boolean = pb.authStore.isValid
    override suspend fun getCurrentUser(): PocketBaseResult<User?> {
        return try {
            if (pb.authStore.isValid && pb.authStore.model != null) {
                PocketBaseResult.Success(pb.authStore.model!!.toUser())
            } else {
                PocketBaseResult.Success(null)
            }
        } catch (e: dynamic) {
            // This part should ideally not throw if only accessing properties,
            // but toUser() could if pb.authStore.model is malformed.
            PocketBaseResult.Error(getExceptionMessage("Failed to get current user", e))
        }
    }

    /* Helper/Mapping Functions */

    private fun EventRecordJS.toEvent(): Event {
        val eventRecord = this

        val parsedStartDateTime: LocalDateTime = eventRecord.startMillis?.let {
            ofEpochMilli(it.toLong())
        } ?: run {
            console.warn("PocketBaseJsService: startDateTime is missing or not a number, using current time.")
            now()
        }

        val parsedEndDateTime = eventRecord.endMillis?.let {
            ofEpochMilli(it.toLong())
        } ?: run {
            console.warn("PocketBaseJsService: endDateTime is missing or not a number, using startDateTime + 1 hour.")
            parsedStartDateTime.plus(1, DateTimeUnit.HOUR)
        }

        return Event(
            id = this.id,
            collectionId = this.collectionId,
            collectionName = this.collectionName,
            created = this.created,
            updated = this.updated,
            title = eventRecord.title ?: "",
            description = eventRecord.description ?: "",
            startDateTime = parsedStartDateTime, // Mapped LocalDateTime
            endDateTime = parsedEndDateTime,     // Mapped LocalDateTime
            location = eventRecord.location,
            contact = eventRecord.contact,
            notes = eventRecord.notes,
            eventType = EventType.valueOf(
                eventRecord.eventType ?: EventType.ACTIVITY.name
            ),
            eventCategory = EventCategory.valueOf(
                eventRecord.eventCategory ?: EventCategory.PRIVATE.name
            ),
            isOnline = eventRecord.isOnline ?: false,
            htmlColor = HtmlColors.valueOf(
                eventRecord.htmlColor ?: HtmlColors.OLIVE_DRAB.name
            ),
            amount = eventRecord.amount,
            price = eventRecord.price,
            owner = eventRecord.owner ?: "",
            viewers = eventRecord.viewers?.toList() ?: emptyList(),
            isPrivate = eventRecord.isPrivate ?: false
        )
    }

    private fun UserRecordJS.toUser(): User {
        val avatarFilename = this.avatar
        return User(
            id = this.id,
            email = this.email,
            name = this.name ?: "",
            avatar = if (avatarFilename.isNullOrBlank()) {
                null
            } else {
                pb.getFileUrl(this, avatarFilename)
            }
        )
    }

    private fun IEvent.toEventRecordJS(): dynamic {
        val commonEvent = this.toEvent() // Get the concrete Event data class
        return jsObject {
            this.title = commonEvent.title
            this.description = commonEvent.description
            // Send as epoch milliseconds (Number in JS)
            this.startMillis = commonEvent.startDateTime.toEpochMilli().toDouble()
            this.endMillis = commonEvent.endDateTime.toEpochMilli().toDouble()
            commonEvent.location?.let { this.location = it }
            commonEvent.contact?.let { this.contact = it }
            commonEvent.notes?.let { this.notes = it }
            this.eventType = commonEvent.eventType.name
            this.eventCategory = commonEvent.eventCategory.name
            this.isOnline = commonEvent.isOnline
            this.htmlColor = commonEvent.htmlColor.name
            commonEvent.amount?.let { this.amount = it }
            commonEvent.price?.let { this.price = it }
            this.owner = commonEvent.owner
            this.viewers = commonEvent.viewers.toTypedArray()
            this.isPrivate = commonEvent.isPrivate
            // id, created, updated, collectionId, collectionName are managed by PocketBase or URL path
        }
    }
}

// Helper to create dynamic JS objects more easily from Kotlin
fun jsObject(builder: dynamic.() -> Unit): dynamic {
    // Create an empty JS object
    val obj = js("new Object()") //js("{}")
    builder(obj)
    return obj
}