package com.github.mheerwaarden.eventdemo.data.pocketbaseservice

import com.github.mheerwaarden.eventdemo.data.model.Event
import com.github.mheerwaarden.eventdemo.data.model.EventCategory
import com.github.mheerwaarden.eventdemo.data.model.EventType
import com.github.mheerwaarden.eventdemo.data.model.IEvent
import com.github.mheerwaarden.eventdemo.data.model.User
import com.github.mheerwaarden.eventdemo.ui.util.HtmlColors
import com.github.mheerwaarden.eventdemo.util.getExceptionMessage
import com.github.mheerwaarden.eventdemo.util.logError
import com.github.mheerwaarden.eventdemo.util.logMessage
import com.github.mheerwaarden.eventdemo.util.logWarning
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

class PocketBaseWasmService(baseUrl: String) : PocketBaseService {
    private val pb = PocketBaseWasm(baseUrl)
    private val supervisorJob = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Default + supervisorJob)

    // For emitting events from the SSE stream to multiple collectors
    private val _eventSubscriptionFlow = MutableSharedFlow<SubscriptionState<IEvent>>(replay = 0)
    override val eventSubscriptionFlow = _eventSubscriptionFlow.asSharedFlow()

    private var unsubscribeRealtimeGlobal: (() -> Unit)? = null

    override suspend fun login(email: String, password: String): PocketBaseResult<AuthResult> {
        return try {
            val response: AuthResponseWasm =
                pb.collection("users").authWithPassword(email, password).await()
            val userRecord = response.record
            val user = userRecord.toUser()
            println("PocketBaseWasmService: Login successful. UserToken: ${response.token}")

            PocketBaseResult.Success(AuthResult(response.token, user))
        } catch (e: Throwable) {
            PocketBaseResult.Error(getExceptionMessage("Login failed", e))
        }
    }

    override suspend fun register(
        email: String, password: String, passwordConfirm: String, name: String
    ): PocketBaseResult<AuthResult> {
        return try {
            val userData = createUserData(email, password, passwordConfirm, name)

            val newUser: JsAny = pb.collection("users").create(userData).await()

            // Auto-login after registration
            val loginResult = login(email, password)
            if (loginResult is PocketBaseResult.Success) {
                loginResult
            } else {
                PocketBaseResult.Error("PocketBaseWasmService: Registration succeeded but auto-login failed. Reason: ${(loginResult as PocketBaseResult.Error).message}")
            }
        } catch (e: Throwable) {
            PocketBaseResult.Error(getExceptionMessage("PocketBaseWasmService: Registration failed", e))
        }
    }

    override suspend fun logout(): PocketBaseResult<Unit> {
        return try {
            pb.authStore.clear()
            PocketBaseResult.Success(Unit)
        } catch (e: Throwable) {
            PocketBaseResult.Error(getExceptionMessage("PocketBaseWasmService: Logout failed", e))
        }
    }

    override suspend fun getEvents(): PocketBaseResult<List<IEvent>> {
        return try {
            val result: RecordListWasm = pb.collection("events").getList(1, 50).await()
            val itemsArray = result.items
            val events = (0 until itemsArray.length).map { index ->
                itemsArray[index].toEvent()
            }
            PocketBaseResult.Success(events)
        } catch (e: Throwable) {
            PocketBaseResult.Error(getExceptionMessage("PocketBaseWasmService: Failed to get events", e))
        }
    }

    override suspend fun createEvent(event: IEvent): PocketBaseResult<IEvent> {
        return try {
            val eventData = event.toWasmObjectForPocketBase()
            val createdRecord: RecordWasm = pb.collection("events").create(eventData).await()
            PocketBaseResult.Success(createdRecord.toEvent())
        } catch (e: Throwable) {
            PocketBaseResult.Error(getExceptionMessage("PocketBaseWasmService: Failed to create event", e))
        }
    }

    override suspend fun updateEvent(event: IEvent): PocketBaseResult<IEvent> {
        val eventId = event.id
        return try {
            val eventData = event.toWasmObjectForPocketBase()
            val updatedRecord: RecordWasm =
                pb.collection("events").update(eventId, eventData).await()
            PocketBaseResult.Success(updatedRecord.toEvent())
        } catch (e: Throwable) {
            PocketBaseResult.Error(getExceptionMessage("PocketBaseWasmService: Failed to update event", e))
        }
    }

    override suspend fun deleteEvent(eventId: String): PocketBaseResult<Boolean> {
        return try {
            val success: JsBoolean = pb.collection("events").delete(eventId).await()
            PocketBaseResult.Success(success.toBoolean())
        } catch (e: Throwable) {
            PocketBaseResult.Error(getExceptionMessage("PocketBaseWasmService: Failed to delete event", e))
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

        val targetCollection = collectionNames.firstOrNull() ?: "events"

        try {
            unsubscribeRealtimeGlobal = pb.realtime().subscribe(targetCollection) { eventData ->
                scope.launch {
                    try {
                        val actionType = eventData.action
                        val eventRecord = eventData.record

                        // Validate the record has required system fields
                        if (eventRecord.id.isBlank()) {
                            logWarning("PocketBaseWasmService: Received realtime event with invalid record (missing ID)")
                            return@launch
                        }

                        val commonEvent = eventRecord.toEvent()
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
                                    rawRecord = eventRecord
                                )
                            )
                        }
                    } catch (e: Throwable) {
                        _eventSubscriptionFlow.emit(
                            SubscriptionState(
                                action = SubscriptionAction.ERROR(
                                    getExceptionMessage(
                                        "PocketBaseWasmService: Error processing realtime event",
                                        e
                                    )
                                ),
                                dataObject = null,
                                rawRecord = eventData
                            )
                        )
                        logError("PocketBaseWasmService: Error processing realtime event", e)
                    }
                }
            }
        } catch (e: Throwable) {
            unsubscribeRealtimeGlobal = null
            logError(
                "PocketBaseWasmService: Failed to subscribe to realtime events for $targetCollection",
                e
            )
            scope.launch {
                _eventSubscriptionFlow.emit(
                    SubscriptionState(
                        action = SubscriptionAction.ERROR(
                            getExceptionMessage(
                                "PocketBaseWasmService: Failed to start listening to events",
                                e
                            )
                        ),
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
        } catch (e: Throwable) {
            logError("PocketBaseWasmService: Failed to unsubscribe", e)
        }
    }

    override fun cleanup() {
        stopListeningToEvents()
        supervisorJob.cancel()
        pb.realtime().disconnect()
        pb.authStore.clear()
        logMessage("PocketBaseWasmService: Cleaned up.")
    }

    override suspend fun isAuthenticated(): Boolean = pb.authStore.isValid

    override suspend fun getCurrentUser(): PocketBaseResult<User?> {
        return try {
            if (pb.authStore.isValid && pb.authStore.model != null) {
                val userRecord = pb.authStore.model!!
                PocketBaseResult.Success(userRecord.toUser())
            } else {
                PocketBaseResult.Success(null)
            }
        } catch (e: Throwable) {
            PocketBaseResult.Error(getExceptionMessage("PocketBaseWasmService: Failed to get current user", e))
        }
    }

    /* Helper/Mapping Functions */

    private fun RecordWasm.toEvent(): Event {
        // Cast to EventRecordWasm for type-safe property access
        val eventRecord = this.unsafeCast<EventRecordWasm>()

        val parsedStartDateTime: LocalDateTime = eventRecord.startMillis?.let {
            ofEpochMilli(it.toLong())
        } ?: run {
            logWarning("PocketBaseWasmService: startDateTime is missing or not a number, using current time.")
            now()
        }

        val parsedEndDateTime = eventRecord.endMillis?.let {
            ofEpochMilli(it.toLong())
        } ?: run {
            logWarning("PocketBaseWasmService: endDateTime is missing or not a number, using startDateTime + 1 hour.")
            parsedStartDateTime.plus(1, DateTimeUnit.HOUR)
        }

        val viewersList = eventRecord.viewers?.let { viewersArray: JsArray<JsString> ->
            (0 until viewersArray.length).map { index -> viewersArray[index].toString() }
        } ?: emptyList()

        return Event(
            id = this.id,
            collectionId = this.collectionId,
            collectionName = this.collectionName,
            created = this.created,
            updated = this.updated,
            title = eventRecord.title ?: "",
            description = eventRecord.description ?: "",
            startDateTime = parsedStartDateTime,
            endDateTime = parsedEndDateTime,
            location = eventRecord.location,
            contact = eventRecord.contact,
            notes = eventRecord.notes,
            eventType = eventRecord.eventType?.let {
                try {
                    EventType.valueOf(it)
                } catch (e: Exception) {
                    EventType.ACTIVITY
                }
            } ?: EventType.ACTIVITY,
            eventCategory = eventRecord.eventCategory?.let {
                try {
                    EventCategory.valueOf(it)
                } catch (e: Exception) {
                    EventCategory.PRIVATE
                }
            } ?: EventCategory.PRIVATE,
            isOnline = eventRecord.isOnline ?: false,
            htmlColor = eventRecord.htmlColor?.let {
                try {
                    HtmlColors.valueOf(it)
                } catch (e: Exception) {
                    HtmlColors.OLIVE_DRAB
                }
            } ?: HtmlColors.OLIVE_DRAB,
            amount = eventRecord.amount,
            price = eventRecord.price,
            owner = eventRecord.owner ?: "",
            viewers = viewersList,
            isPrivate = eventRecord.isPrivate ?: false
        )
    }

    private fun UserRecordWasm.toUser(): User {
        val userRecord = this
        val avatarFilename = userRecord.avatar

        return User(
            id = userRecord.id,
            email = userRecord.email ?: "",
            name = userRecord.name ?: "",
            avatar = if (!avatarFilename.isNullOrBlank()) {
                pb.getFileUrl(userRecord, avatarFilename)
            } else {
                null
            }
        )
    }

    private fun IEvent.toWasmObjectForPocketBase(): JsAny {
        val commonEvent = this.toEvent()
        return createEventObject(
            title = commonEvent.title,
            description = commonEvent.description,
            startMillis = commonEvent.startDateTime.toEpochMilli(),
            endMillis = commonEvent.endDateTime.toEpochMilli(),
            location = commonEvent.location,
            contact = commonEvent.contact,
            notes = commonEvent.notes,
            eventType = commonEvent.eventType.name,
            eventCategory = commonEvent.eventCategory.name,
            isOnline = commonEvent.isOnline,
            htmlColor = commonEvent.htmlColor.name,
            amount = commonEvent.amount,
            price = commonEvent.price,
            owner = commonEvent.owner,
            viewers = toJsArray(commonEvent.viewers),
            isPrivate = commonEvent.isPrivate
        )
    }

    private fun toJsArray(list: List<String>?): JsArray<JsString>? {
        if (list == null) return null
        return elementsToJsArray(*list.toTypedArray())
    }

}

// Helper function that takes vararg strings and constructs a JS array
// When a vararg parameter is passed to js(), it's available as a JS array
// with the same name as the vararg parameter.
@Suppress("UNUSED_PARAMETER") // elements is used by the js code
private fun elementsToJsArray(vararg elements: String): JsArray<JsString> = js("elements")



@Suppress("UNUSED_PARAMETER")
private fun createUserData(
    email: String,
    password: String,
    passwordConfirm: String,
    name: String
): JsAny =
    js("({ email: email, password: password, passwordConfirm: passwordConfirm, name: name })")

@Suppress("UNUSED_PARAMETER")
private fun createEventObject(
    title: String,
    description: String,
    startMillis: Long,
    endMillis: Long,
    location: String?,
    contact: String?,
    notes: String?,
    eventType: String,
    eventCategory: String,
    isOnline: Boolean,
    htmlColor: String,
    amount: Double?,
    price: Double?,
    owner: String?,
    viewers: JsArray<JsString>?,
    isPrivate: Boolean
): JsAny = js(
    """
    (() => { // Start of IIFE
        const obj = {
            title: title,
            description: description,
            startMillis: startMillis, // Use the parameter name
            endMillis: endMillis,     // Use the parameter name
            eventType: eventType,
            eventCategory: eventCategory,
            isOnline: isOnline,
            htmlColor: htmlColor,
            isPrivate: isPrivate
        };

        if (viewers !== null) obj.viewers = viewers; // Assign if not null
        if (location !== null && location !== undefined) obj.location = location;
        if (contact !== null && contact !== undefined) obj.contact = contact;
        if (notes !== null && notes !== undefined) obj.notes = notes;
        if (amount !== null && amount !== undefined) obj.amount = amount;
        if (price !== null && price !== undefined) obj.price = price;
        if (owner !== null && owner !== undefined) obj.owner = owner;
        
        return obj;
    })() // End and execute IIFE
    """
)

//@Suppress("UNUSED_PARAMETER")
//private fun toJsArrayJS(list: List<String>?): JsArray<JsString> = js(
//    """
//        if (list === null) {
//            return null;
//        }
//        const jsArray = [];
//        for (let i = 0; i < list.length; i++) {
//            jsArray.push(list[i]);
//        }
//        return jsArray;
//    """
//)
