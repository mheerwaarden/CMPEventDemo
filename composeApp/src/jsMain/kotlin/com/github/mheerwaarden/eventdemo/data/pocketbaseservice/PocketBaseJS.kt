package com.github.mheerwaarden.eventdemo.data.pocketbaseservice

@JsModule("pocketbase")
@JsNonModule
external class PocketBaseJS(baseUrl: String) {
    val authStore: AuthStoreJS
    fun collection(name: String): CollectionJS
    fun realtime(): RealtimeServiceJS
    fun getFileUrl(record: RecordJS, filename: String, queryParams: dynamic = definedExternally): String
}

external class CollectionJS {
    suspend fun authWithPassword(email: String, password: String): AuthResponseJS
    suspend fun getList(page: Int = definedExternally, perPage: Int = definedExternally, queryParams: dynamic = definedExternally): RecordListJS
    suspend fun create(data: dynamic, queryParams: dynamic = definedExternally): RecordJS
    suspend fun update(id: String, data: dynamic, queryParams: dynamic = definedExternally): RecordJS
    suspend fun delete(id: String, queryParams: dynamic = definedExternally): Boolean
    suspend fun getOne(id: String, queryParams: dynamic = definedExternally): RecordJS
}

external interface RecordJS {
    val id: String
    val collectionId: String
    val collectionName: String
    val created: String // ISO Date String (PocketBase system field)
    val updated: String // ISO Date String (PocketBase system field)
    // Other fields are accessed dynamically, e.g., record.asDynamic().title
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
    val model: RecordJS? // User model after auth
    fun clear()
}

external interface AuthResponseJS {
    val token: String
    val record: RecordJS // User record
}

// Simplified RealtimeService for JS SDK
external interface RealtimeServiceJS {
    fun subscribe(collection: String, callback: (dynamic) -> Unit) : () -> Unit // Returns an unsubscribe function
    fun subscribe(collection: String, recordId: String, callback: (dynamic) -> Unit) : () -> Unit
    fun unsubscribe(topic: String = definedExternally) // Unsubscribe from specific or all
    fun connect()
    fun disconnect()
}


