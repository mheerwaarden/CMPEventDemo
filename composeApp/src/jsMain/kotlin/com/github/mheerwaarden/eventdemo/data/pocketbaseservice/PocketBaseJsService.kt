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

    override suspend fun login(email: String, password: String): PocketBaseResult<AuthResult> {
        return try {
            val response = pb.collection("users").authWithPassword(email, password)
            val userRecord = response.record
            val user = userRecord.toUser()
            PocketBaseResult.Success(AuthResult(response.token, user))
        } catch (e: dynamic) {
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

            pb.collection("users").create(userData)

            // Auto-login after registration
            val loginResult = login(email, password)
            if (loginResult is PocketBaseResult.Success) {
                loginResult
            } else {
                PocketBaseResult.Error("Registration succeeded but auto-login failed. Reason: ${(loginResult as PocketBaseResult.Error).message}")
            }
        } catch (e: dynamic) {
            PocketBaseResult.Error(getExceptionMessage("Registration failed", e))
        }
    }

    override suspend fun logout(): PocketBaseResult<Unit> {
        return try {
            pb.authStore.clear()
            PocketBaseResult.Success(Unit)
        } catch (e: dynamic) {
            PocketBaseResult.Error(getExceptionMessage("Logout failed", e))
        }
    }

    override suspend fun getEvents(): PocketBaseResult<List<IEvent>> {
        return try {
            val result = pb.collection("events").getList(1, 50)
            val events = result.items.map { it.toEvent() }
            PocketBaseResult.Success(events)
        } catch (e: dynamic) {
            PocketBaseResult.Error(getExceptionMessage("Failed to get events", e))
        }
    }

    override suspend fun createEvent(event: IEvent): PocketBaseResult<IEvent> {
        return try {
            val eventDataJs = event.toJsDynamicForPocketBase()
            val createdRecord = pb.collection("events").create(eventDataJs)
            PocketBaseResult.Success(createdRecord.toEvent())
        } catch (e: dynamic) {
            PocketBaseResult.Error(getExceptionMessage("Failed to create event", e))
        }
    }

    override suspend fun updateEvent(event: IEvent): PocketBaseResult<IEvent> {
        val eventId = event.id
        return try {
            val eventDataJs = event.toJsDynamicForPocketBase()
            val updatedRecord = pb.collection("events").update(eventId, eventDataJs)
            PocketBaseResult.Success(updatedRecord.toEvent())
        } catch (e: dynamic) {
            PocketBaseResult.Error(getExceptionMessage("Failed to update event", e))
        }
    }

    override suspend fun deleteEvent(eventId: String): PocketBaseResult<Boolean> {
        return try {
            val success = pb.collection("events").delete(eventId)
            PocketBaseResult.Success(success)
        } catch (e: dynamic) {
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
        stopListeningToEvents() // Ensure any previous subscription is stopped

        // The PocketBase JS SDK's subscribe gives one callback for create, update, delete.
        // We will map these to our SubscriptionState.
        // For simplicity, this example subscribes to the first collection name, typically "events".
        // A more robust implementation would handle multiple collections if needed.
        val targetCollection = collectionNames.firstOrNull() ?: "events"

        try {
            unsubscribeRealtimeGlobal = pb.realtime().subscribe(targetCollection) { data ->
                scope.launch {
                    val actionType = data.action as? String // e.g., "create", "update", "delete"

                    @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE") val record =
                        data.record as? RecordJS

                    if (actionType != null && record != null) {
                        val commonEvent = record.toEvent()
                        val subscriptionAction = when (actionType) {
                            "create" -> SubscriptionAction.CREATE
                            "update" -> SubscriptionAction.UPDATE
                            "delete" -> SubscriptionAction.DELETE
                            else -> null
                        }

                        if (subscriptionAction != null) {
                            _eventSubscriptionFlow.emit(
                                SubscriptionState(
                                    action = subscriptionAction,
                                    dataObject = commonEvent,
                                    rawRecord = record // Keep the raw JS record
                                )
                            )
                        }
                    } else {
                        _eventSubscriptionFlow.emit(
                            SubscriptionState(
                                action = SubscriptionAction.ERROR("Received malformed real-time event from JS SDK."),
                                dataObject = null,
                                rawRecord = data // Keep the raw JS record
                            )
                        )
                        console.error("PocketBaseJsService: Malformed realtime event data", data)
                    }
                }
            }
        } catch (e: dynamic) {
            unsubscribeRealtimeGlobal = null
            console.error(
                "PocketBaseJsService: Failed to subscribe to realtime events for $targetCollection",
                e
            )
            scope.launch {
                _eventSubscriptionFlow.emit(
                    SubscriptionState(
                        action = SubscriptionAction.ERROR(getExceptionMessage("Failed to start listening to events", e)),
                        dataObject = null
                    )
                )
            }
        }
    }

    override fun stopListeningToEvents() {
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
        stopListeningToEvents()
        supervisorJob.cancel()
        pb.realtime().disconnect()
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

    private fun RecordJS.toEvent(): Event {
        val dynamicRecord = this.asDynamic()

        val startMillis =
            dynamicRecord.startDateTime as? Number // Expect epoch millis (Number in JS)
        val endMillis = dynamicRecord.endDateTime as? Number   // Expect epoch millis (Number in JS)

        val parsedStartDateTime: LocalDateTime = if (startMillis != null) {
            ofEpochMilli(startMillis.toLong()) // Use your utility function
        } else {
            console.warn("PocketBaseJsService: startDateTime is missing or not a number, using current time.")
            now()
        }

        val parsedEndDateTime = if (endMillis != null) {
            ofEpochMilli(endMillis.toLong()) // Use your utility function
        } else {
            console.warn("PocketBaseJsService: endDateTime is missing or not a number, using startDateTime + 1 hour.")
            parsedStartDateTime.plus(1, DateTimeUnit.HOUR)
        }

        return Event(
            id = this.id,
            collectionId = this.collectionId,
            collectionName = this.collectionName,
            created = this.created,
            updated = this.updated,
            title = dynamicRecord.title as? String ?: "",
            description = dynamicRecord.description as? String ?: "",
            startDateTime = parsedStartDateTime, // Mapped LocalDateTime
            endDateTime = parsedEndDateTime,     // Mapped LocalDateTime
            location = dynamicRecord.location as? String,
            contact = dynamicRecord.contact as? String,
            notes = dynamicRecord.notes as? String,
            eventType = EventType.valueOf(
                dynamicRecord.eventType as? String ?: EventType.ACTIVITY.name
            ),
            eventCategory = EventCategory.valueOf(
                dynamicRecord.eventCategory as? String ?: EventCategory.PRIVATE.name
            ),
            isOnline = dynamicRecord.isOnline as? Boolean ?: false,
            htmlColor = HtmlColors.valueOf(
                dynamicRecord.htmlColor as? String ?: HtmlColors.OLIVE_DRAB.name
            ),
            amount = dynamicRecord.amount as? Double,
            price = dynamicRecord.price as? Double,
            owner = dynamicRecord.owner as? String ?: "",
            viewers = (dynamicRecord.viewers as? Array<String>)?.toList() ?: emptyList(),
            isPrivate = dynamicRecord.isPrivate as? Boolean ?: false
        )
    }

    private fun RecordJS.toUser(): User {
        val dynamicRecord = this.asDynamic()
        val avatarFilename = dynamicRecord.avatar as? String

        return User(
            id = this.id,
            email = dynamicRecord.email as? String ?: "",
            name = dynamicRecord.name as? String ?: "",
            avatar = if (!avatarFilename.isNullOrBlank()) {
                pb.getFileUrl(this, avatarFilename)
            } else {
                null
            }
        )
    }

    private fun IEvent.toJsDynamicForPocketBase(): dynamic {
        val commonEvent = this.toEvent() // Get the concrete Event data class
        return jsObject {
            this.title = commonEvent.title
            this.description = commonEvent.description
            // Send as epoch milliseconds (Number in JS)
            this.startDateTime = commonEvent.startDateTime.toEpochMilli()
            this.endDateTime = commonEvent.endDateTime.toEpochMilli()
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
    val obj = js("{}")
    builder(obj)
    return obj
}