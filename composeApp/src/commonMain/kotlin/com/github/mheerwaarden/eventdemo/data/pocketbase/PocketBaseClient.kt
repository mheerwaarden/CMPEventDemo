package com.github.mheerwaarden.eventdemo.data.pocketbase

import com.github.mheerwaarden.eventdemo.data.model.AuthResponse
import com.github.mheerwaarden.eventdemo.data.model.Event
import com.github.mheerwaarden.eventdemo.data.model.User
import com.github.mheerwaarden.eventdemo.network.createPlatformHttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class PocketBaseClient(private val baseUrl: String = "http://127.0.0.1:8090") {
    // Use configured platform-specific client
    private val client = createPlatformHttpClient()

    private var authToken: String? = null
    private var currentUser: User? = null

    // Authentication
    suspend fun login(email: String, password: String): Result<AuthResponse> = try {
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
        val response = client.post("$baseUrl/api/collections/users/records") {
            contentType(ContentType.Application.Json)
            setBody(mapOf(
                "email" to email,
                "password" to password,
                "passwordConfirm" to password,
                "name" to name
            ))
        }

        val user = response.body<User>()
        Result.success(user)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Events CRUD
    suspend fun createEvent(event: Event): Result<Event> = try {
        val response = client.post("$baseUrl/api/collections/events/records") {
            contentType(ContentType.Application.Json)
            bearerAuth(authToken!!)
            setBody(event.copy(owner = currentUser?.id ?: 0))
        }

        Result.success(response.body<Event>())
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
            Json.decodeFromString<Event>(Json.encodeToString(it))
        }

        Result.success(items)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateEvent(event: Event): Result<Event> = try {
        val response = client.patch("$baseUrl/api/collections/events/records/${event.id}") {
            contentType(ContentType.Application.Json)
            bearerAuth(authToken!!)
            setBody(event)
        }

        Result.success(response.body<Event>())
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteEvent(eventId: Long): Result<Boolean> = try {
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
    fun subscribeToEvents(onEvent: (Event) -> Unit): Flow<Event> = flow {
        client.webSocket("$baseUrl/api/realtime") {
            // Send subscription request
            send(Json.encodeToString(mapOf(
                "clientId" to "kotlin-client",
                "subscriptions" to listOf("events")
            )))

            // Listen for messages
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    try {
                        val message = Json.parseToJsonElement(frame.readText()).jsonObject
                        val action = message["action"]?.jsonPrimitive?.content

                        if (action in listOf("create", "update", "delete")) {
                            val record = message["record"]?.let {
                                Json.decodeFromJsonElement<Event>(it)
                            }
                            record?.let {
                                onEvent(it)  // Call the callback
                                emit(it)     // Also emit to Flow
                            }
                        }
                    } catch (e: Exception) {
                        println("Error parsing realtime message: ${e.message}")
                    }
                }
            }
        }
    }
}
