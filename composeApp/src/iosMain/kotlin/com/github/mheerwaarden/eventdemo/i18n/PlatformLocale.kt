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
 * For iOS, directly changing the entire app's locale programmatically without
 * a restart or specific setup (like environment variables for launch) is complex.
 * This function primarily acknowledges the preference has changed.
 * The UI (Compose) will update due to effectiveAppLocale changing.
 * Further native changes might require restarting the app or more advanced techniques.
 *
 * @param localeTag The BCP 47 language tag that was chosen.
 */
actual fun applyPlatformLocale(localeTag: String?) {
    // On iOS, there's no simple, direct equivalent to AppCompatDelegate.setApplicationLocales()
    // for an already running app to change its entire locale context for native components.
    // The primary effect of changing the preference will be that Compose Multiplatform Resources
    // will pick up the new locale via LocalAppLocale.
    // For a "full" native locale switch, users typically change it in system settings for the app,
    // or the app might be programmatically restarted with specific launch arguments (advanced).

    // You could store the `localeTag` in NSUserDefaults here if you need to inform native
    // Swift/Objective-C code that the preference has changed, so it can adapt if possible
    // (e.g., by manually reloading resources or re-initializing components).
    // Example:
    // NSUserDefaults.standardUserDefaults.setObject(localeTag, forKey = "app_preferred_language")
    // NSUserDefaults.standardUserDefaults.synchronize()

    if (localeTag == null) {
        NSUserDefaults.standardUserDefaults.removeObjectForKey(LANG_KEY)
        println("iOS: Locale reset to default")
    } else {
        NSUserDefaults.standardUserDefaults.setObject(arrayListOf(localeTag), LANG_KEY)
        println("iOS: Locale set to: $localeTag.")
    }
}
