package com.github.mheerwaarden.eventdemo

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