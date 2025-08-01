package com.github.mheerwaarden.eventdemo.data.pocketbase

import com.github.mheerwaarden.eventdemo.data.model.AuthResponse
import com.github.mheerwaarden.eventdemo.data.model.Event
import com.github.mheerwaarden.eventdemo.data.model.User
import com.github.mheerwaarden.eventdemo.network.createPlatformHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.timeout
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.prepareGet
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class PocketBaseClient1(private val baseUrl: String = "https://6f7182262e63.ngrok-free.app") {
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
        println("Error during login: ${e.message}")
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
        println("Error during registration: ${e.message}")
        Result.failure(e)
    }

    suspend fun logout(): Result<Unit> = try {
        // Use the proper PocketBase logout endpoint
        authToken?.let { token ->
            try {
                client.post("$baseUrl/api/collections/users/auth-logout") {
                    headers {
                        append(HttpHeaders.Authorization, "Bearer $token")
                    }
                }
                println("Server-side logout successful")
            } catch (e: Exception) {
                // Log the error but continue with local cleanup
                println("Server logout failed (continuing with local cleanup): ${e.message}")
            }
        }

        // Clear local authentication state
        clearAuthState()

        Result.success(Unit)
    } catch (e: Exception) {
        // Even if server call fails, we should clear local state
        clearAuthState()
        Result.failure(e)
    }

    // Helper method to check if user is logged in
    fun isLoggedIn(): Boolean = authToken != null

    // Helper method to clear all authentication state
    private fun clearAuthState() {
        // Clear local authentication state
        authToken = null
        currentUser = null
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
        println("Error during event creation: ${e.message}")
        Result.failure(e)
    }

    suspend fun getEvents(page: Int = 1, perPage: Int = 30): Result<List<Event>> = try {
        val response = client.get("$baseUrl/api/collections/events/records") {
            bearerAuth(authToken!!)
            parameter("page", page)
            parameter("perPage", perPage)
            parameter("sort", "-created")
        }

        val data = response.body<PocketBaseResponse<EventData>>()
        Result.success(data.items.map { it.toEvent() })
    } catch (e: Exception) {
        println("Error during get event: ${e.message}")
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
        println("Error during event update: ${e.message}")
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
    fun subscribeToEventsClaude(): Flow<SubscriptionState<Event>> = flow {
        var reconnectAttempts = 0
        val maxReconnectAttempts = 3

        while (reconnectAttempts < maxReconnectAttempts) {
            try {
                val clientId = Uuid.random().toString()

                // Step 1: Establish SSE connection
                val response = client.get("$baseUrl/api/realtime") {
                    headers {
                        append(HttpHeaders.Accept, "text/event-stream")
                        append(HttpHeaders.CacheControl, "no-cache")
                        authToken?.let { token ->
                            append(HttpHeaders.Authorization, "Bearer $token")
                        }
                    }
                    timeout {
                        requestTimeoutMillis = null // Disable timeout for SSE
                        socketTimeoutMillis = null
                    }
                }

                val channel = response.bodyAsChannel()

                // Step 2: Send subscription request
                client.post("$baseUrl/api/realtime") {
                    contentType(ContentType.Application.Json)
                    authToken?.let { token ->
                        headers {
                            append(HttpHeaders.Authorization, "Bearer $token")
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

                // Reset reconnect attempts on successful connection
                reconnectAttempts = 0

                // Step 3: Process SSE stream
                while (!channel.isClosedForRead) {
                    val chunk = channel.readUTF8Line()
                    if (chunk != null) {
                        when {
                            chunk.startsWith("data: ") -> {
                                val data = chunk.substring(6)
                                if (data.trim() == "[DONE]") break

                                try {
                                    val jsonData = Json.parseToJsonElement(data).jsonObject
                                    processRealtimeMessage(jsonData)?.let { subscriptionState ->
                                        emit(subscriptionState)
                                    }
                                } catch (e: Exception) {
                                    println("Error parsing SSE data: ${e.message}")
                                }
                            }

                            chunk.startsWith("event: ") -> {
                                val eventType = chunk.substring(7)
                                println("SSE Event type: $eventType")
                            }
                        }
                    }
                }

                // If we reach here, the connection closed normally
                break

            } catch (e: Exception) {
                println("SSE connection error: ${e.message}")
                reconnectAttempts++

                when (e) {
                    is SocketTimeoutException -> {
                        if (reconnectAttempts < maxReconnectAttempts) {
                            println("Attempting reconnect $reconnectAttempts/$maxReconnectAttempts")
                            delay(calculateBackoffDelay(reconnectAttempts)) // Exponential backoff
                        } else {
                            throw Exception("Connection lost after $maxReconnectAttempts attempts. Please login again.")
                        }
                    }

                    is HttpRequestTimeoutException -> {
                        throw Exception("Authentication expired. Please login again.")
                    }

                    else -> throw e
                }
            }
        }
    }.catch { e ->
        // Handle any remaining errors
        println("Flow error: ${e.message}")
        throw e
    }

    // Updated processRealtimeMessage without callback
    private fun processRealtimeMessage(message: JsonObject): SubscriptionState<Event>? {
        return try {
            val action = message["action"]?.jsonPrimitive?.content
            when (action) {
                "create", "update", "delete" -> {
                    message["record"]?.let { recordJsonElement ->
                        val eventData = Json.decodeFromJsonElement<EventData>(recordJsonElement)
                        // Just return the subscription state
                        println("Subscription received: $action of ${eventData.title}")
                        SubscriptionState(
                            dataObject = eventData.toEvent(),
//                            action = SubscriptionAction.valueOf(action.uppercase())
                            action = when (action.lowercase()) {
                                "create" -> SubscriptionAction.CREATE
                                "update" -> SubscriptionAction.UPDATE
                                "delete" -> SubscriptionAction.DELETE
                                else -> {
                                    println("PocketBaseClient: Unknown action '${action}'")
                                    SubscriptionAction.NOOP
                                }
                            }
                        )
                    }
                }

                else -> null
            }
        } catch (e: Exception) {
            println("Error processing realtime message: ${e.message}")
            null
        }
    }

    private fun calculateBackoffDelay(attempt: Int, baseDelay: Long = 2000): Long {
        val exponentialDelay = baseDelay * (1 shl (attempt - 1)) // 2s, 4s, 8s, 16s...
        // Add randomness to prevent multiple clients from reconnecting at exactly the same time
        val jitter = (0..1000).random()
        return exponentialDelay + jitter
    }

    // Gemini: This is a simplified SSE client. PocketBase might have specific event formats.
    fun subscribeToEvents(): Flow<SubscriptionState<Event>> = flow {
        println("Subscribing to events...")
        client.prepareGet("$baseUrl/api/realtime") {
            // Add any necessary headers, e.g., authentication token if PocketBase requires it
            // For SSE, you might need to set an 'Accept: text/event-stream' header,
            // though Ktor might do this or the server might not strictly require it.
            header(HttpHeaders.Accept, "text/event-stream")
            // If you have an auth token from login:
            // authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
        }.execute { response ->
            if (!response.status.isSuccess()) {
                // Handle non-successful HTTP status, e.g., throw an exception
                throw SSEException("Failed to connect to SSE endpoint: ${response.status} - ${response.bodyAsText()}")
            }

            // Check content type to be sure (optional but good practice)
            val contentType = response.headers[HttpHeaders.ContentType]
            if (contentType?.startsWith("text/event-stream") != true) {
                throw SSEException("Expected text/event-stream but got $contentType")
            }

            val channel: ByteReadChannel = response.bodyAsChannel()
            while (!channel.isClosedForRead) {
                // SSE events are typically line-based
                // Format:
                // event: <event_name>
                // data: <json_payload>
                // id: <event_id>
                // :<comment>
                // <empty_line> (signals end of event)

                // This is a very basic parser. A more robust SSE parser is needed for production.
                var eventData = ""
                var eventType: String? = null

                while (true) {
                    val line = channel.readUTF8Line() ?: break // End of stream
                    if (line.isBlank()) { // Empty line signifies end of an event
                        if (eventData.isNotEmpty()) {
                            // Parse eventData (often JSON) based on eventType

                            try {
                                val jsonData = Json.parseToJsonElement(eventData).jsonObject
                                processRealtimeMessage(jsonData)?.let { subscriptionState ->
                                    emit(subscriptionState)
                                }
                            } catch (e: Exception) {
                                println("Error parsing SSE data: ${e.message}")
                            }
//                            emit(eventData) // Emit the raw data string for now
                        }
                        eventData = ""
                        eventType = null
                        // Move to next event block
                    } else if (line.startsWith("data:")) {
                        if (line.substring(6) == "[DONE]") break
                        eventData += line.substringAfter("data:").trim()
                    } else if (line.startsWith("event:")) {
                        eventType = line.substringAfter("event:").trim()
                    }
                    // TODO: Handle 'id:' and comments if necessary
                }
            }
        }
    }
}

class SSEException(message: String) : Exception(message)

// In your ViewModel or where you use it:
// viewModelScope.launch {
// pocketBaseClient.subscribeToRealtimeEvents()
// .catch { e -> /* handle errors */ }
// .collect { eventString ->
// // Process the eventString (likely JSON)
//            }
// }

