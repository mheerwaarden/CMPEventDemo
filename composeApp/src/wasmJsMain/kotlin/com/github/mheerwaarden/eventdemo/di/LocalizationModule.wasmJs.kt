package com.github.mheerwaarden.eventdemo.di

import com.github.mheerwaarden.eventdemo.localization.DateTimeFormatter
import com.github.mheerwaarden.eventdemo.localization.NumberFormatter
import com.github.mheerwaarden.eventdemo.localization.PlatformLocaleManager
import com.github.mheerwaarden.eventdemo.localization.PlatformLocaleProvider
import com.github.mheerwaarden.eventdemo.localization.WasmJsDateTimeFormatter
import com.github.mheerwaarden.eventdemo.localization.WasmJsNumberFormatter
import com.github.mheerwaarden.eventdemo.localization.WasmJsPlatformLocaleManager
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformLocalizationModule: Module = module {
    // AppLocaleManager is also the LocaleProvider
    single<PlatformLocaleManager> { WasmJsPlatformLocaleManager() }
    single<PlatformLocaleProvider> { get<PlatformLocaleManager>() } // Use the same instance

    single<DateTimeFormatter> { WasmJsDateTimeFormatter() }
    single<NumberFormatter> { WasmJsNumberFormatter() }
}