package com.github.mheerwaarden.eventdemo

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.github.mheerwaarden.eventdemo.di.initKoin
import com.github.mheerwaarden.eventdemo.resources.Res
import com.github.mheerwaarden.eventdemo.resources.calendar
import org.jetbrains.compose.resources.painterResource
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun main() = application {
    // Although running earlier, I now get:
    // Exception in thread "main" java.lang.UnsatisfiedLinkError: 'void org.jetbrains.skiko.OpenGLApi.glFlush()'
    //	at org.jetbrains.skiko.OpenGLApi.glFlush(Native Method)
    //  ...
    // Updating OpenGL libs on Ubuntu did not help.
    // Fall back to software rendering, bypass OpenGL completely
    System.setProperty("skiko.renderApi", "SOFTWARE_COMPAT")

    initKoin(module {
        // define desktop dependencies here
        single<PlatformAppInfo> {
            /*
            This does not work: Exception in thread "AWT-EventQueue-0"
            org.koin.core.error.NoDefinitionFoundException: No definition found for type
            'com.github.mheerwaarden.eventdemo.AppInfo'. Check your Modules configuration and add
            missing type and/or qualifier!
             */
            PlatformAppInfo(
                appId = get(named("appId")),
                versionName = get(named("versionName")),
                versionCode = get(named("versionCode"))
            )
        }
        single {
            { println("Startup - Desktop/Kotlin") }
        }
    })

    val windowState = rememberWindowState(
        width = Dimensions.max_compact_width,
        height = Dimensions.max_compact_height
    )
    Window(
        onCloseRequest = ::exitApplication,
        state = windowState,
        title = "Event Demo",
        icon = painterResource(Res.drawable.calendar)
    ) {
        EventDemoApp(
            modifier = Modifier.fillMaxSize(),
        )
    }

}
