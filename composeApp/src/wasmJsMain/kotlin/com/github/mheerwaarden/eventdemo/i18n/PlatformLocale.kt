package com.github.mheerwaarden.eventdemo.i18n

import kotlinx.browser.window

/*
 * Actual implementation to get and set the current Web (Browser) system locale tag. For the web
 * platform, bypass the read-only restriction of the window.navigator.languages property to
 * introduce a custom locale logic, following the example from:
 * https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-resource-environment.html#locale.
 * - In index.html, a custom getter for the window.navigator.languages property is installed that
 *   returns window.__customLocale when set or the default value when not set.
 * - applyPlatformLocale sets the value of window.__customLocale. By setting null, the default
 *   locale will be used by the getter.
 * - getPlatformSystemLocaleTag returns the value of window.navigator.languages, which will be
 *   window.__customLocale when set or the default value when not set.
 */

/**
 * Actual implementation to get the current Web (Browser) system locale tag.
 */
actual fun getPlatformSystemLocaleTag(): String? {
    // Retrieve window.__customLocale using the custom handler installed in index.html
    val languages = window.navigator.languages
    // Testing languages against null includes testing against undefined
    if (languages != null && languages.length > 0) {
        // langs[0] here is a JsString because langs is JsArray<JsString>
        // Calling toString() on JsString converts it to Kotlin String
        // Standardize for browsers that might return an underscore as separator
        val language = languages[0].toString().replace('_', '-')
        println("Web: Current platform locale: $language")
        return language
    }
    println("Web: No current platform language, returning null")
    return null
}

/**
 * Actual implementation to apply the chosen locale tag on the Web.
 * By setting null, the default locale is used.
 *
 * @param localeTag The BCP 47 language tag that was chosen.
 */
actual fun applyPlatformLocale(localeTag: String?) {
    println("Web: Setting custom locale to: $localeTag")
    setCustomLocaleJs(localeTag)
}

// Functions defined by js() must be top-level
private fun setCustomLocaleJs(localeTag: String?): Unit = js(
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
