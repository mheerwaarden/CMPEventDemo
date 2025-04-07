package com.github.mheerwaarden.eventdemo.di

import co.touchlab.kermit.Logger
import com.github.mheerwaarden.eventdemo.data.preferences.UserPreferencesRepository
import com.github.mheerwaarden.eventdemo.data.preferences.UserPreferencesSettingsRepository
import com.github.mheerwaarden.eventdemo.initLogger
import kotlinx.datetime.Clock
import org.koin.core.KoinApplication
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.parameter.parametersOf
import org.koin.core.scope.Scope
import org.koin.dsl.module


fun initKoin(appModule: Module, inJsModule: Module? = null): KoinApplication {
    val koinApplication = startKoin {
        modules(
            appModule, // Platform dependent, not in Kotlin
            inJsModule // Only in js for now
                ?: platformModule, // Platform dependent, in Kotlin
            coreModule // Platform independent
        )
    }

    // Dummy initialization logic, making use of appModule declarations for demonstration purposes.
    val koin = koinApplication.koin
    // doOnStartup is a lambda which is implemented in Swift on iOS side
    val doOnStartup = koin.get<() -> Unit>()
    doOnStartup.invoke()

    return koinApplication
}

expect val platformModule: Module

private val coreModule = module {
   factory { (tag: String?) -> get<Logger>().withTag(tag ?: "Event") }

    single<UserPreferencesRepository> { UserPreferencesSettingsRepository(get(), initLogger()) }

    single<Clock> { Clock.System }
}

internal inline fun <reified T> Scope.getWith(vararg params: Any?): T {
    return get(parameters = { parametersOf(*params) })
}

// Simple function to clean up the syntax a bit
fun KoinComponent.injectLogger(tag: String): Lazy<Logger> = inject { parametersOf(tag) }
