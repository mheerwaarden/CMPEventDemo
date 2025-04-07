package com.github.mheerwaarden.eventdemo

import kotlinx.browser.window

object WasmJsAppContext : AppContext {
    override val is24HourFormat: Boolean
        get() = is24HourFormatJs(window.navigator.language)

}

private fun is24HourFormatJs(locale: String): Boolean = js(
    """
    (locale) => {
        const options = { hour: 'numeric' };
        const fmt = new Intl.DateTimeFormat(locale, options);
        const resolved = fmt.resolvedOptions();
        return resolved.hourCycle === 'h23' || resolved.hourCycle === 'h24';
    }
    """
)
