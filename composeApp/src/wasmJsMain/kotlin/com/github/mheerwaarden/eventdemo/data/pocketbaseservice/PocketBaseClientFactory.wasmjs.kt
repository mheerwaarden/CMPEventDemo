package com.github.mheerwaarden.eventdemo.data.pocketbaseservice

import com.github.mheerwaarden.eventdemo.data.preferences.PocketBaseClientType

actual fun createPocketBaseService(
    baseUrl: String,
    clientType: PocketBaseClientType
): PocketBaseService = when (clientType) {
        PocketBaseClientType.KTOR_ONLY -> PocketBaseKtorService(baseUrl)
        PocketBaseClientType.POCKETBASE_KOTLIN_KTOR_WEB -> PocketBaseKtorService(baseUrl)
        PocketBaseClientType.POCKETBASE_KOTLIN_JS_WEB -> PocketBaseWasmService(baseUrl)
        PocketBaseClientType.POCKETBASE_KOTLIN_ONLY -> throw UnsupportedOperationException("pocketbase-kotlin not supported on WasmJS platform")
    }
