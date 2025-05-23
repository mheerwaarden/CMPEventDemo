package com.github.mheerwaarden.eventdemo

import kotlinx.browser.window

object WasmJsAppContext : AppContext {
    override val is24HourFormat: Boolean
        get() = is24HourFormatJs(window.navigator.language)

}

private fun is24HourFormatJs(locale: String): Boolean = js(
    """
    {
        const options = { hour: 'numeric' };
        const fmt = new Intl.DateTimeFormat(locale, options);
        const resolved = fmt.resolvedOptions();
        console.log("WasmJsAppContext.is24HourFormat: " + resolved);
        return resolved.hourCycle === 'h23' || resolved.hourCycle === 'h24';
    }
    """
)
