package com.github.mheerwaarden.eventdemo

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeViewport
import com.github.mheerwaarden.eventdemo.di.initKoin
import kotlinx.browser.document
import org.koin.core.qualifier.named
import org.koin.dsl.module

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    try {
        initKoin(
            module {
                // define desktop dependencies here
                single<PlatformAppInfo> {
                    PlatformAppInfo(
                        appId = get(named("appId")),
                        versionName = get(named("versionName")),
                        versionCode = get(named("versionCode"))
                    )
                }
                single<AppContext> { WasmJsAppContext }
                single {
                    {
                        println("Startup - WasmJs")
                    }
                }
            }
        )
        ComposeViewport(document.body!!) {
            EventDemoApp(modifier = Modifier.fillMaxSize())
        }
    } catch (e: Throwable) {
        println("An uncaught exception occurred: ${e::class.simpleName}")
        println("Message: ${e.message}")
        println("Stack Trace:")
        println(e.stackTraceToString())
    }

}
