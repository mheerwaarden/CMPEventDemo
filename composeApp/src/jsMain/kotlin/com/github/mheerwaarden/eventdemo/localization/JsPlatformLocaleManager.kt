package com.github.mheerwaarden.eventdemo.localization

import kotlinx.browser.window

class JsPlatformLocaleManager : PlatformLocaleManager {

    override fun setAppLocale(localeTag: String?) {
        println("JsAppLocaleManager: Setting app locale to '$localeTag'")
        setCustomLocaleJs(localeTag)
    }

    override fun getCurrentLocaleTag(): String? {
        // Retrieve window.__customLocale using the custom handler installed in index.html
        val languages = window.navigator.languages
        // Testing languages against null includes testing against undefined
        if (languages != null && languages.isNotEmpty()) {
            // languages[0] here is a JsString because languages is JsArray<JsString>
            // Calling toString() on JsString converts it to Kotlin String
            // Standardize for browsers that might return an underscore as separator
            val language = languages[0].toString().replace('_', '-')
            println("Web: Current platform locale: $language")
            return language
        }
        println("Web: No current platform language, returning null")
        return null
    }
}

// Functions defined by js() must be top-level
@Suppress("UNUSED_PARAMETER")
private fun setCustomLocaleJs(localeTag: String?) = js(
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
