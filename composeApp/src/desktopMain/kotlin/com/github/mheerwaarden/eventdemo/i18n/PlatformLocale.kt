package com.github.mheerwaarden.eventdemo.i18n

import java.util.Locale

private var defaultLocale = Locale.getDefault()

/**
 * Actual implementation to get the current Desktop (JVM) system locale tag.
 */
actual fun getPlatformSystemLocaleTag(): String? {
    return Locale.getDefault().toLanguageTag()
}

/**
 * Actual implementation to apply the chosen locale tag on Desktop.
 * This attempts to set the JVM's default locale.
 *
 * @param localeTag The BCP 47 language tag to apply. If null, it implies using the system default.
 */
actual fun applyPlatformLocale(localeTag: String?) {
    try {
        val newLocale = if (localeTag == null) {
            println("Desktop: Reverting to system locale.")
            defaultLocale
        } else {
            Locale.forLanguageTag(localeTag)
        }

        if (newLocale == null) {
            // If localeTag is invalid, just to reflect the system's current value.
            println("Desktop: Not changing system locale.")
        } else {
            Locale.setDefault(newLocale)
            println("Desktop: JVM default locale set to: ${newLocale.toLanguageTag()}")
        }
    } catch (e: Exception) {
        println("Error setting JVM default locale, not changing system locale. Error: ${e.message}")
    }
}
