package com.github.mheerwaarden.eventdemo

class JsPlatform: Platform {
    override val name: String = "JavaScript"
}

actual fun getPlatform(): Platform = JsPlatform()