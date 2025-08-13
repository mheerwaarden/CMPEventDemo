package com.github.mheerwaarden.eventdemo

/**
 * JVM (Desktop) specific implementation of PlatformInfo.
 */
class JVMPlatformInfo : PlatformInfo {
    override val name: String = "Java ${System.getProperty("java.version")}"

    /**
     * For JVM (Desktop):
     * - Checks a system property e.g., "eventdemo.debug=true".
     * - Set this property when running your desktop application:
     *   `java -eventdemo.debug=true -jar event_demo_app.jar`
     * - Defaults to false if the property is not set or not "true".
     */
    override val isDebugBuild: Boolean = System.getProperty("eventdemo.debug", "false").toBoolean()
}

actual fun getPlatformInfo(): PlatformInfo = JVMPlatformInfo()