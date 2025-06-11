package com.github.mheerwaarden.eventdemo.di

import com.russhwolf.settings.Settings
import com.russhwolf.settings.StorageSettings
import kotlinx.browser.window
import org.koin.core.module.Module
import org.koin.dsl.module
import org.w3c.dom.Storage

actual val settingsModule: Module = module {
    single<Storage> { window.localStorage }
    single<Settings> { StorageSettings(get()) }
}
