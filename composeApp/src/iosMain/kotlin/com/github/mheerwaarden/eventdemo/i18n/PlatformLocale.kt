package com.github.mheerwaarden.eventdemo.i18n

import platform.Foundation.NSLocale
import platform.Foundation.NSUserDefaults
import platform.Foundation.preferredLanguages

private const val LANG_KEY = "AppleLanguages"

actual fun getPlatformSystemLocaleTag(): String? {
    return NSLocale.preferredLanguages.firstOrNull() as? String?
}

actual fun applyPlatformLocale(localeTag: String?) {
    if (localeTag == null) {
        NSUserDefaults.standardUserDefaults.removeObjectForKey(LANG_KEY)
        println("iOS: Locale reset to default")
    } else {
        NSUserDefaults.standardUserDefaults.setObject(arrayListOf(localeTag), LANG_KEY)
        println("iOS: Locale set to: $localeTag.")
    }
}
