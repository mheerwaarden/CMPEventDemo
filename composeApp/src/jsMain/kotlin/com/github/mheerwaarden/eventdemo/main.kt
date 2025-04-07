package com.github.mheerwaarden.eventdemo

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import com.github.mheerwaarden.eventdemo.di.initKoin
import kotlinx.browser.window
import org.jetbrains.skiko.wasm.onWasmReady
import org.koin.core.qualifier.named
import org.koin.dsl.module

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    // Make sure everything is loaded
    window.onload = {
        // Make sure skiko is ready
        onWasmReady {
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
                        single<AppContext> { JsAppContext() }
                        single {
                            {
                                println("Startup - Js")
                            }
                        }
                    }
                )
                println("Koin is initialised")

                CanvasBasedWindow(canvasElementId = "appTarget" /*title = document.title.ifBlank { "Event Demo App" } */) {
                    EventDemoApp(/*modifier = Modifier.fillMaxSize()*/)
                }

            } catch (e: Throwable) {
                println("An uncaught exception occurred: ${e::class.simpleName}")
                println("Message: ${e.message}")
                println("Stack Trace:")
                println(e.stackTraceToString())
            } finally {
                println("onload finished")
            }
        }
    }
}
