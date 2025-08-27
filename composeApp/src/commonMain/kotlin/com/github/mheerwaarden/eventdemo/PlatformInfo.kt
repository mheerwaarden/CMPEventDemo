package com.github.mheerwaarden.eventdemo

enum class PlatformType {
    JVM, JS, NATIVE, WASM
}

/**
 * Provides information about the current platform and build environment.
 */
interface PlatformInfo {
    /**
     * The name of the current platform:
     * - Android: "Android <version>"
     * - iOS: "iOS <version>"
     * - JS: "JavaScript"
     * - WasmJs: "JavaScript"
     * - JVM: "JVM"
     *
     * @return The name of the current platform.
     */
    val name: String

    /**
     * The specific [PlatformType] the application is currently running on.
     * This provides a more formal classification than [name], allowing for
     * platform-specific logic.
     *
     * @return The current [PlatformType].
     */
    val platformType: PlatformType

    /**
     * Indicates if the current build is intended for debugging or development.
     * The exact definition of "debug" can vary by platform.
     * - Android: Typically corresponds to the 'debug' build type.
     * - iOS: Corresponds to binaries compiled with the debug configuration.
     * - JS/WasmJs: May rely on webpack mode, specific global variables, or hostname checks.
     * - JVM: May rely on system properties or arguments passed at runtime.
     *
     * @return True if the current build is in debug mode, false otherwise.
     */
    val isDebugBuild: Boolean
}

expect fun getPlatformInfo(): PlatformInfo