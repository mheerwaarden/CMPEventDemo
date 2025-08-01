package com.github.mheerwaarden.eventdemo.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.pingInterval
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * Leave the common configuration to the caller.
 * @return a Ktor HttpClient with the platform-specific engine.
 */
expect fun createHttpClientWithEngine(): HttpClient

/** @return a configured platform dependent Ktor HttpClient */
fun createPlatformHttpClient(): HttpClient = createHttpClientWithEngine().config {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            encodeDefaults = false
        })
    }
    install(WebSockets) {
        pingInterval = 20.toDuration(DurationUnit.SECONDS)
    }
    install(Auth)
    install(HttpTimeout) {
        requestTimeoutMillis = 30_000
        connectTimeoutMillis = 15_000
        socketTimeoutMillis = 15_000 // Disable socket timeout for SSE in httpClient.prepareGet
    }
    install(Logging) {
        level = LogLevel.INFO
    }
}