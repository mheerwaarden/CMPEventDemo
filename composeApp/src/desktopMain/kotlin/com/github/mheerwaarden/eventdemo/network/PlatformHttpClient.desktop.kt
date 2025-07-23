package com.github.mheerwaarden.eventdemo.network


import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO

actual fun createHttpClientWithEngine(): HttpClient = HttpClient(CIO) {
    engine {
        requestTimeout = 60_000
    }
}