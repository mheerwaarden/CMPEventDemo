package com.github.mheerwaarden.eventdemo.di

import co.touchlab.kermit.Logger
import com.github.mheerwaarden.eventdemo.AppContext
import com.github.mheerwaarden.eventdemo.AppInfo
import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.NSUserDefaults
import platform.Foundation.currentLocale


// Called from Koin.swift
fun initKoinIos(
    userDefaults: NSUserDefaults,
    appInfo: AppInfo,
    doOnStartup: () -> Unit
): KoinApplication = initKoin(
    module {
        single<Settings> { NSUserDefaultsSettings(userDefaults) }
        single { appInfo }
        single<AppContext> {
            object : AppContext {
                override val is24HourFormat: Boolean
                    get() {
                        val dateFormat = NSDateFormatter()
                        dateFormat.locale = NSLocale.currentLocale
                        dateFormat.dateFormat = "j"
                        val dateFormatString = dateFormat.stringFromDate(platform.Foundation.NSDate())
                        return dateFormatString.contains("24")
                    }
            }
        }
        single { doOnStartup }
    }
)



// Access from Swift to create a logger
@Suppress("unused")
fun Koin.loggerWithTag(tag: String) = get<Logger>(qualifier = null) { parametersOf(tag) }
