package com.github.mheerwaarden.eventdemo.data.pocketbaseservice

import kotlin.js.Promise

@JsModule("pocketbase")
external class PocketBaseJS(baseUrl: String) {
    val authStore: AuthStoreJS
    val realtime: RealtimeServiceJS
    fun collection(name: String): CollectionJS
    fun getFileUrl(record: RecordJS, filename: String, queryParams: dynamic = definedExternally): String
}

external class CollectionJS {
    suspend fun authWithPassword(email: String, password: String, options: dynamic): Promise<AuthResponseJS>
    suspend fun getList(page: Int = definedExternally, perPage: Int = definedExternally, queryParams: dynamic = definedExternally): Promise<RecordListJS>
    suspend fun create(data: dynamic, queryParams: dynamic = definedExternally): Promise<RecordJS>
    suspend fun update(id: String, data: dynamic, queryParams: dynamic = definedExternally): Promise<RecordJS>
    suspend fun delete(id: String, queryParams: dynamic = definedExternally): Promise<Boolean>
    suspend fun getOne(id: String, queryParams: dynamic = definedExternally): Promise<RecordJS>
}

external interface RecordJS {
    val id: String
    val collectionId: String
    val collectionName: String
    val created: String // ISO Date String (PocketBase system field)
    val updated: String // ISO Date String (PocketBase system field)
    // Other fields must be accessed dynamically, e.g., record.asDynamic().title
}

external interface RecordListJS {
    val page: Int
    val perPage: Int
    val totalItems: Int
    val totalPages: Int
    val items: Array<RecordJS>
}

external interface AuthStoreJS {
    val isValid: Boolean
    val token: String? // For debug
    val model: UserRecordJS? // User model after auth
    fun clear()
}

external interface AuthResponseJS {
    val token: String
    val record: UserRecordJS // User record
}

// Simplified RealtimeService for JS SDK
external interface RealtimeServiceJS {
    fun subscribe(topic: String, callback: (RealtimeDataJS) -> Unit, options: dynamic = definedExternally) : () -> Unit // Returns an unsubscribe function
    fun unsubscribe(topic: String = definedExternally) // Unsubscribe from specific or all
    fun connect()
    fun disconnect()
}

// Helper interfaces for type-safe property access since we can't use dynamic
external interface EventRecordJS : RecordJS {
    val title: String?
    val description: String?
    val startMillis: Number? // epoch millis as Double
    val endMillis: Number? // epoch millis as Double
    val location: String?
    val contact: String?
    val notes: String?
    val eventType: String?
    val eventCategory: String?
    val isOnline: Boolean?
    val htmlColor: String?
    val amount: Double?
    val price: Double?
    val owner: String?
    val viewers: Array<String>?
    val isPrivate: Boolean?
}

external interface UserRecordJS : RecordJS {
    val email: String?
    val name: String?
    val avatar: String?
}

external interface RealtimeDataJS {
    val action: String
    val record: RecordJS
}
