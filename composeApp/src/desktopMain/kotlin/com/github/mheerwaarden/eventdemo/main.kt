package com.github.mheerwaarden.eventdemo

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.github.mheerwaarden.eventdemo.di.initKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale

fun main() = application {
    initKoin(module {
        // define desktop dependencies here
        single<PlatformAppInfo> {
            PlatformAppInfo(
                appId = get(named("appId")),
                versionName = get(named("versionName")),
                versionCode = get(named("versionCode"))
            )
        }
        single<AppContext> {
            object : AppContext {
                override val is24HourFormat: Boolean
                    get() {
                        val dateFormat: DateFormat = SimpleDateFormat.getTimeInstance(
                            SimpleDateFormat.SHORT, Locale.getDefault()
                        )
                        return dateFormat is SimpleDateFormat && dateFormat.toPattern()
                            .contains("H")
                    }
            }
        }
        single {
            {
                println("Startup - Desktop/Kotlin")
            }
        }

    })
    Window(
        onCloseRequest = ::exitApplication,
        title = "Event Demo",
    ) {
        EventDemoApp(modifier = Modifier.fillMaxSize())
    }

}
