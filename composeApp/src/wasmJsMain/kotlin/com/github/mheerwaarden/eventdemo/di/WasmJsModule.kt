package com.github.mheerwaarden.eventdemo.di

import com.github.mheerwaarden.eventdemo.data.preferences.UserPreferencesSettingsRepository
import com.russhwolf.settings.Settings
import com.russhwolf.settings.StorageSettings
import org.koin.core.module.Module
import org.koin.dsl.module
import org.w3c.dom.Storage
import org.w3c.dom.Window
import co.touchlab.kermit.Logger

actual val platformModule: Module = module {
    single<Storage> { getLocalStorage() }
    single<Settings> { StorageSettings(get()) }
    single { UserPreferencesSettingsRepository(get(), Logger) }
}

private val jsWindow: Window = js("window")

fun getLocalStorage(): Storage = jsWindow.localStorage
