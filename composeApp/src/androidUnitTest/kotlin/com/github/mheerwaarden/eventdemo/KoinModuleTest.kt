package com.github.mheerwaarden.eventdemo

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import co.touchlab.kermit.Logger
import org.junit.runner.RunWith
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.verify.verify
import org.robolectric.annotation.Config
import kotlin.test.Test

@RunWith(AndroidJUnit4::class)
@Config(sdk = [32])
class KoinModuleTest : KoinTest {

    private val testModule = module {
        single<Context> { getApplicationContext<Application>() }
        single { get<Context>().getSharedPreferences("TEST", Context.MODE_PRIVATE) }
        single<AppInfo> { TestAppInfo }
        single { {} }
    }

    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun checkAllModules() {
        testModule.verify(extraTypes = listOf(Logger::class))
    }

}