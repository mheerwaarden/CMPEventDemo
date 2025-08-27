package com.github.mheerwaarden.eventdemo.data.pocketbaseservice

import com.github.mheerwaarden.eventdemo.data.model.User
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// --- Helper Data Models for API Responses ---

@Serializable
data class AuthResult(
    val token: String,
    val record: User
)

@Serializable
data class PocketBaseListResponse<T>(
    val page: Int,
    val perPage: Int,
    val totalItems: Int,
    val totalPages: Int,
    val items: List<T>
)

sealed class PocketBaseResult<out T> {
    data class Success<T>(val data: T) : PocketBaseResult<T>()
    data class Error(val message: String, val code: Int? = null) : PocketBaseResult<Nothing>()
}

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

// Model classes for PocketBase's detailed validation error response

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
