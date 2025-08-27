package com.github.mheerwaarden.eventdemo.data.preferences

import kotlinx.serialization.Serializable

@Serializable
enum class PocketBaseClientType {
    KTOR_ONLY,                    // Custom ktor implementation
    POCKETBASE_KOTLIN_KTOR_WEB,   // pocketbase-kotlin lib + custom ktor for web
    POCKETBASE_KOTLIN_JS_WEB,     // pocketbase-kotlin lib + PocketBase JS SDK for web
    POCKETBASE_KOTLIN_ONLY;        // pocketbase-kotlin for all (custom fork of pocketbase-kotlin lib)
}
