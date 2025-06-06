package com.github.mheerwaarden.eventdemo.di


import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.github.mheerwaarden.eventdemo.module.DateTimeFormatter
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module
import com.github.mheerwaarden.eventdemo.module.AndroidDateTimeFormatter

actual val settingsModule: Module = module {
    single<SharedPreferences> { PreferenceManager.getDefaultSharedPreferences(androidContext()) }
    single<Settings> { SharedPreferencesSettings(get()) }
    single<DateTimeFormatter> { AndroidDateTimeFormatter() }
}