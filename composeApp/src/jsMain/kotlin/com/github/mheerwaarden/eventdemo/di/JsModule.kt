package com.github.mheerwaarden.eventdemo.di

import co.touchlab.kermit.Logger
import com.github.mheerwaarden.eventdemo.data.preferences.UserPreferencesSettingsRepository
import com.russhwolf.settings.Settings
import com.russhwolf.settings.StorageSettings
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<Settings> { StorageSettings() }
    single { UserPreferencesSettingsRepository(get(), Logger) }
}
