package com.github.mheerwaarden.eventdemo.i18n

import platform.Foundation.NSLocale
import platform.Foundation.NSUserDefaults
import platform.Foundation.preferredLanguages

private const val LANG_KEY = "AppleLanguages"

/**
 * Actual implementation to get the current iOS system locale tag.
 */
actual fun getPlatformSystemLocaleTag(): String? {
    return NSLocale.preferredLanguages.firstOrNull() as? String?
}

/**
 * Actual implementation to apply the chosen locale tag on iOS.
 *
 * @param localeTag The BCP 47 language tag that was chosen.
 */
actual fun applyPlatformLocale(localeTag: String?) {
    if (localeTag == null) {
        NSUserDefaults.standardUserDefaults.removeObjectForKey(LANG_KEY)
        println("iOS: Locale reset to default")
    } else {
        NSUserDefaults.standardUserDefaults.setObject(arrayListOf(localeTag), LANG_KEY)
        println("iOS: Locale set to: $localeTag.")
    }
}
