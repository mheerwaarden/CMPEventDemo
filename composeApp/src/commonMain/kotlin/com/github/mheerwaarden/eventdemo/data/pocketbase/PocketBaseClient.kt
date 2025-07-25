package com.github.mheerwaarden.eventdemo.data.pocketbase

import com.github.mheerwaarden.eventdemo.data.model.AuthResponse
import com.github.mheerwaarden.eventdemo.data.model.Event
import com.github.mheerwaarden.eventdemo.data.model.User
import com.github.mheerwaarden.eventdemo.network.createPlatformHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class PocketBaseClient(private val baseUrl: String = "https://66b4f919f0e1.ngrok-free.app") {
    // Use configured platform-specific client
    private lateinit var client: HttpClient
    private var createPlatformHttpClientException: Exception?

    init {
        try {
            client = createPlatformHttpClient()
            createPlatformHttpClientException = null
        } catch (e: Exception) {
            createPlatformHttpClientException = e
        }
    }

    private var authToken: String? = null
    private var currentUser: User? = null

    // Authentication
    suspend fun login(email: String, password: String): Result<AuthResponse> = try {
        if (createPlatformHttpClientException != null) {
            throw createPlatformHttpClientException as Exception
        }
        val response = client.post("$baseUrl/api/collections/users/auth-with-password") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("identity" to email, "password" to password))
        }

        val authResponse = response.body<AuthResponse>()
        authToken = authResponse.token
        currentUser = authResponse.record

        Result.success(authResponse)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun register(email: String, password: String, name: String): Result<User> = try {
        if (createPlatformHttpClientException != null) {
            throw createPlatformHttpClientException as Exception
        }
        val response = client.post("$baseUrl/api/collections/users/records") {
            contentType(ContentType.Application.Json)
            setBody(
                mapOf(
                    "email" to email,
                    "password" to password,
                    "passwordConfirm" to password,
                    "name" to name
                )
            )
        }

        val user = response.body<User>()
        Result.success(user)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Events CRUD
    suspend fun createEvent(event: Event): Result<Event> = try {
        val eventData = event.toEventData(owner = currentUser?.id ?: "")
        val response = client.post("$baseUrl/api/collections/events/records") {
            contentType(ContentType.Application.Json)
            bearerAuth(authToken!!)
            setBody(eventData)
        }

        Result.success(response.body<EventData>().toEvent())
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getEvents(page: Int = 1, perPage: Int = 30): Result<List<Event>> = try {
        val response = client.get("$baseUrl/api/collections/events/records") {
            bearerAuth(authToken!!)
            parameter("page", page)
            parameter("perPage", perPage)
            parameter("sort", "-created")
        }

        val data = response.body<Map<String, Any>>()
        val items = (data["items"] as List<*>).map {
            Json.decodeFromString<EventData>(Json.encodeToString(it))
        }

        Result.success(items.map { it.toEvent() })
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateEvent(event: Event): Result<Event> = try {
        val response = client.patch("$baseUrl/api/collections/events/records/${event.id}") {
            contentType(ContentType.Application.Json)
            bearerAuth(authToken!!)
            setBody(event.toEventData())
        }

        Result.success(response.body<EventData>().toEvent())
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteEvent(eventId: String): Result<Boolean> = try {
        client.delete("$baseUrl/api/collections/events/records/$eventId") {
            bearerAuth(authToken!!)
        }
        Result.success(true)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Real-time subscriptions
    // Callers will receive the events through both the callback parameter and the Flow collection,
    // giving the flexibility in how to handle the realtime updates.
    @OptIn(ExperimentalUuidApi::class)
    fun subscribeToEvents(onEvent: (Event) -> Unit): Flow<Event> = flow {
        val clientId = Uuid.random().toString()

        try {
            // Step 1: Establish SSE connection
            val response = client.get("$baseUrl/api/realtime") {
                headers {
                    append(HttpHeaders.Accept, "text/event-stream")
                    append(HttpHeaders.CacheControl, "no-cache")
                    // Add authorization if needed
                    authToken?.let { token ->
                        append(HttpHeaders.Authorization, token)
                    }
                }
            }

            val channel = response.bodyAsChannel()

            // Step 2: Send subscription request via POST
            client.post("$baseUrl/api/realtime") {
                contentType(ContentType.Application.Json)
                authToken?.let { token ->
                    headers {
                        append(HttpHeaders.Authorization, token)
                    }
                }
                setBody(
                    Json.encodeToString(
                        mapOf(
                            "clientId" to clientId,
                            "subscriptions" to listOf("events")
                        )
                    )
                )
            }

            // Step 3: Process SSE stream
            while (!channel.isClosedForRead) {
                val chunk = channel.readUTF8Line()
                if (chunk != null) {
                    when {
                        chunk.startsWith("data: ") -> {
                            val data = chunk.substring(6) // Remove "data: " prefix
                            if (data.trim() == "[DONE]") break

                            try {
                                val jsonData = Json.parseToJsonElement(data).jsonObject
                                processRealtimeMessage(jsonData, onEvent)?.let { event ->
                                    emit(event)
                                }
                            } catch (e: Exception) {
                                println("Error parsing SSE data: ${e.message}")
                            }
                        }

                        chunk.startsWith("event: ") -> {
                            // Handle event type if needed
                            val eventType = chunk.substring(7)
                            println("SSE Event type: $eventType")
                        }

                        chunk.isEmpty() -> {
                            // Empty line indicates end of event
                            continue
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("SSE connection error: ${e.message}")
            throw e
        }
    }

    // Helper function to process PocketBase realtime messages
    private fun processRealtimeMessage(message: JsonObject, onEvent: (Event) -> Unit): Event? {
        return try {
            val action = message["action"]?.jsonPrimitive?.content

            when (action) {
                "create", "update", "delete" -> {
                    message["record"]?.let { recordJsonElement ->
                        val eventData = Json.decodeFromJsonElement<EventData>(recordJsonElement)
                        val event = eventData.toEvent()
                        onEvent(event)  // Call the callback
                        event           // Return for Flow emission
                    }
                }

                else -> {
                    println("Received realtime message with action: $action")
                    null
                }
            }
        } catch (e: Exception) {
            println("Error processing realtime message: ${e.message}")
            null
        }
    }
}