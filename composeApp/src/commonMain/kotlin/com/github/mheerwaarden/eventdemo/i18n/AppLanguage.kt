package com.github.mheerwaarden.eventdemo.i18n

import com.github.mheerwaarden.eventdemo.data.preferences.DEFAULT_LOCALE_FROM_PLATFORM
import com.github.mheerwaarden.eventdemo.resources.Res
import com.github.mheerwaarden.eventdemo.resources.en
import com.github.mheerwaarden.eventdemo.resources.nl
import com.github.mheerwaarden.eventdemo.resources.system
import org.jetbrains.compose.resources.StringResource

enum class AppLanguage(
    val code: String,
    val stringRes: StringResource
) {
    System(DEFAULT_LOCALE_FROM_PLATFORM, Res.string.system),
    English("en", Res.string.en),
    Dutch("nl", Res.string.nl)
}
