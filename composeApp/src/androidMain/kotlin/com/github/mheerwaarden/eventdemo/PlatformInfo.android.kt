package com.github.mheerwaarden.eventdemo

import android.os.Build

/**
 * Android-specific implementation of PlatformInfo.
 */
class AndroidPlatformInfo : PlatformInfo {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"

    /* For Android, `isDebugBuild` is true if the app was built with the 'debug' build type. */
    override val isDebugBuild: Boolean = BuildConfig.DEBUG
}

actual fun getPlatformInfo(): PlatformInfo = AndroidPlatformInfo()