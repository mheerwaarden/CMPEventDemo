package com.github.mheerwaarden.eventdemo.data.pocketbaseservice

import com.github.mheerwaarden.eventdemo.data.model.Event
import com.github.mheerwaarden.eventdemo.data.model.User
import com.github.mheerwaarden.eventdemo.getPlatformInfo
import com.github.mheerwaarden.eventdemo.network.createHttpClientWithEngine
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpTimeoutConfig
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.timeout
import io.ktor.client.request.accept
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
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
import io.ktor.http.Url
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

// NGROK free tier requires this header for programmatic access without interstitial.
// Use only for debug/dev builds.
private const val NGROK_SKIP_BROWSER_WARNING = "ngrok-skip-browser-warning"

class PocketBaseService(
    private val baseUrl: String,
    private val onAuthFailure: () -> Unit = {},
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
            logger = Logger.DEFAULT
            level = LogLevel.ALL // Max logging for debugging Ktor
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
                    println("PocketBaseService: Auth: Attempting to refresh token")
                    try {
                        // This POST request itself will use the Auth plugin, sending the
                        // 'oldTokens.accessToken'.
                        val response: AuthResponse =
                            client.post("$baseUrl/api/collections/users/auth-refresh") {
                                // PocketBase auth-refresh typically doesn't need a body if using the current valid token.
                            }.body()

                        val newToken = response.token
                        println("PocketBaseService: Auth: Token refreshed successfully. New token: $newToken")
                        settingsRepository.saveAuthToken(newToken)
                        if (response.record.id.isNotBlank()) {
                            settingsRepository.saveUserId(response.record.id)
                        }
                        BearerTokens(newToken, newToken) // Return new tokens
                    } catch (e: Exception) {
                        println("PocketBaseService: Auth: Failed to refresh token: ${e.message}")
                        // Clear tokens and notify about auth failure
                        settingsRepository.clearAuthToken()
                        settingsRepository.clearUserId()
                        onAuthFailure()
                        null // Return null to indicate refresh failure
                    }
                }
                sendWithoutRequest { request ->
                    // This condition ensures that the Authorization header is only sent
                    // to requests targeting your API domain. This is generally good practice.
                    // For the JS engine, Ktor might be conservative about when to send
                    // the Authorization header. This helps ensure it's sent.
                    request.url.host == Url(baseUrl).host && request.url.protocol.name == Url(
                        baseUrl
                    ).protocol.name
                }
            }
        }
        // Configure default request headers
        defaultRequest {
            if (getPlatformInfo().isDebugBuild) {
                header(NGROK_SKIP_BROWSER_WARNING, "true")
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
            println("PocketBaseService: Attempting to connect to SSE...")
            var attempts = 0
            val maxAttempts = 5 // Max reconnection attempts for certain failures

            while (isActive && attempts < maxAttempts) {
                try {
                    httpClient.prepareGet("$baseUrl/api/realtime") {
                        method = HttpMethod.Get
                        accept(ContentType.Text.EventStream)
                        headers {
                            append(HttpHeaders.CacheControl, "no-cache")
                            // Auth header will be added by the Auth plugin if token exists
                        }
                        // Explicitly disable timeout for this SSE request
                        timeout { requestTimeoutMillis = HttpTimeoutConfig.INFINITE_TIMEOUT_MS }
                    }.execute { response ->
                        println("PocketBaseService: SSE connection established, status: ${response.status}")
                        attempts = 0 // Reset attempts on successful connection

                        val channel: ByteReadChannel = response.bodyAsChannel()
                        while (isActive && !channel.isClosedForRead) {
                            val event = parseSseEvent(channel)
                            if (event != null) {
                                println("PocketBaseService: Received SSE Event: ID=${event.id}, Event=${event.event}, Data=${event.data}")
                                if (event.event == "PB_CONNECT") {
                                    try {
                                        val connectData =
                                            json.decodeFromString<PBConnectEventData>(event.data)
                                        sseClientId = connectData.clientId
                                        println("PocketBaseService: PB_CONNECT received, clientId: $sseClientId. Subscribing to collections: $collectionNames")
                                        subscribeToCollections(collectionNames)
                                    } catch (e: Exception) {
                                        println("PocketBaseService: Error parsing PB_CONNECT data: ${e.message}")
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
                                                println("PocketBaseService: Unknown action '${wrapper.action}'")
                                                null
                                            }
                                        }
                                        if (action != null) {
                                            val subscriptionState =
                                                SubscriptionState(action, wrapper.record)
                                            _eventSubscriptionFlow.emit(subscriptionState)
                                            println("PocketBaseService: Emitted SubscriptionState: $subscriptionState")
                                        }
                                    } catch (e: Exception) {
                                        println("PocketBaseService: Error parsing record data for event '${event.event}': ${e.message}")
                                        println("PocketBaseService: Failing data: ${event.data}")
                                    }
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    println("PocketBaseService: SSE Error: ${e.message}")
                    e.printStackTrace() // For detailed stack trace
                    sseClientId = null // Reset clientId on disconnection
                    attempts++
                    if (isActive && attempts < maxAttempts) {
                        val delayMillis = calculateBackoffDelay(attempts) // Exponential backoff
                        println("PocketBaseService: Retrying SSE connection in ${delayMillis / 1000}s (attempt $attempts/$maxAttempts)")
                        delay(delayMillis)
                    } else if (isActive) {
                        println("PocketBaseService: Max SSE reconnection attempts reached. Stopping.")
                        // Optionally emit an error state to the UI
                    }
                } finally {
                    if (!isActive || attempts >= maxAttempts) {
                        println("PocketBaseService: SSE connection loop ending (isActive: $isActive, attempts: $attempts).")
                    }
                }
                if (!isActive) break // Exit while loop if scope is cancelled
            }
        }
        println("PocketBaseService: startListeningToEvents launched job: $sseJob")
    }

    private suspend fun subscribeToCollections(collectionNames: List<String>) {
        val currentClientId = sseClientId
        if (currentClientId == null) {
            println("PocketBaseService: Cannot subscribe, clientId is null.")
            return
        }
        try {
            val requestBody = SubscriptionRequest(currentClientId, collectionNames)
            println("PocketBaseService: Sending subscription POST with body: $requestBody")
            val response = httpClient.post("$baseUrl/api/realtime") {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
                // Auth header will be added by the Auth plugin if token exists
            }
            println("PocketBaseService: Subscription POST response: ${response.status} - ${response.bodyAsText()}")
            if (!response.status.isSuccess()) {
                println("PocketBaseService: Failed to subscribe to collections. Status: ${response.status}")
            } else {
                println("PocketBaseService: Successfully subscribed to collections: $collectionNames")
            }
        } catch (e: Exception) {
            println("PocketBaseService: Error sending subscription POST: ${e.message}")
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
                    println("PocketBaseService: SSE Comment: $line")
                }

                else -> {
                    println("PocketBaseService: SSE Unknown line: $line")
                }
            }
        }
    }

    fun stopListeningToEvents() {
        println("PocketBaseService: stopListeningToEvents called. Cancelling job: $sseJob")
        sseJob?.cancel()
        sseJob = null
        sseClientId = null
        // Note: _eventSubscriptionFlow is a SharedFlow and doesn't need explicit closing here unless
        // the PocketBaseService itself is being destroyed and no longer needed.
        // If PocketBaseService is a singleton, this is fine.
    }

    // Call when PocketBaseService is no longer needed (e.g. ViewModel onCleared)
    fun cleanup() {
        println("PocketBaseService: Cleaning up...")
        stopListeningToEvents()
        if (realtimeScope.isActive) {
            realtimeScope.cancel("PocketBaseService cleanup initiated")
        }
        if (httpClient.engine.isActive) { // Check before closing
            httpClient.close()
            println("PocketBaseService: HttpClient closed.")
        }
        // Re-initialize for potential reuse if PocketBaseService is a long-lived singleton
        // Or ensure this client instance is not used again after cleanup if it's shorter-lived.
        // If it's a true singleton meant to live for the app's lifetime and be reused after "logout/login",
        // then you might need to re-initialize realtimeScope and _eventSubscriptionFlow.
        // For simplicity, if it's a singleton, often cleanup is only done when app truly exits.
        // If tied to user session, then full cleanup and potentially allowing GC is better.
    }

    // --- Example methods for login/logout and event CRUD to make client complete ---
    // You would adapt these to your actual API calls
    suspend fun login(user: String, pass: String): Result<AuthResponse> = try {
            val response: AuthResponse =
                httpClient.post("$baseUrl/api/collections/users/auth-with-password") {
                    contentType(ContentType.Application.Json)
                    setBody(mapOf("identity" to user, "password" to pass))
                }.body()
            settingsRepository.saveAuthToken(response.token)
            settingsRepository.saveUserId(response.record.id) // Assuming record.id is userId
            println("PocketBaseService: Login successful. Token: ${response.token}")
            Result.success(response)
        } catch (e: Exception) {
            println("PocketBaseService: Login failed: ${e.message}")
            Result.failure(e)
        }

    suspend fun logout(): Result<Unit> {
        try {
            // Use the proper PocketBase logout endpoint
            settingsRepository.getAuthToken()?.let { _ ->
                httpClient.post("$baseUrl/api/collections/users/auth-logout")
                println("PocketBaseService: Server-side logout successful")
            }
        } catch (e: Exception) {
            // Log the error but continue with local cleanup
            println("PocketBaseService: Server logout failed (continuing with local cleanup): ${e.message}")
        }

        // Always clear local authentication state
        return try {
            clearAuthState()
            Result.success(Unit)
        } catch (e: Exception) {
            // Catch if clearAuthState itself throws an unexpected error,
            println("PocketBaseService: Critical error during clearAuthState: ${e.message}")
            Result.failure(e)
        }
    }

    private suspend fun clearAuthState() {
        settingsRepository.clearAuthToken()
        settingsRepository.clearUserId()
        stopListeningToEvents()
        println("PocketBaseService: Logged out (cleared local token).")
    }

    suspend fun getEvents(): Result<List<Event>> = try {
        val response: PocketBaseListResponse<Event> =
            httpClient.get("$baseUrl/api/collections/events/records") {
                println("PocketBaseService.getEvents")
                url { parameters.append("sort", "-created") } // sort by newest
                // Auth header added by plugin
            }.body()
        Result.success(response.items)
    } catch (e: Exception) {
        println("PocketBaseService.getEvents: Exception: ${e.message}")
        Result.failure(e)
    }

    suspend fun createEvent(event: Event): Result<Event> = try {
        val response: EventData = httpClient.post("$baseUrl/api/collections/events/records") {
            contentType(ContentType.Application.Json)
            setBody(event.toEventData(owner = settingsRepository.getUserId() ?: ""))
        }.body()
        Result.success(response.toEvent())
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateEvent(event: Event): Result<Event> = try {
        val response: EventData =
            httpClient.patch("$baseUrl/api/collections/events/records/${event.id}") {
                contentType(ContentType.Application.Json)
                setBody(event.toEventData())
            }.body()

        Result.success(response.toEvent())
    } catch (e: Exception) {
        println("PocketBaseService: Error during event update: ${e.message}")
        Result.failure(e)
    }

    suspend fun deleteEvent(eventId: String): Result<Boolean> = try {
        httpClient.delete("$baseUrl/api/collections/events/records/$eventId")
        Result.success(true)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun register(email: String, password: String, name: String): Result<User> = try {
        println("PocketBaseService: Registering user: $email")
        val registrationUrl = "$baseUrl/api/collections/users/records"
        val response: User = httpClient.post(registrationUrl) {
            contentType(ContentType.Application.Json)
            setBody(
                mapOf(
                    "email" to email,
                    "password" to password,
                    "passwordConfirm" to password,
                    "name" to name,
                    "username" to email,
                    "emailVisibility" to "false"
                )
            )
        }.body()

        println("PocketBaseService: Registration successful: ${response.email}")
        Result.success(response)
    } catch (e: ClientRequestException) {
        // This exception is thrown for 4xx and 5xx responses
        // Get the raw error response body
        val errorResponseText = e.response.body<String>()
        println("PocketBaseService: ClientRequestException during registration: ${e.message}\nResponse: $errorResponseText")
        try {
            // Attempt to deserialize it into our PocketBaseErrorResponse structure
            val pocketBaseError = json.decodeFromString<PocketBaseErrorResponse>(errorResponseText)

            // Construct a more informative error message
            var detailedErrorMessage =
                "PocketBase registration failed: ${pocketBaseError.message}\n"
            pocketBaseError.data.forEach { (field, detail) ->
                detailedErrorMessage += "  - Field '$field': ${detail.message} (code: ${detail.code})\n"
            }
            Result.failure(Exception(detailedErrorMessage.trim(), e)) // Wrap original exception
        } catch (jsonException: Exception) {
            // If deserializing the error fails, fall back to the raw response
            Result.failure(
                Exception(
                    "Registration failed with status ${e.response.status}. Response: $errorResponseText",
                    e
                )
            )
        }
    } catch (e: Exception) {
        println("PocketBaseService: Error during registration: ${e.message}")
        Result.failure(e)
    }

    private fun calculateBackoffDelay(attempt: Int, baseDelay: Long = 2000): Long {
        val exponentialDelay = baseDelay * (1 shl (attempt - 1)) // 2s, 4s, 8s, 16s...
        // Add randomness to prevent multiple clients from reconnecting at exactly the same time
        val jitter = (0..1000).random()
        return exponentialDelay + jitter
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

// Data class for PocketBase's detailed validation error response
@Serializable
data class PocketBaseErrorDetail(
    val code: String,
    val message: String
)

@Serializable
data class PocketBaseErrorResponse(
    val code: Int,
    val message: String,
    val data: Map<String, PocketBaseErrorDetail> // This will hold field-specific errors
)