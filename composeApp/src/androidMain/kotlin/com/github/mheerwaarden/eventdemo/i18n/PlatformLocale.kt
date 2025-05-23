package com.github.mheerwaarden.eventdemo.i18n

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

private var defaultLocale = Locale.getDefault()

/**
 * Actual implementation to get the current Android system locale tag.
 */
actual fun getPlatformSystemLocaleTag(): String? {
    return Locale.getDefault().toLanguageTag()
}

/**
 * Actual implementation to apply the chosen locale tag on Android.
 * This uses AppCompatDelegate to change the application's locale.
 *
 * @param localeTag The BCP 47 language tag to apply. If null, it reverts to system default.
 */
actual fun applyPlatformLocale(localeTag: String?) {
    try {
        if (localeTag == null) {
            println("Android: Reverting to system locale.")
            Locale.setDefault(defaultLocale)
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
        } else {
            val localeList = LocaleListCompat.forLanguageTags(localeTag)
            if (localeList.isEmpty) {
                // If localeTag is invalid, just to reflect the system's current value.
                println("Android: Not changing system locale.")
            } else {
                val javaLocale = Locale.forLanguageTag(localeTag)
                Locale.setDefault(javaLocale)
                AppCompatDelegate.setApplicationLocales(localeList)
                println("Android: Successfully set $javaLocale")
            }
        }
    } catch (e: Exception) {
        // Log error or handle invalid tag, do not change the current locale
        println("Invalid localeTag: $localeTag, not changing system locale. Error: ${e.message}")
    }
}
