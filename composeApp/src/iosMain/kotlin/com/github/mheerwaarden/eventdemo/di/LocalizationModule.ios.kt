package com.github.mheerwaarden.eventdemo.di

import com.github.mheerwaarden.eventdemo.localization.DateTimeFormatter
import com.github.mheerwaarden.eventdemo.localization.IOSDateTimeFormatter
import com.github.mheerwaarden.eventdemo.localization.IOSNumberFormatter
import com.github.mheerwaarden.eventdemo.localization.IOSPlatformLocaleManager
import com.github.mheerwaarden.eventdemo.localization.NumberFormatter
import com.github.mheerwaarden.eventdemo.localization.PlatformLocaleManager
import com.github.mheerwaarden.eventdemo.localization.PlatformLocaleProvider
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformLocalizationModule: Module = module {
    // AppLocaleManager is also the LocaleProvider
    single<PlatformLocaleManager> { IOSPlatformLocaleManager() }
    single<PlatformLocaleProvider> { get<PlatformLocaleManager>() } // Use the same instance

    single<DateTimeFormatter> { IOSDateTimeFormatter() }
    single<NumberFormatter> { IOSNumberFormatter() }
}