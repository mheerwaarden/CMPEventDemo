package com.github.mheerwaarden.eventdemo.data.pocketbaseservice

import com.github.mheerwaarden.eventdemo.data.model.Event
import com.github.mheerwaarden.eventdemo.data.model.IEvent
import com.github.mheerwaarden.eventdemo.data.model.User
import io.github.agrevster.pocketbaseKotlin.PocketbaseClient
import io.github.agrevster.pocketbaseKotlin.PocketbaseException
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
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class PBUserRecord(
    val email: String = "",
    val name: String = "",
    val verified: Boolean = false
) : BaseModel()


class PocketBaseKotlinService(baseUrl: String) : PocketBaseService {
    private val client = PocketbaseClient(baseUrl = {
        val url = baseUrl.removePrefix("http://").removePrefix("https://")
        val parts = url.split(":")

        protocol = if (baseUrl.startsWith("https")) URLProtocol.HTTPS else URLProtocol.HTTP
        host = parts[0]
        if (parts.size > 1) {
            port = parts[1].toIntOrNull() ?: 8090
        }
    })

    // Subscription jobs
    private var connectionJob: Job? = null
    private var listenerJob: Job? = null

    // For emitting events from the SSE stream to multiple collectors
    private val _eventSubscriptionFlow = MutableSharedFlow<SubscriptionState<IEvent>>(replay = 0)
    override val eventSubscriptionFlow = _eventSubscriptionFlow.asSharedFlow()

    private var currentUser: User? = null

    override suspend fun login(email: String, password: String): PocketBaseResult<AuthResult> {
        println("PocketBaseKotlinService: Login in with email: $email")
        return try {
            val authResult = client.records.authWithPassword<AuthRecord>("users", email, password)
            client.login { token = authResult.token }

            currentUser = User(
                id = authResult.record.id ?: "",
                email = authResult.record.email,
                verified = authResult.record.verified
            )
            println("PocketBaseKotlinService: Login successful. Token: ${authResult.token}")

            PocketBaseResult.Success(
                AuthResult(
                    token = authResult.token,
                    record = currentUser!!
                )
            )
        } catch (e: PocketbaseException) {
            println("PocketBaseKotlinService: Login failed: ${e.reason}")
            PocketBaseResult.Error("Login failed: ${e.reason}")
        } catch (e: Exception) {
            println("PocketBaseKotlinService: Login failed: ${e.message}")
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
        } catch (e: PocketbaseException) {
            println("PocketBaseKotlinService: Registration failed: ${e.reason}")
            PocketBaseResult.Error("Registration failed: ${e.reason}")
        } catch (e: Exception) {
            println("PocketBaseKotlinService: Registration failed: ${e.message}")
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
        } catch (e: PocketbaseException) {
            println("PocketBaseKotlinService: Failed to fetch events: ${e.reason}")
            PocketBaseResult.Error("Failed to fetch events: ${e.reason}")
        } catch (e: Exception) {
            println("PocketBaseKotlinService: Failed to fetch events: ${e.message}")
            PocketBaseResult.Error("Failed to fetch events: ${e.message}")
        }
    }

    override suspend fun createEvent(event: IEvent): PocketBaseResult<IEvent> {
        val user = currentUser ?: return PocketBaseResult.Error("No current user. Login first")

        return try {
            val eventWithOwner = event.toEvent().copy(owner = user.id)
            val createdEvent =
                client.records.create<PocketBaseEvent>(
                    "events",
                    Json.encodeToString(PocketBaseEvent(eventWithOwner))
                )
            PocketBaseResult.Success(createdEvent.eventData)
        } catch (e: PocketbaseException) {
            println("PocketBaseKotlinService: Failed to create events: ${e.reason}")
            PocketBaseResult.Error("Failed to create events: ${e.reason}")
        } catch (e: Exception) {
            println("PocketBaseKotlinService: Failed to create events: ${e.message}")
            PocketBaseResult.Error("Failed to create event: ${e.message}")
        }
    }

    override suspend fun updateEvent(event: IEvent): PocketBaseResult<IEvent> {
        if (event.id.isBlank()) {
            return PocketBaseResult.Error("Event ID is required for update")
        }

        return try {
            val updatedEvent =
                client.records.update<PocketBaseEvent>(
                    "events",
                    event.id,
                    Json.encodeToString(PocketBaseEvent(event))
                )

            PocketBaseResult.Success(updatedEvent.eventData)
        } catch (e: PocketbaseException) {
            println("PocketBaseKotlinService: Failed to update events: ${e.reason}")
            PocketBaseResult.Error("Failed to update events: ${e.reason}")
        } catch (e: Exception) {
            println("PocketBaseKotlinService: Failed to update events: ${e.message}")
            PocketBaseResult.Error("Failed to update event: ${e.message}")
        }
    }

    override suspend fun deleteEvent(eventId: String): PocketBaseResult<Boolean> {
        if (eventId.isBlank()) {
            return PocketBaseResult.Error("Event ID is required for delete")
        }

        return try {
            val result = client.records.delete("events", eventId)

            PocketBaseResult.Success(result)
        } catch (e: PocketbaseException) {
            println("PocketBaseKotlinService: Failed to delete events: ${e.reason}")
            PocketBaseResult.Error("Failed to delete events: ${e.reason}")
        } catch (e: Exception) {
            println("PocketBaseKotlinService: Failed to delete events: ${e.message}")
            PocketBaseResult.Error("Failed to delete event: ${e.message}")
        }
    }

    override fun subscribeToEvents(onUpdate: (IEvent) -> Unit): () -> Unit {
        startListeningToEvents()
        return { stopListeningToEvents(listOf("events")) }
    }

    override fun startListeningToEvents(collectionNames: List<String>) {
        if (connectionJob == null) {
            connectionJob = CoroutineScope(Dispatchers.Default).launch {
            try {
                println("PocketBaseKotlinService: Connect")
                client.realtime.connect()
            } catch (e: PocketbaseException) {
                println("PocketBaseKotlinService: Connection failed: ${e.reason}")
                CoroutineScope(Dispatchers.Default).launch {
                    _eventSubscriptionFlow.emit(
                        SubscriptionState(
                            action = SubscriptionAction.ERROR("Connection failed: ${e.reason}"),
                            dataObject = null
                        )
                    )
                }
            } catch (e: Exception) {
                println("PocketBaseKotlinService: Connection failed: ${e.message}")
                CoroutineScope(Dispatchers.Default).launch {
                    _eventSubscriptionFlow.emit(
                        SubscriptionState(
                            action = SubscriptionAction.ERROR("Connection failed: ${e.message}"),
                            dataObject = null
                        )
                    )
                }
            }
            }
        }

        println("PocketBaseKotlinService: Subscribe to ${collectionNames.joinToString(", ")}")
        CoroutineScope(Dispatchers.Default).launch {
            client.realtime.subscribe(subscriptionList = collectionNames.toTypedArray())
        }

        if (listenerJob == null) {
            listenerJob = CoroutineScope(Dispatchers.Default).launch {
                try {
                    println("PocketBaseKotlinService: Listen to ${collectionNames.joinToString(", ")}")
                    client.realtime.listen {
                        this@launch.launch {
                            try {
                                // `this@listen` refers to MessageData from `listen`
                                val action = this@listen.action
                                val record = this@listen.record

                                println("PocketBaseKotlinService: Realtime update: $action - $record")

                                if (record != null) {
                                    val collectionName =
                                        record.jsonObject["collectionName"]?.jsonPrimitive?.content
                                    if (collectionName != "events") {
                                        println("PocketBaseKotlinService: Skipping record of type $collectionName")
                                    } else {
                                        val subscriptionAction = when (action) {
                                            RealtimeService.RealtimeActionType.CREATE -> SubscriptionAction.CREATE
                                            RealtimeService.RealtimeActionType.UPDATE -> SubscriptionAction.UPDATE
                                            RealtimeService.RealtimeActionType.DELETE -> SubscriptionAction.DELETE
                                            RealtimeService.RealtimeActionType.CONNECT -> null
                                        }


                                        if (subscriptionAction != null) {
                                            val eventData: IEvent =
                                                Json.decodeFromJsonElement<Event>(record)
                                            val subscriptionState =
                                                SubscriptionState(
                                                    action = subscriptionAction,
                                                    dataObject = eventData,
                                                    rawRecord = record
                                                )
                                            _eventSubscriptionFlow.emit(subscriptionState)
                                            println("PocketBaseKotlinService: Emitted SubscriptionState: $subscriptionState")
                                        }
                                    }
                                }
                            } catch (e: PocketbaseException) {
                                println("PocketBaseKotlinService: Failed to parse realtime message: ${e.reason}")
                            } catch (e: Exception) {
                                println("PocketBaseKotlinService: Failed to parse realtime message: ${e.message}")
                            }
                        }
                    }
                } catch (e: PocketbaseException) {
                    println("PocketBaseKotlinService: Listening failed: ${e.reason}")
                    _eventSubscriptionFlow.emit(
                        SubscriptionState(
                            action = SubscriptionAction.ERROR("Listening failed: ${e.reason}"),
                            dataObject = null
                        )
                    )
                } catch (e: Exception) {
                    println("PocketBaseKotlinService: Listening failed: ${e.message}")
                    _eventSubscriptionFlow.emit(
                        SubscriptionState(
                            action = SubscriptionAction.ERROR("Listening failed: ${e.message}"),
                            dataObject = null
                        )
                    )
                }
            }
        }
    }

    override fun stopListeningToEvents(collectionNames: List<String>) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                client.realtime.unsubscribe(subscriptionList = collectionNames.toTypedArray())
            } catch (e: PocketbaseException) {
                println("PocketBaseKotlinService: Failed to unsubscribe: ${e.reason}")
            } catch (e: Exception) {
                println("PocketBaseKotlinService: Failed to unsubscribe: ${e.message}")
            }
        }
    }

    override fun cleanup() {
        stopListeningToEvents(listOf("events"))
        client.logout()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                client.realtime.disconnect()
            } catch (e: PocketbaseException) {
                println("PocketBaseKotlinService: Failed to disconnect: ${e.reason}")
            } catch (e: Exception) {
                println("PocketBaseKotlinService: Failed to disconnect: ${e.message}")
            }
        }
    }

    override suspend fun isAuthenticated(): Boolean = !client.authStore.token.isNullOrBlank()
    override suspend fun getCurrentUser(): PocketBaseResult<User?> =
        PocketBaseResult.Success(currentUser)
}
