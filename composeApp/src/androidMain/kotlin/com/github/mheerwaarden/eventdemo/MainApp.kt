package com.github.mheerwaarden.eventdemo

import android.app.Application
import android.content.Context
import android.util.Log
import com.github.mheerwaarden.eventdemo.di.AndroidAppContext
import com.github.mheerwaarden.eventdemo.di.initKoin
import org.koin.dsl.module

class MainApp : Application() {

    override fun onCreate() {
        super.onCreate()
        initKoin(
            module {
                single<Context> { this@MainApp }
                single<AppInfo> { AndroidAppInfo }
                single<AppContext> { AndroidAppContext(this@MainApp) }
                single {
                    { Log.i("Startup", "Startup Android/Kotlin") }
                }
            }
        )
    }
}

/* Note: com.github.mheerwaarden.eventdemo.BuildConfig is a generated class */
object AndroidAppInfo : AppInfo {
    override val appId: String = BuildConfig.APPLICATION_ID
    override val versionName: String = BuildConfig.VERSION_NAME
    override val versionCode: String = BuildConfig.VERSION_CODE.toString()
}

