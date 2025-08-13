package com.github.mheerwaarden.eventdemo

import kotlinx.browser.window

/**
 * WebAssembly (WasmJs) specific implementation of PlatformInfo.
 */
class WasmPlatformInfo: PlatformInfo {
    override val name: String = "Web with Kotlin/Wasm"

    /*
     * For WasmJs (browser environments), the logic is similar to jsMain.
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

actual fun getPlatformInfo(): PlatformInfo = WasmPlatformInfo()