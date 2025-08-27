package com.github.mheerwaarden.eventdemo.data.pocketbaseservice

import com.github.mheerwaarden.eventdemo.data.model.Event
import com.github.mheerwaarden.eventdemo.data.model.IEvent
import com.github.mheerwaarden.eventdemo.data.model.User
import com.github.mheerwaarden.eventdemo.network.createHttpClientWithEngine
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpTimeoutConfig
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
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
import kotlinx.serialization.json.Json

// Represents the state from the subscription
sealed class SubscriptionAction {
    data object CREATE : SubscriptionAction()
    data object UPDATE : SubscriptionAction()
    data object DELETE : SubscriptionAction()
    data object NOOP : SubscriptionAction()
    data class ERROR(val message: String) : SubscriptionAction()
}

data class SubscriptionState<T>(
    val action: SubscriptionAction,
    val dataObject: T?,
    val rawRecord: Any? = null
)

class PocketBaseKtorService(
    private val baseUrl: String,
    private val onAuthFailure: () -> Unit = {},
    private val settingsRepository: SettingsRepository = InMemorySettingsRepository() // For auth token
) : PocketBaseService {

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
                    println("PocketBaseKtorService: Auth: Attempting to refresh token")
                    try {
                        // This POST request itself will use the Auth plugin, sending the
                        // 'oldTokens.accessToken'.
                        val response: AuthResult =
                            client.post("$baseUrl/api/collections/users/auth-refresh") {
                                // PocketBase auth-refresh typically doesn't need a body if using the current valid token.
                            }.body()

                        val newToken = response.token
                        println("PocketBaseKtorService: Auth: Token refreshed successfully. New token: $newToken")
                        settingsRepository.saveAuthToken(newToken)
                        response.record.id.let { userId ->
                            if (userId.isNotBlank()) {
                                settingsRepository.saveUserId(userId)
                            }
                        }
                        BearerTokens(newToken, newToken) // Return new tokens
                    } catch (e: Exception) {
                        println("PocketBaseKtorService: Auth: Failed to refresh token: ${e.message}")
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
    }

    private var sseClientId: String? = null
    private val realtimeScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var sseJob: Job? = null

    // For emitting events from the SSE stream to multiple collectors
    private val _eventSubscriptionFlow = MutableSharedFlow<SubscriptionState<IEvent>>(replay = 0)
    override val eventSubscriptionFlow = _eventSubscriptionFlow.asSharedFlow()

    override fun subscribeToEvents(onUpdate: (IEvent) -> Unit): () -> Unit {
        startListeningToEvents()
        return { stopListeningToEvents() }
    }

    override fun startListeningToEvents(collectionNames: List<String>) {
        // Cancel any existing job
        sseJob?.cancel()

        // Start a new job
        sseJob = realtimeScope.launch {
            println("PocketBaseKtorService: Attempting to connect to SSE...")
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
                        println("PocketBaseKtorService: SSE connection established, status: ${response.status}")
                        attempts = 0 // Reset attempts on successful connection

                        val channel: ByteReadChannel = response.bodyAsChannel()
                        while (isActive && !channel.isClosedForRead) {
                            val serverSentEvent = parseSseEvent(channel)
                            if (serverSentEvent != null) {
                                println("PocketBaseKtorService: Received SSE Event: ID=${serverSentEvent.id}, Event=${serverSentEvent.event}, Data=${serverSentEvent.data}")
                                if (serverSentEvent.event == "PB_CONNECT") {
                                    try {
                                        val connectData =
                                            json.decodeFromString<PBConnectEventData>(
                                                serverSentEvent.data
                                            )
                                        sseClientId = connectData.clientId
                                        println("PocketBaseKtorService: PB_CONNECT received, clientId: $sseClientId. Subscribing to collections: $collectionNames")
                                        subscribeToCollections(collectionNames)
                                    } catch (e: Exception) {
                                        println("PocketBaseKtorService: Error parsing PB_CONNECT data: ${e.message}")
                                    }
                                } else
                                    // Check if it's one of the collections we care about
                                    if (collectionNames.contains(serverSentEvent.event)) {
                                    try {
                                        val wrapper =
                                            json.decodeFromString<RealtimeDataWrapper<Event>>(
                                                serverSentEvent.data
                                            )
                                        val action = when (wrapper.action) {
                                            "create" -> SubscriptionAction.CREATE
                                            "update" -> SubscriptionAction.UPDATE
                                            "delete" -> SubscriptionAction.DELETE
                                            else -> {
                                                println("PocketBaseKtorService: Unknown action '${wrapper.action}'")
                                                null
                                            }
                                        }
                                        if (action != null) {
                                            val subscriptionState =
                                                SubscriptionState(
                                                    action = action,
                                                    dataObject = wrapper.record as IEvent,
                                                    rawRecord = serverSentEvent.data
                                                )
                                            _eventSubscriptionFlow.emit(subscriptionState)
                                            println("PocketBaseKtorService: Emitted SubscriptionState: $subscriptionState")
                                        }
                                    } catch (e: Exception) {
                                        println("PocketBaseKtorService: Error parsing record data for event '${serverSentEvent.event}': ${e.message}")
                                        println("PocketBaseKtorService: Failing data: ${serverSentEvent.data}")
                                    }
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    println("PocketBaseKtorService: SSE Error: ${e.message}")
                    e.printStackTrace() // For detailed stack trace
                    sseClientId = null // Reset clientId on disconnection
                    attempts++
                    if (isActive && attempts < maxAttempts) {
                        val delayMillis = calculateBackoffDelay(attempts) // Exponential backoff
                        println("PocketBaseKtorService: Retrying SSE connection in ${delayMillis / 1000}s (attempt $attempts/$maxAttempts)")
                        delay(delayMillis)
                    } else if (isActive) {
                        println("PocketBaseKtorService: Max SSE reconnection attempts reached. Stopping.")
                        _eventSubscriptionFlow.emit(
                            SubscriptionState(
                                action = SubscriptionAction.ERROR("Max subscribe reconnection attempts reached: ${e.message}"),
                                dataObject = null
                            )
                        )
                    }
                } finally {
                    if (!isActive || attempts >= maxAttempts) {
                        println("PocketBaseKtorService: SSE connection loop ending (isActive: $isActive, attempts: $attempts).")
                    }
                }
                if (!isActive) break // Exit while loop if scope is cancelled
            }
        }
        println("PocketBaseKtorService: startListeningToEvents launched job: $sseJob")
    }

    private suspend fun subscribeToCollections(collectionNames: List<String>) {
        val currentClientId = sseClientId
        if (currentClientId == null) {
            println("PocketBaseKtorService: Cannot subscribe, clientId is null.")
            return
        }
        try {
            val requestBody = SubscriptionRequest(currentClientId, collectionNames)
            println("PocketBaseKtorService: Sending subscription POST with body: $requestBody")
            val response = httpClient.post("$baseUrl/api/realtime") {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
                // Auth header will be added by the Auth plugin if token exists
            }
            println("PocketBaseKtorService: Subscription POST response: ${response.status} - ${response.bodyAsText()}")
            if (!response.status.isSuccess()) {
                println("PocketBaseKtorService: Failed to subscribe to collections. Status: ${response.status}")
            } else {
                println("PocketBaseKtorService: Successfully subscribed to collections: $collectionNames")
            }
        } catch (e: Exception) {
            println("PocketBaseKtorService: Error sending subscription POST: ${e.message}")
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
                    println("PocketBaseKtorService: SSE Comment: $line")
                }

                else -> {
                    println("PocketBaseKtorService: SSE Unknown line: $line")
                }
            }
        }
    }

    override fun stopListeningToEvents() {
        println("PocketBaseKtorService: stopListeningToEvents called. Cancelling job: $sseJob")
        sseJob?.cancel()
        sseJob = null
        sseClientId = null
        // Note: _eventSubscriptionFlow is a SharedFlow and doesn't need explicit closing here unless
        // the PocketBaseService itself is being destroyed and no longer needed.
        // If PocketBaseService is a singleton, this is fine.
    }

    // Call when PocketBaseService is no longer needed (e.g. ViewModel onCleared)
    override fun cleanup() {
        println("PocketBaseKtorService: Cleaning up...")
        stopListeningToEvents()
        if (realtimeScope.isActive) {
            realtimeScope.cancel("PocketBaseService cleanup initiated")
        }
        if (httpClient.engine.isActive) { // Check before closing
            httpClient.close()
            println("PocketBaseKtorService: HttpClient closed.")
        }
        // Re-initialize for potential reuse if PocketBaseService is a long-lived singleton
        // Or ensure this client instance is not used again after cleanup if it's shorter-lived.
        // If it's a true singleton meant to live for the app's lifetime and be reused after "logout/login",
        // then you might need to re-initialize realtimeScope and _eventSubscriptionFlow.
        // For simplicity, if it's a singleton, often cleanup is only done when app truly exits.
        // If tied to user session, then full cleanup and potentially allowing GC is better.
    }

    override suspend fun login(email: String, password: String): PocketBaseResult<AuthResult> =
        try {
            val response: AuthResult =
                httpClient.post("$baseUrl/api/collections/users/auth-with-password") {
                    contentType(ContentType.Application.Json)
                    setBody(mapOf("identity" to email, "password" to password))
                }.body()
            settingsRepository.saveAuthToken(response.token)
            settingsRepository.saveUserId(response.record.id) // Assuming record.id is userId -- TODO: record.id is id of user record, so is this correct?
            println("PocketBaseKtorService: Login successful. Token: ${response.token}")
            PocketBaseResult.Success(response)
        } catch (e: Exception) {
            println("PocketBaseKtorService: Login failed: ${e.message}")
            PocketBaseResult.Error(message = "Login failed: ${e.message}")
        }

    override suspend fun logout(): PocketBaseResult<Unit> {
        try {
            // Use the proper PocketBase logout endpoint
            settingsRepository.getAuthToken()?.let { _ ->
                httpClient.post("$baseUrl/api/collections/users/auth-logout")
                println("PocketBaseKtorService: Server-side logout successful")
            }
        } catch (e: Exception) {
            // Log the error but continue with local cleanup
            println("PocketBaseKtorService: Server logout failed (continuing with local cleanup): ${e.message}")
        }

        // Always clear local authentication state
        return try {
            clearAuthState()
            PocketBaseResult.Success(Unit)
        } catch (e: Exception) {
            // Catch if clearAuthState itself throws an unexpected error,
            println("PocketBaseKtorService: Critical error during clearAuthState: ${e.message}")
            PocketBaseResult.Error("PocketBaseKtorService: Error during clear of authentication state: ${e.message}")
        }
    }

    private suspend fun clearAuthState() {
        settingsRepository.clearAuthToken()
        settingsRepository.clearUserId()
        stopListeningToEvents()
        println("PocketBaseKtorService: Logged out (cleared local token).")
    }

    override suspend fun getEvents(): PocketBaseResult<List<Event>> = try {
        val response: PocketBaseListResponse<Event> =
            httpClient.get("$baseUrl/api/collections/events/records") {
                println("PocketBaseService.getEvents")
                url { parameters.append("sort", "-created") } // sort by newest
                // Auth header added by plugin
            }.body()
        PocketBaseResult.Success(response.items)
    } catch (e: Exception) {
        println("PocketBaseService.getEvents: Exception: ${e.message}")
        PocketBaseResult.Error(e.message ?: "Exception during getting events")
    }

    override suspend fun createEvent(event: IEvent): PocketBaseResult<IEvent> = try {
        val response: Event = httpClient.post("$baseUrl/api/collections/events/records") {
            contentType(ContentType.Application.Json)
            setBody(event.toEvent().copy(owner = settingsRepository.getUserId() ?: ""))
        }.body()
        PocketBaseResult.Success(response)
    } catch (e: Exception) {
        println("PocketBaseKtorService: Error during event creation: ${e.message}")
        PocketBaseResult.Error("PocketBaseKtorService: Error during event creation: ${e.message}")
    }

    override suspend fun updateEvent(event: IEvent): PocketBaseResult<IEvent> = try {
        val response: Event =
            httpClient.patch("$baseUrl/api/collections/events/records/${event.id}") {
                contentType(ContentType.Application.Json)
                setBody(event)
            }.body()

        PocketBaseResult.Success(response)
    } catch (e: Exception) {
        println("PocketBaseKtorService: Error during event update: ${e.message}")
        PocketBaseResult.Error("PocketBaseKtorService: Error during event update: ${e.message}")
    }

    override suspend fun deleteEvent(eventId: String): PocketBaseResult<Boolean> = try {
        httpClient.delete("$baseUrl/api/collections/events/records/$eventId")
        PocketBaseResult.Success(true)
    } catch (e: Exception) {
        println("PocketBaseKtorService: Error during event deletion: ${e.message}")
        PocketBaseResult.Error("PocketBaseKtorService: Error during event deletion: ${e.message}")
    }

    override suspend fun register(
        email: String,
        password: String,
        passwordConfirm: String,
        name: String
    ): PocketBaseResult<AuthResult> {
        return if (password != passwordConfirm) {
            PocketBaseResult.Error("PocketBaseKtorService: Passwords do not match")
        } else try {
            println("PocketBaseKtorService: Registering user: $email")
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

            println("PocketBaseKtorService: Registration successful: ${response.email}")

            // Auto-login after registration
            login(email, password)
        } catch (e: ClientRequestException) {
            // This exception is thrown for 4xx and 5xx responses
            // Get the raw error response body
            val errorResponseText = e.response.body<String>()
            println("PocketBaseKtorService: ClientRequestException during registration: ${e.message}\nResponse: $errorResponseText")
            try {
                // Attempt to deserialize it into our PocketBaseErrorResponse structure
                val pocketBaseError =
                    json.decodeFromString<PocketBaseErrorResponse>(errorResponseText)

                // Construct a more informative error message
                var detailedErrorMessage =
                    "PocketBase registration failed: ${pocketBaseError.message}\n"
                pocketBaseError.data.forEach { (field, detail) ->
                    detailedErrorMessage += "  - Field '$field': ${detail.message} (code: ${detail.code})\n"
                }
                PocketBaseResult.Error(detailedErrorMessage.trim())
            } catch (jsonException: Exception) {
                // If deserializing the error fails, fall back to the raw response
                PocketBaseResult.Error(
                    "PocketBaseKtorService: Registration failed with status ${e.response.status}. Response: $errorResponseText",
                )
            }
        } catch (e: Exception) {
            println("PocketBaseKtorService: Error during registration: ${e.message}")
            PocketBaseResult.Error("PocketBaseKtorService: Error during registration: ${e.message}")
        }
    }

    override suspend fun isAuthenticated(): Boolean {
        val token = settingsRepository.getAuthToken()
        return !token.isNullOrBlank()
    }

    override suspend fun getCurrentUser(): PocketBaseResult<User?> = try {
        val userId = settingsRepository.getUserId()
        if (userId.isNullOrBlank()) {
            PocketBaseResult.Success(null)
        }
        val response: User =
            httpClient.get("$baseUrl/api/collections/users/records/$userId").body()
        PocketBaseResult.Success(response)
    } catch (e: Exception) {
        println("PocketBaseKtorService: Error while getting current user: ${e.message}")
        PocketBaseResult.Error("PocketBaseKtorService: while getting current user: ${e.message}")
    }

    private fun calculateBackoffDelay(attempt: Int, baseDelay: Long = 2000): Long {
        val exponentialDelay = baseDelay * (1 shl (attempt - 1)) // 2s, 4s, 8s, 16s...
        // Add randomness to prevent multiple clients from reconnecting at exactly the same time
        val jitter = (0..1000).random()
        return exponentialDelay + jitter
    }

}

// --- Dummy SettingsRepository for token management ---
// TODO: Replace with an actual implementation (e.g., using DataStore or multiplatform-settings)
interface SettingsRepository {
    suspend fun saveAuthToken(token: String)
    suspend fun getAuthToken(): String?
    suspend fun clearAuthToken()
    suspend fun saveUserId(userId: String?) // Example if you need userId
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

    override suspend fun saveUserId(userId: String?) {
        this.userId = userId
    }

    override suspend fun getUserId(): String? = userId
    override suspend fun clearUserId() {
        this.userId = null
    }
}

