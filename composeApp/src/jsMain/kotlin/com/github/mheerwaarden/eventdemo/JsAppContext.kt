package com.github.mheerwaarden.eventdemo

import com.github.mheerwaarden.eventdemo.module.JsDateTimeFormatter

class JsAppContext : AppContext {
    override val is24HourFormat: Boolean // true // by lazy { is24HourFormatJs() }
        get() = JsDateTimeFormatter().is24HourFormat() // is24HourFormatJs(window.navigator.language)

}
