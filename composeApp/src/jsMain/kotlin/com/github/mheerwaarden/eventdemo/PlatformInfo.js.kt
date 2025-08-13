package com.github.mheerwaarden.eventdemo

import kotlinx.browser.window

/**
 * JavaScript-specific implementation of PlatformInfo.
 */
class JsPlatformInfo : PlatformInfo {
    override val name: String = "JavaScript"

    /*
     * For JavaScript (browser environments):
     * - Checks if the hostname suggests a local development environment (localhost)
     *   or a known ngrok domain.
     */
    override val isDebugBuild: Boolean by lazy {
        val hostname = window.location.hostname
        val isLocal = hostname == "localhost"
                || hostname.startsWith("192.168.")
                || hostname.startsWith("127.")
        val isNgrok = hostname.contains("ngrok-free.app") || hostname.contains("ngrok.io")
        isLocal || isNgrok
    }
}

actual fun getPlatformInfo(): PlatformInfo = JsPlatformInfo()