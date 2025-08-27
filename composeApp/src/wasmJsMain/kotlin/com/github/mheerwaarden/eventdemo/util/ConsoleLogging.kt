package com.github.mheerwaarden.eventdemo.util

@Suppress("UNUSED_PARAMETER")
fun logMessage(message: String): Unit = js("console.log(message)")

@Suppress("UNUSED_PARAMETER")
fun logWarning(message: String): Unit = js("console.warn(message)")

fun logError(message: String, error: Throwable) {
    val errorMessage = getExceptionMessage(message, error)
    val errorStack = try {
        error.stackTraceToString()
    } catch (e: Exception) {
        // stackTraceToString() itself could theoretically fail in rare/odd scenarios
        "Could not retrieve stack trace."
    }

    // Pass the strings to the js() block
    logErrorAndStackTrace(errorMessage, errorStack)
}

@Suppress("UNUSED_PARAMETER")
private fun logErrorAndStackTrace(message: String, errorStack: String): Unit =
    js("console.error(message, '\\nStack Trace:\\n' + errorStack)")
