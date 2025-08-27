package com.github.mheerwaarden.eventdemo

expect val currentPlatformInfo: PlatformInfo

fun isJs(): Boolean = currentPlatformInfo.platformType == PlatformType.JS
fun isJvm(): Boolean = currentPlatformInfo.platformType == PlatformType.JVM
fun isNative(): Boolean = currentPlatformInfo.platformType == PlatformType.NATIVE
fun isWasm(): Boolean = currentPlatformInfo.platformType == PlatformType.WASM