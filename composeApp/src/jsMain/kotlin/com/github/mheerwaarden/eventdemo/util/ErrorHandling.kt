package com.github.mheerwaarden.eventdemo.util

/**
 * Constructs a standardized error message string from an exception object.
 *
 * This function attempts to extract the most relevant information from a JavaScript
 * error object (`dynamic` type in Kotlin/JS). It prioritizes the `name` and `message`
 * properties, which are standard for JavaScript `Error` objects.
 *
 * If `name` and `message` are not available or are unhelpful, it falls back to
 * `toString()`. If `toString()` is also generic (like "[object Object]"),
 * it logs the raw error object to the console for debugging purposes and returns
 * a generic error message.
 *
 * The final message is prepended with the provided `reason`.
 *
 * @param reason A string describing the context or operation that led to the error.
 * @param e The exception object (typically a JavaScript Error object).
 * @return A formatted error message string in the format "reason: exceptionDetails".
 */
fun getExceptionMessage(reason: String, e: dynamic): String {
    val message = e.message as? String
    val name = e.name as? String // Standard for JS Error objects

    val exceptionMessage = when {
        name != null && !message.isNullOrBlank() -> "$name: $message"
        name != null && message.isNullOrBlank() -> name // If message is blank/null but name exists
        !message.isNullOrBlank() -> message // If name is missing but message exists
        else -> {
            // Last resort: try toString(). It's unlikely to be null, but good to handle.
            val toStringValue = e.toString() as? String
            if (!toStringValue.isNullOrBlank() && toStringValue != "[object Object]") {
                // Use toString if it's somewhat descriptive
                toStringValue
            } else {
                // If toString() is also unhelpful or just "[object Object], log the raw object for dev debugging
                console.error("Raw error object for reason '$reason':", e)
                // Fallback to a generic message
                "An unexpected error occurred."
            }
        }
    }
    return "$reason: $exceptionMessage"
}