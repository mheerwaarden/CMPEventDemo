package com.github.mheerwaarden.eventdemo

enum class Platform {
    JVM, JS, NATIVE, WASM
}

expect val currentPlatform: Platform

fun isJs(): Boolean = currentPlatform == Platform.JS
fun isJvm(): Boolean = currentPlatform == Platform.JVM
fun isNative(): Boolean = currentPlatform == Platform.NATIVE
fun isWasm(): Boolean = currentPlatform == Platform.WASM