package com.github.mheerwaarden.eventdemo.di

import com.github.mheerwaarden.eventdemo.module.DateTimeFormatter
import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSUserDefaults
import com.github.mheerwaarden.eventdemo.module.IOSDateTimeFormatter

actual val settingsModule: Module = module {
    single<NSUserDefaults> { NSUserDefaults.standardUserDefaults }
    single<Settings> { NSUserDefaultsSettings(get()) }
    single<DateTimeFormatter> { IOSDateTimeFormatter() }
}
