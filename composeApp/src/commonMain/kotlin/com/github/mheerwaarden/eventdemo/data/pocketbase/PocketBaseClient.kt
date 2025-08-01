package com.github.mheerwaarden.eventdemo.data.pocketbase

import com.github.mheerwaarden.eventdemo.data.model.Event
import com.github.mheerwaarden.eventdemo.data.model.User
import com.github.mheerwaarden.eventdemo.network.createHttpClientWithEngine
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeoutConfig
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.timeout
import io.ktor.client.request.accept
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.prepareGet
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

// --- Data Models for PocketBase Realtime ---

@Serializable
data class RealtimeMessage(
    val id: String? = null, // Event ID from SSE
    val event: String? = null, // Event name (collection name or PB_CONNECT)
    val data: String // Raw JSON data string
)

@Serializable
data class RealtimeDataWrapper<T>(
    val action: String, // "create", "update", "delete"
    val record: T
)

@Serializable
data class PBConnectEventData(
    @SerialName("clientId")
    val clientId: String
)

@Serializable
data class SubscriptionRequest(
    @SerialName("clientId")
    val clientId: String,
    val subscriptions: List<String> // List of collection names, e.g., listOf("events")
)

// Represents the state from the subscription
sealed class SubscriptionAction {
    data object CREATE : SubscriptionAction()
    data object UPDATE : SubscriptionAction()
    data object DELETE : SubscriptionAction()
    data object NOOP : SubscriptionAction()
}

data class SubscriptionState<T>(
    val action: SubscriptionAction,
    val dataObject: T,
    val rawRecord: JsonObject? = null // Optional: include raw for debugging
)


class PocketBaseClient(
    private val baseUrl: String,
    private val settingsRepository: SettingsRepository = InMemorySettingsRepository() // For auth token
) {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true // Useful for slightly malformed JSON sometimes
    }

    private val httpClient = createHttpClientWithEngine().config {
        expectSuccess = true // Will throw exceptions for non-2xx responses by default
        install(ContentNegotiation) {
            json(json)
        }
        install(Logging) {
            level = LogLevel.ALL // Max logging for debugging Ktor
            // logger = object : Logger {
            //     override fun log(message: String) {
            //         println("KtorClient: $message")
            //     }
            // }
        }
        install(Auth) {
            bearer {
                loadTokens {
                    // Load token from your settings/storage
                    val token = settingsRepository.getAuthToken()
                    if (token != null) {
                        BearerTokens(token, "") // Refresh token not used here
                    } else {
                        null
                    }
                }
                refreshTokens {
                    // If you have token refresh logic, implement it here.
                    // For PocketBase, usually, you re-login if the token expires.
                    // Returning null will effectively mean re-authentication is needed.
                    val oldToken = settingsRepository.getAuthToken()
                    if (oldToken != null) {
                        // Attempt to refresh (pseudo-code, PocketBase doesn't have a direct refresh endpoint like this)
                        // val refreshed = refreshTokenApiCall(oldToken)
                        // if (refreshed != null) BearerTokens(refreshed.accessToken, refreshed.refreshToken) else null
                        println("PocketBaseClient: Auth token needs refresh, but refresh mechanism not implemented. User should re-login.")
                        settingsRepository.clearAuthToken() // Clear expired token
                    }
                    null
                }
            }
        }
    }

    private var sseClientId: String? = null
    private val realtimeScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var sseJob: Job? = null

    // For emitting events from the SSE stream to multiple collectors
    private val _eventSubscriptionFlow = MutableSharedFlow<SubscriptionState<Event>>(replay = 0)
    val eventSubscriptionFlow = _eventSubscriptionFlow.asSharedFlow()

    fun startListeningToEvents(collectionNames: List<String> = listOf("events")) {
        sseJob?.cancel() // Cancel any existing job
        sseJob = realtimeScope.launch {
            println("PocketBaseClient: Attempting to connect to SSE...")
            var attempts = 0
            val maxAttempts = 5 // Max reconnection attempts for certain failures

            while (isActive && attempts < maxAttempts) {
                try {
                    httpClient.prepareGet("$baseUrl/api/realtime") {
                        method = HttpMethod.Get
                        accept(ContentType.Text.EventStream)
                        headers {
                            // Auth header will be added by the Auth plugin if token exists
                            append(HttpHeaders.CacheControl, "no-cache")
                        }
                        // Explicitly disable timeout for this SSE request
                        timeout { requestTimeoutMillis = HttpTimeoutConfig.INFINITE_TIMEOUT_MS }
                    }.execute { response ->
                        println("PocketBaseClient: SSE connection established, status: ${response.status}")
                        attempts = 0 // Reset attempts on successful connection

                        val channel: ByteReadChannel = response.bodyAsChannel()
                        while (isActive && !channel.isClosedForRead) {
                            val event = parseSseEvent(channel)
                            if (event != null) {
                                println("PocketBaseClient: Received SSE Event: ID=${event.id}, Event=${event.event}, Data=${event.data}")
                                if (event.event == "PB_CONNECT") {
                                    try {
                                        val connectData =
                                            json.decodeFromString<PBConnectEventData>(event.data)
                                        sseClientId = connectData.clientId
                                        println("PocketBaseClient: PB_CONNECT received, clientId: $sseClientId. Subscribing to collections: $collectionNames")
                                        subscribeToCollections(collectionNames)
                                    } catch (e: Exception) {
                                        println("PocketBaseClient: Error parsing PB_CONNECT data: ${e.message}")
                                    }
                                } else if (collectionNames.contains(event.event)) { // Check if it's one of the collections we care about
                                    try {
                                        val wrapper =
                                            json.decodeFromString<RealtimeDataWrapper<Event>>(event.data)
                                        val action = when (wrapper.action) {
                                            "create" -> SubscriptionAction.CREATE
                                            "update" -> SubscriptionAction.UPDATE
                                            "delete" -> SubscriptionAction.DELETE
                                            else -> {
                                                println("PocketBaseClient: Unknown action '${wrapper.action}'")
                                                null
                                            }
                                        }
                                        if (action != null) {
                                            val subscriptionState =
                                                SubscriptionState(action, wrapper.record)
                                            _eventSubscriptionFlow.emit(subscriptionState)
                                            println("PocketBaseClient: Emitted SubscriptionState: $subscriptionState")
                                        }
                                    } catch (e: Exception) {
                                        println("PocketBaseClient: Error parsing record data for event '${event.event}': ${e.message}")
                                        println("PocketBaseClient: Failing data: ${event.data}")
                                    }
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    println("PocketBaseClient: SSE Error: ${e.message}")
                    e.printStackTrace() // For detailed stack trace
                    sseClientId = null // Reset clientId on disconnection
                    attempts++
                    if (isActive && attempts < maxAttempts) {
                        val delayMillis = attempts * 2000L // Exponential backoff
                        println("PocketBaseClient: Retrying SSE connection in ${delayMillis / 1000}s (attempt $attempts/$maxAttempts)")
                        delay(delayMillis)
                    } else if (isActive) {
                        println("PocketBaseClient: Max SSE reconnection attempts reached. Stopping.")
                        // Optionally emit an error state to the UI
                    }
                } finally {
                    if (!isActive || attempts >= maxAttempts) {
                        println("PocketBaseClient: SSE connection loop ending (isActive: $isActive, attempts: $attempts).")
                    }
                }
                if (!isActive) break // Exit while loop if scope is cancelled
            }
        }
        println("PocketBaseClient: startListeningToEvents launched job: $sseJob")
    }

    private suspend fun subscribeToCollections(collectionNames: List<String>) {
        val currentClientId = sseClientId
        if (currentClientId == null) {
            println("PocketBaseClient: Cannot subscribe, clientId is null.")
            return
        }
        try {
            val requestBody = SubscriptionRequest(currentClientId, collectionNames)
            println("PocketBaseClient: Sending subscription POST with body: $requestBody")
            val response = httpClient.post("$baseUrl/api/realtime") {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
                // Auth header will be added by the Auth plugin if token exists
            }
            println("PocketBaseClient: Subscription POST response: ${response.status} - ${response.bodyAsText()}")
            if (!response.status.isSuccess()) {
                println("PocketBaseClient: Failed to subscribe to collections. Status: ${response.status}")
            } else {
                println("PocketBaseClient: Successfully subscribed to collections: $collectionNames")
            }
        } catch (e: Exception) {
            println("PocketBaseClient: Error sending subscription POST: ${e.message}")
            e.printStackTrace()
        }
    }

    private suspend fun parseSseEvent(channel: ByteReadChannel): RealtimeMessage? {
        var eventId: String? = null
        var eventName: String? = null
        val dataBuffer = StringBuilder()

        while (true) {
            val line = channel.readUTF8Line() ?: return null // End of stream or incomplete event
            if (line.isBlank()) { // End of an event
                return if (dataBuffer.isNotEmpty()) {
                    RealtimeMessage(eventId, eventName, dataBuffer.toString())
                } else {
                    // It could be a keep-alive comment or just an empty line separator
                    null // Or continue if you expect more events after empty lines
                }
            }

            when {
                line.startsWith("id:") -> eventId = line.substring("id:".length).trim()
                line.startsWith("event:") -> eventName = line.substring("event:".length).trim()
                line.startsWith("data:") -> {
                    if (dataBuffer.isNotEmpty()) dataBuffer.append("\n") // For multi-line data
                    dataBuffer.append(line.substring("data:".length).trim())
                }

                line.startsWith(":") -> { // Comment, ignore
                    println("PocketBaseClient: SSE Comment: $line")
                }

                else -> {
                    println("PocketBaseClient: SSE Unknown line: $line")
                }
            }
        }
    }

    fun stopListeningToEvents() {
        println("PocketBaseClient: stopListeningToEvents called. Cancelling job: $sseJob")
        sseJob?.cancel()
        sseJob = null
        sseClientId = null // Reset client ID
        // Note: _eventSubscriptionFlow is a SharedFlow and doesn't need explicit closing here unless
        // the PocketBaseClient itself is being destroyed and no longer needed.
        // If PocketBaseClient is a singleton, this is fine.
    }

    fun cleanup() { // Call when PocketBaseClient is no longer needed (e.g. ViewModel onCleared)
        println("PocketBaseClient: Cleaning up...")
        stopListeningToEvents()
//        if (realtimeScope.isActive) { // Check if it's active before cancelling
//            realtimeScope.cancel("PocketBaseClient cleanup initiated")
//        }
//        if (httpClient.engine.isActive) { // Check before closing
//            httpClient.close()
//            println("PocketBaseClient: HttpClient closed.")
//        }
        // Re-initialize for potential reuse if PocketBaseClient is a long-lived singleton
        // Or ensure this client instance is not used again after cleanup if it's shorter-lived.
        // If it's a true singleton meant to live for the app's lifetime and be reused after "logout/login",
        // then you might need to re-initialize realtimeScope and _eventSubscriptionFlow.
        // For simplicity, if it's a singleton, often cleanup is only done when app truly exits.
        // If tied to user session, then full cleanup and potentially allowing GC is better.
    }

    // --- Example methods for login/logout and event CRUD to make client complete ---
    // You would adapt these to your actual API calls
    suspend fun login(user: String, pass: String): Result<AuthResponse> {
        return try {
            val response: AuthResponse =
                httpClient.post("$baseUrl/api/collections/users/auth-with-password") {
                    contentType(ContentType.Application.Json)
                    setBody(mapOf("identity" to user, "password" to pass))
                }.body()
            settingsRepository.saveAuthToken(response.token)
            settingsRepository.saveUserId(response.record.id) // Assuming record.id is userId
            println("PocketBaseClient: Login successful. Token: ${response.token}")
            Result.success(response)
        } catch (e: Exception) {
            println("PocketBaseClient: Login failed: ${e.message}")
            Result.failure(e)
        }
    }

//    suspend fun logout() {
//        // PocketBase doesn't have a server-side logout endpoint that invalidates the token.
//        // Logout is client-side: clear the token.
//        settingsRepository.clearAuthToken()
//        settingsRepository.clearUserId()
//        sseClientId = null // Clear SSE client ID
//        println("PocketBaseClient: Logged out (cleared local token).")
//        // Optionally, could also cancel existing SSE job if desired on logout
//        // stopListeningToEvents()
//    }

    suspend fun logout(): Result<Unit> {
        try {
            // Use the proper PocketBase logout endpoint
            settingsRepository.getAuthToken()?.let { _ ->
                httpClient.post("$baseUrl/api/collections/users/auth-logout")
                println("Server-side logout successful")
            }
        } catch (e: Exception) {
            // Log the error but continue with local cleanup
            println("Server logout failed (continuing with local cleanup): ${e.message}")
        }

        // Always clear local authentication state
        return try {
            clearAuthState()
            Result.success(Unit)
        } catch (e: Exception) {
            // Catch if clearAuthState itself throws an unexpected error,
            println("PocketBaseClient: Critical error during clearAuthState: ${e.message}")
            Result.failure(e)
        }
    }

    private suspend fun clearAuthState() {
        settingsRepository.clearAuthToken()
        settingsRepository.clearUserId()
        sseClientId = null // Clear SSE client ID
        stopListeningToEvents()
        println("PocketBaseClient: Logged out (cleared local token).")
    }

    suspend fun getEvents(): Result<List<Event>> {
        return try {
            val response: PocketBaseListResponse<Event> =
                httpClient.get("$baseUrl/api/collections/events/records") {
                    // Auth header added by plugin
                    url { parameters.append("sort", "-created") } // Example: sort by newest
                }.body()
            Result.success(response.items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ... other CRUD methods (createEvent, updateEvent, deleteEvent)
    //  Make sure they use the httpClient and handle authentication correctly.
    // Example:
    suspend fun createEvent(event: Event): Result<Event> {
        return try {
            val response: EventData = httpClient.post("$baseUrl/api/collections/events/records") {
                contentType(ContentType.Application.Json)
                setBody(event.toEventData(owner = settingsRepository.getUserId() ?: ""))
            }.body()
            Result.success(response.toEvent())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateEvent(event: Event): Result<Event> = try {
        val response: EventData =
            httpClient.patch("$baseUrl/api/collections/events/records/${event.id}") {
                contentType(ContentType.Application.Json)
                setBody(event.toEventData())
            }.body()

        Result.success(response.toEvent())
    } catch (e: Exception) {
        println("Error during event update: ${e.message}")
        Result.failure(e)
    }

    suspend fun deleteEvent(eventId: String): Result<Boolean> = try {
        httpClient.delete("$baseUrl/api/collections/events/records/$eventId")
        Result.success(true)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun register(email: String, password: String, name: String): Result<User> = try {
        val response: User = httpClient.post("$baseUrl/api/collections/users/records") {
            contentType(ContentType.Application.Json)
            setBody(
                mapOf(
                    "email" to email,
                    "password" to password,
                    "passwordConfirm" to password,
                    "name" to name
                )
            )
        }.body()

        Result.success(response)
    } catch (e: Exception) {
        println("Error during registration: ${e.message}")
        Result.failure(e)
    }
}

// --- Helper Data Models for API Responses (Illustrative) ---
@Serializable
data class AuthResponse(
    val token: String,
    val record: UserRecord // Or your specific user model from PocketBase
)

@Serializable
data class UserRecord(
    val id: String,
    val email: String,
    // ... other user fields
)

@Serializable
data class PocketBaseListResponse<T>(
    val page: Int,
    val perPage: Int,
    val totalItems: Int,
    val totalPages: Int,
    val items: List<T>
)


// --- Dummy SettingsRepository for token management ---
// Replace with your actual implementation (e.g., using DataStore or multiplatform-settings)
interface SettingsRepository {
    suspend fun saveAuthToken(token: String)
    suspend fun getAuthToken(): String?
    suspend fun clearAuthToken()
    suspend fun saveUserId(userId: String) // Example if you need userId
    suspend fun getUserId(): String?
    suspend fun clearUserId()
}

class InMemorySettingsRepository : SettingsRepository {
    private var token: String? = null
    private var userId: String? = null
    override suspend fun saveAuthToken(token: String) {
        this.token = token
    }

    override suspend fun getAuthToken(): String? = token
    override suspend fun clearAuthToken() {
        this.token = null
    }

    override suspend fun saveUserId(userId: String) {
        this.userId = userId
    }

    override suspend fun getUserId(): String? = userId
    override suspend fun clearUserId() {
        this.userId = null
    }
}
