package com.github.mheerwaarden.eventdemo.data.pocketbaseservice

import kotlin.js.Promise

@JsModule("pocketbase")
external class PocketBaseWasm(baseUrl: String) {
    val authStore: AuthStoreWasm
    fun collection(name: String): CollectionWasm
    fun realtime(): RealtimeServiceWasm
    fun getFileUrl(record: JsAny, filename: String, queryParams: JsAny? = definedExternally): String
}

external class CollectionWasm {
    fun authWithPassword(email: String, password: String): Promise<AuthResponseWasm>
    fun getList(page: Int = definedExternally, perPage: Int = definedExternally, queryParams: JsAny? = definedExternally): Promise<RecordListWasm>
    fun create(data: JsAny, queryParams: JsAny? = definedExternally): Promise<RecordWasm>
    fun update(id: String, data: JsAny, queryParams: JsAny? = definedExternally): Promise<RecordWasm>
    fun delete(id: String, queryParams: JsAny? = definedExternally): Promise<JsBoolean>
    fun getOne(id: String, queryParams: JsAny? = definedExternally): Promise<RecordWasm>
}

external interface RecordWasm : JsAny {
    val id: String
    val collectionId: String
    val collectionName: String
    val created: String // ISO Date String (PocketBase system field)
    val updated: String // ISO Date String (PocketBase system field)
    // Other fields must be accessed via property access or cast to specific interfaces
}

external interface RecordListWasm : JsAny {
    val page: Int
    val perPage: Int
    val totalItems: Int
    val totalPages: Int
    val items: JsArray<RecordWasm>
}

external interface AuthStoreWasm : JsAny {
    val isValid: Boolean
    val token: String?
    val model: UserRecordWasm? // User model after auth
    fun clear()
}

external interface AuthResponseWasm : JsAny {
    val token: String
    val record: UserRecordWasm // User record
}

// Simplified RealtimeService for Wasm SDK
external interface RealtimeServiceWasm : JsAny {
    fun subscribe(collection: String, callback: (RealtimeDataWasm) -> Unit): () -> Unit // Returns an unsubscribe function
    fun subscribe(collection: String, recordId: String, callback: (RealtimeDataWasm) -> Unit): () -> Unit
    fun unsubscribe(topic: String = definedExternally) // Unsubscribe from specific or all
    fun connect()
    fun disconnect()
}

// Helper interfaces for type-safe property access since we can't use dynamic
external interface EventRecordWasm : RecordWasm {
    val title: String?
    val description: String?
    val startMillis: Double? // epoch millis as Double
    val endMillis: Double? // epoch millis as Double
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
    val viewers: JsArray<JsString>?
    val isPrivate: Boolean?
}

external interface UserRecordWasm : RecordWasm {
    val email: String?
    val name: String?
    val avatar: String?
}

external interface RealtimeDataWasm : JsAny {
    val action: String
    val record: RecordWasm
}

// Helper interface for working with JavaScript arrays in wasmJs
external interface JsArrayLike<T : JsAny> : JsAny {
    val length: Int
}

external fun <T : JsAny> jsArrayOf(vararg elements: T): JsArray<T>

external interface JsArray<T : JsAny> : JsArrayLike<T> {
    operator fun get(index: Int): T
    operator fun set(index: Int, value: T)
}