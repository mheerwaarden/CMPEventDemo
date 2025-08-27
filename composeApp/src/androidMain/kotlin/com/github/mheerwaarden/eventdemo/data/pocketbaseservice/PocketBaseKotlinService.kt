package com.github.mheerwaarden.eventdemo.data.pocketbaseservice

import com.github.mheerwaarden.eventdemo.data.model.Event
import com.github.mheerwaarden.eventdemo.data.model.IEvent
import com.github.mheerwaarden.eventdemo.data.model.User
import io.github.agrevster.pocketbaseKotlin.PocketbaseClient
import io.github.agrevster.pocketbaseKotlin.dsl.login
import io.github.agrevster.pocketbaseKotlin.dsl.logout
import io.github.agrevster.pocketbaseKotlin.models.AuthRecord
import io.github.agrevster.pocketbaseKotlin.models.utils.BaseModel
import io.github.agrevster.pocketbaseKotlin.services.RealtimeService
import io.ktor.http.URLProtocol
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject

@Serializable
data class PBUserRecord(
    val email: String = "",
    val name: String = "",
    val verified: Boolean = false
) : BaseModel()

// PocketBase Kotlin client library requires the data objects to extend BaseModel. Therefore,
// PocketBaseEvent is a wrapper that extends BaseModel and delegates all to eventData.
@Serializable
class PocketBaseEvent(val eventData: IEvent) : BaseModel(eventData.id),
    IEvent by eventData {
    override val id: String
        get() = eventData.id

    override fun toEvent() = eventData.toEvent()
}

class PocketBaseKotlinService(baseUrl: String) : PocketBaseService {
    private val client = PocketbaseClient(baseUrl = {
        val url = baseUrl.removePrefix("http://").removePrefix("https://")
        val parts = url.split(":")

        protocol = if (baseUrl.startsWith("https")) URLProtocol.HTTPS else URLProtocol.HTTP
        host = parts[0]
        port = if (parts.size > 1) parts[1].toIntOrNull() ?: 8090 else 8090
    })

    private var subscriptionJob: Job? = null

    // For emitting events from the SSE stream to multiple collectors
    private val _eventSubscriptionFlow = MutableSharedFlow<SubscriptionState<IEvent>>(replay = 0)
    override val eventSubscriptionFlow = _eventSubscriptionFlow.asSharedFlow()

    private var currentUser: User? = null

    override suspend fun login(email: String, password: String): PocketBaseResult<AuthResult> {
        return try {
            val authResult = client.records.authWithPassword<AuthRecord>("users", email, password)
            client.login { token = authResult.token }

            currentUser = User(
                id = authResult.record.id ?: "",
                email = authResult.record.email,
//                name = authResult.record.name,
                verified = authResult.record.verified
            )
            println("PocketBaseKotlinService: Login successful. Token: ${authResult.token}")

            PocketBaseResult.Success(
                AuthResult(
                    token = authResult.token,
                    record = currentUser!!
                )
            )
        } catch (e: Exception) {
            PocketBaseResult.Error("Login failed: ${e.message}")
        }
    }

    override suspend fun register(
        email: String,
        password: String,
        passwordConfirm: String,
        name: String
    ): PocketBaseResult<AuthResult> {
        return try {
            val userData = PBUserRecord(email = email, name = name)
            val userJson = Json.encodeToString(userData).let {
                Json.parseToJsonElement(it).jsonObject.toMutableMap().apply {
                    put("password", JsonPrimitive(password))
                    put("passwordConfirm", JsonPrimitive(passwordConfirm))
                }
            }

            val newUser = client.records.create<PBUserRecord>(
                "users",
                Json.encodeToString(JsonObject(userJson))
            )
            println("PocketBaseKotlinService: Registration successful: ${newUser.email}")

            // Auto-login after registration
            login(email, password)
        } catch (e: Exception) {
            PocketBaseResult.Error("Registration failed: ${e.message}")
        }
    }

    override suspend fun logout(): PocketBaseResult<Unit> {
        client.logout()
        currentUser = null
        return PocketBaseResult.Success(Unit)
    }

    override suspend fun getEvents(): PocketBaseResult<List<IEvent>> {
        return try {
            val events = client.records.getList<PocketBaseEvent>("events", 1, 50)
            val eventList = events.items.map { pbEvent -> pbEvent.eventData }

            PocketBaseResult.Success(eventList)
        } catch (e: Exception) {
            PocketBaseResult.Error("Failed to fetch events: ${e.message}")
        }
    }

    override suspend fun createEvent(event: IEvent): PocketBaseResult<IEvent> = try {
        val createdEvent =
            client.records.create<PocketBaseEvent>(
                "events",
                Json.encodeToString(PocketBaseEvent(event))
            )

        PocketBaseResult.Success(createdEvent.eventData)
    } catch (e: Exception) {
        PocketBaseResult.Error("Failed to create event: ${e.message}")
    }

    override suspend fun updateEvent(event: IEvent): PocketBaseResult<IEvent> = try {
        if (event.id.isBlank()) {
            PocketBaseResult.Error("Event ID is required for update")
        }

        val updatedEvent =
            client.records.update<PocketBaseEvent>(
                "events",
                event.id,
                Json.encodeToString(PocketBaseEvent(event))
            )

        PocketBaseResult.Success(updatedEvent.eventData)
    } catch (e: Exception) {
        PocketBaseResult.Error("Failed to create event: ${e.message}")
    }

    override suspend fun deleteEvent(eventId: String): PocketBaseResult<Boolean> = try {
        if (eventId.isBlank()) {
            PocketBaseResult.Error("Event ID is required for delete")
        }

        val result = client.records.delete("events", eventId)

        PocketBaseResult.Success(result)
    } catch (e: Exception) {
        PocketBaseResult.Error("Failed to create event: ${e.message}")
    }


    override fun subscribeToEvents(onUpdate: (IEvent) -> Unit): () -> Unit {
        startListeningToEvents()
        return { stopListeningToEvents() }
    }

    override fun startListeningToEvents(collectionNames: List<String>) {
        subscriptionJob?.cancel() // Cancel any existing job
        subscriptionJob = CoroutineScope(Dispatchers.Default).launch {
            try {
                client.realtime.subscribe(subscriptionList = arrayOf("events"))

                client.realtime.listen {
                    this@launch.launch {
                        try {
                            // `this@listen` refers to MessageData from `listen`
                            val action = this@listen.action
                            val record = this@listen.record

                            if (record != null) {
                                val subscriptionAction = when (action) {
                                    RealtimeService.RealtimeActionType.CREATE -> SubscriptionAction.CREATE
                                    RealtimeService.RealtimeActionType.UPDATE -> SubscriptionAction.UPDATE
                                    RealtimeService.RealtimeActionType.DELETE -> SubscriptionAction.DELETE
                                    RealtimeService.RealtimeActionType.CONNECT -> null
                                }

                                if (subscriptionAction != null) {
                                    val eventData = Json.decodeFromJsonElement<Event>(record)
                                    val subscriptionState =
                                        SubscriptionState(
                                            action = subscriptionAction,
                                            dataObject = eventData as IEvent,
                                            rawRecord = record
                                        )
                                    _eventSubscriptionFlow.emit(subscriptionState)
                                    println("PocketBaseKotlinService: Emitted SubscriptionState: $subscriptionState")
                                }
                            }
                        } catch (e: Exception) {
                            println("Failed to parse realtime message: ${e.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                println("Subscription failed: ${e.message}")
                _eventSubscriptionFlow.emit(
                    SubscriptionState(
                        action = SubscriptionAction.ERROR("Subscription failed: ${e.message}"),
                        dataObject = null
                    )
                )
            }
        }
    }

    override fun stopListeningToEvents() {
        subscriptionJob?.cancel()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                client.realtime.unsubscribe(subscriptionList = arrayOf("events"))
            } catch (e: Exception) {
                println("Failed to unsubscribe: ${e.message}")
            }
        }
    }

    override fun cleanup() {
        stopListeningToEvents()
        client.logout()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                client.realtime.disconnect()
            } catch (e: Exception) {
                println("Failed to disconnect: ${e.message}")
            }
        }
    }

    override suspend fun isAuthenticated(): Boolean = !client.authStore.token.isNullOrBlank()
    override suspend fun getCurrentUser(): PocketBaseResult<User?> =
        PocketBaseResult.Success(currentUser)
}
