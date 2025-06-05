package com.github.mheerwaarden.eventdemo.di

import com.russhwolf.settings.Settings
import com.russhwolf.settings.StorageSettings
import org.koin.core.module.Module
import org.koin.dsl.module

actual val settingsModule: Module = module {
    single<Settings> { StorageSettings() }
}
