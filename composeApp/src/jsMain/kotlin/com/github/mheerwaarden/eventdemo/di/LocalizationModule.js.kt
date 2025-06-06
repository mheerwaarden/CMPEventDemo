package com.github.mheerwaarden.eventdemo.di

import org.koin.core.module.Module
import org.koin.dsl.module
import com.github.mheerwaarden.eventdemo.localization.PlatformLocaleManager
import com.github.mheerwaarden.eventdemo.localization.DateTimeFormatter
import com.github.mheerwaarden.eventdemo.localization.JsPlatformLocaleManager
import com.github.mheerwaarden.eventdemo.localization.JsDateTimeFormatter
import com.github.mheerwaarden.eventdemo.localization.JsNumberFormatter
import com.github.mheerwaarden.eventdemo.localization.PlatformLocaleProvider
import com.github.mheerwaarden.eventdemo.localization.NumberFormatter

/**
 * Expected Koin module that provides platform-specific implementations for
 * [PlatformLocaleManager], [DateTimeFormatter], and [NumberFormatter].
 * The [PlatformLocaleProvider] will typically be the same instance as [PlatformLocaleManager].
 */
actual val platformLocalizationModule: Module = module {
    // AppLocaleManager is also the LocaleProvider
    single<PlatformLocaleManager> { JsPlatformLocaleManager() }
    single<PlatformLocaleProvider> { get<PlatformLocaleManager>() } // Use the same instance

    single<DateTimeFormatter> { JsDateTimeFormatter() }
    single<NumberFormatter> { JsNumberFormatter() }
}