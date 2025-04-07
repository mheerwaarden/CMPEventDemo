package com.github.mheerwaarden.eventdemo

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform