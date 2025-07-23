package com.github.mheerwaarden.eventdemo.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js

actual fun createHttpClientWithEngine(): HttpClient = HttpClient(Js)