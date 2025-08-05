package com.github.mheerwaarden.eventdemo.di

import co.touchlab.kermit.Logger
import com.github.mheerwaarden.eventdemo.data.preferences.UserPreferencesRepository
import com.github.mheerwaarden.eventdemo.data.preferences.UserPreferencesSettingsRepository
import com.github.mheerwaarden.eventdemo.ui.localization.LocaleViewModel
import com.github.mheerwaarden.eventdemo.initLogger
import com.github.mheerwaarden.eventdemo.localization.DateTimeFormatter
import com.github.mheerwaarden.eventdemo.localization.NumberFormatter
import com.github.mheerwaarden.eventdemo.localization.PlatformLocaleManager
import com.github.mheerwaarden.eventdemo.localization.PlatformLocaleProvider
import com.github.mheerwaarden.eventdemo.ui.screen.settings.SettingsViewModel
import com.russhwolf.settings.Settings
import kotlinx.datetime.Clock
import org.koin.core.KoinApplication
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.parameter.parametersOf
import org.koin.core.scope.Scope
import org.koin.dsl.module

fun initKoin(platformModule: Module): KoinApplication {
    println("Starting Koin...")
    val koinApplication = startKoin {
        modules(
            // Platform info
            // Platform dependent, not in Kotlin
            platformModule,
            // Localization info: locale handling; date, time and number formatting
            // Platform dependent, partly in Kotlin
            platformLocalizationModule,
            // Settings storage
            // Platform dependent, in Kotlin
            settingsModule,
            // Common core modules
            // Platform independent, in Kotlin
            coreModule
        )
    }

    // Dummy initialization logic, making use of appModule declarations for demonstration purposes.
    val koin = koinApplication.koin
    // doOnStartup is a lambda which is implemented in Swift on iOS side
    val doOnStartup = koin.get<() -> Unit>()
    doOnStartup.invoke()

    println("Koin initialized")
    return koinApplication
}

/**
 * Expected Koin module that provides platform-specific implementations for
 * [PlatformLocaleManager], [DateTimeFormatter], and [NumberFormatter].
 * The [PlatformLocaleProvider] will typically be the same instance as [PlatformLocaleManager].
 */
expect val platformLocalizationModule: Module

/**
 * Expected Koin module that provides platform-specific implementation for the [Settings] storage.
 */
expect val settingsModule: Module

private val coreModule = module {
    factory { (tag: String?) -> get<Logger>().withTag(tag ?: "Event") }

    single<UserPreferencesRepository> { UserPreferencesSettingsRepository(get(), initLogger()) }
    single { LocaleViewModel(userPreferencesRepository = get()) }
    single { SettingsViewModel(userPreferencesRepository = get()) }

    single<Clock> { Clock.System }
}

internal inline fun <reified T> Scope.getWith(vararg params: Any?): T {
    return get(parameters = { parametersOf(*params) })
}

// Simple function to clean up the syntax a bit
fun KoinComponent.injectLogger(tag: String): Lazy<Logger> = inject { parametersOf(tag) }
