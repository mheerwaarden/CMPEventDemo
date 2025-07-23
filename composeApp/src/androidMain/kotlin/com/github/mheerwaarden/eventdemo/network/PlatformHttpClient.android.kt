package com.github.mheerwaarden.eventdemo.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android

actual fun createHttpClientWithEngine(): HttpClient = HttpClient(Android) {
    engine {
        connectTimeout = 60_000
        socketTimeout = 60_000
    }
}
