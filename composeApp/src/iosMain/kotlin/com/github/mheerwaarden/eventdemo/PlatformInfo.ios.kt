package com.github.mheerwaarden.eventdemo

import platform.UIKit.UIDevice
import kotlin.experimental.ExperimentalNativeApi

/**
 * iOS-specific implementation of PlatformInfo.
 */
class IOSPlatformInfo: PlatformInfo {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
    override val platformType: PlatformType = PlatformType.NATIVE

    /* For iOS, `isDebugBuild` checks if the binary was compiled in debug mode. */
    @OptIn(ExperimentalNativeApi::class)
    override val isDebugBuild: Boolean = Platform.isDebugBinary
}

actual fun getPlatformInfo(): PlatformInfo = IOSPlatformInfo()