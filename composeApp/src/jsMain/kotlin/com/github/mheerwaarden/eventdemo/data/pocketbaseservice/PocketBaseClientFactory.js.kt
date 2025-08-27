package com.github.mheerwaarden.eventdemo.data.pocketbaseservice

import com.github.mheerwaarden.eventdemo.data.preferences.PocketBaseClientType

actual class PocketBaseServiceFactory {
    actual fun create(baseUrl: String, clientType: PocketBaseClientType): PocketBaseService {
        return when (clientType) {
            PocketBaseClientType.KTOR_ONLY -> PocketBaseKtorService(baseUrl)
            PocketBaseClientType.POCKETBASE_KOTLIN_KTOR_WEB -> PocketBaseKtorService(baseUrl)
            PocketBaseClientType.POCKETBASE_KOTLIN_JS_WEB -> PocketBaseJsService(baseUrl)
            PocketBaseClientType.POCKETBASE_KOTLIN_ONLY -> throw UnsupportedOperationException("pocketbase-kotlin not supported on JS platform")
        }
    }
}