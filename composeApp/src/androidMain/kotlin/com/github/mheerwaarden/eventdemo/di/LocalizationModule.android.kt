package com.github.mheerwaarden.eventdemo.di

import org.koin.core.module.Module

/**
 * Expected Koin module that provides platform-specific implementations for
 * [AppLocaleManager], [DateTimeFormatter], and [NumberFormatter].
 * The [LocaleProvider] will typically be the same instance as [AppLocaleManager].
 */
actual val platformLocalizationModule: Module
    get() = TODO("Not yet implemented")