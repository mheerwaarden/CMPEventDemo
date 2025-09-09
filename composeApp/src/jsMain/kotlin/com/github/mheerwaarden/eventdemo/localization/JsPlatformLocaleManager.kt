package com.github.mheerwaarden.eventdemo.localization

import kotlinx.browser.window

/**
 * Actual implementation to get and set the current Web (Browser) platform locale tag. For the web
 * platform, bypass the read-only restriction of the window.navigator.languages property to
 * introduce a custom locale logic, following the example from:
 * https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-resource-environment.html#locale.
 * - In index.html, a custom getter for the window.navigator.languages property is installed that
 *   returns window.__customLocale when set or the default value when not set.
 * - setPlatformLocale sets the value of window.__customLocale. By setting null, the default
 *   locale will be used by the getter.
 * - getPlatformSystemLocaleTag returns the value of window.navigator.languages, which will be
 *   window.__customLocale when set or the default value when not set.
 */
class JsPlatformLocaleManager : PlatformLocaleManager {

    private var mustLogCurrentLocale = true

    // Set window.__customLocale that effectively controls the navigator.languages property
    override fun setPlatformLocale(localeTag: String?) {
        val currentLocale = getPlatformLocaleTag()
        if (currentLocale != localeTag) {println("Web: Setting platform locale to '$localeTag'")
            setPlatformLocaleJs(localeTag)
            mustLogCurrentLocale = true
        }
    }

    // Get the navigator.languages property that will return window.__customLocale because of the
    // custom getter defined in index.html
    override fun getPlatformLocaleTag(): String? {
        // Retrieve window.__customLocale using the custom handler installed in index.html
        val languages = window.navigator.languages
        if (languages.isNotEmpty() && languages[0].isNotBlank()) {
            // languages[0] here is a JsString because languages is JsArray<JsString>
            // Calling toString() on JsString converts it to Kotlin String
            // Standardize for browsers that might return an underscore as separator
            val language = languages[0].replace('_', '-')
            if (mustLogCurrentLocale) {
                println("Web: Current platform locale: $language")
                mustLogCurrentLocale = false
            }
            return language
        }
        println("Web: No current platform language, returning null")
        return null
    }
}

// Functions defined by js() must be top-level
@Suppress("UNUSED_PARAMETER")
// Set window.__customLocale that effectively controls the navigator.languages property
private fun setPlatformLocaleJs(localeTag: String?) = js(
    """
    { 
        try {
            window.__customLocale = localeTag;
            console.log("Web JS: window.__customLocale is set to " + window.__customLocale);
        } catch (e) {
            console.error("Web JS: Error in setCustomLocaleJs setting " + localeTag + ": " + e.message, e);
        }
    }
    """
)
