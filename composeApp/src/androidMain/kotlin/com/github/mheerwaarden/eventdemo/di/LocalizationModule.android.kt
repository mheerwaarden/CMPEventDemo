package com.github.mheerwaarden.eventdemo.di

import com.github.mheerwaarden.eventdemo.localization.AndroidDateTimeFormatter
import com.github.mheerwaarden.eventdemo.localization.AndroidNumberFormatter
import com.github.mheerwaarden.eventdemo.localization.AndroidPlatformLocaleManager
import com.github.mheerwaarden.eventdemo.localization.DateTimeFormatter
import com.github.mheerwaarden.eventdemo.localization.NumberFormatter
import com.github.mheerwaarden.eventdemo.localization.PlatformLocaleManager
import com.github.mheerwaarden.eventdemo.localization.PlatformLocaleProvider
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Expected Koin module that provides platform-specific implementations for
 * [PlatformLocaleManager], [DateTimeFormatter], and [NumberFormatter].
 * The [PlatformLocaleProvider] will typically be the same instance as [PlatformLocaleManager].
 */
actual val platformLocalizationModule: Module = module {
    // AppLocaleManager is also the LocaleProvider
    single<PlatformLocaleManager> { AndroidPlatformLocaleManager() }
    single<PlatformLocaleProvider> { get<PlatformLocaleManager>() } // Use the same instance

    single<DateTimeFormatter> { AndroidDateTimeFormatter() }
    single<NumberFormatter> { AndroidNumberFormatter() }
}