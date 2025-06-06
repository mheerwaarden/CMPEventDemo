package com.github.mheerwaarden.eventdemo

import com.github.mheerwaarden.eventdemo.di.platformLocalizationModule
import org.koin.core.context.startKoin
import org.koin.dsl.module

object KoinJsTestInitializer {
    private var koinStarted = false

    fun startKoinOnce() {
        if (!koinStarted) {
            setupCustomLocale()
            startKoin {
                modules(
                    module {
                        // define js dependencies here
                        single<PlatformAppInfo> {
                            PlatformAppInfo(
                                appId = "EventDemo",
                                versionName = "Test",
                                versionCode = "0.0.1"
                            )
                        }
                        single<AppContext> { JsAppContext() }
                    },
                    platformLocalizationModule,
                )
            }
            println("Koin started for JS tests")
            koinStarted = true
        }
    }
}

// Code from index.html
private fun setupCustomLocale() = js(
    """{
        var currentLanguagesImplementation = Object.getOwnPropertyDescriptor(Navigator.prototype, "languages");
        var newLanguagesImplementation = Object.assign({}, currentLanguagesImplementation, {
            get: function () {
                if (window.__customLocale) {
                    return [window.__customLocale];
                } else {
                    return currentLanguagesImplementation.get.apply(this);
                }
            }
        });

        Object.defineProperty(Navigator.prototype, "languages", newLanguagesImplementation)
    }"""
)