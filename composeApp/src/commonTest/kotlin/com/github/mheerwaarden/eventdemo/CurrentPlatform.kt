package com.github.mheerwaarden.eventdemo

enum class Platform {
    JVM, JS, NATIVE, WASM
}

expect val currentPlatformInfo: PlatformInfo

fun isJs(): Boolean = currentPlatformInfo == PlatformInfo.JS
fun isJvm(): Boolean = currentPlatformInfo == PlatformInfo.JVM
fun isNative(): Boolean = currentPlatformInfo == PlatformInfo.NATIVE
fun isWasm(): Boolean = currentPlatformInfo == PlatformInfo.WASM