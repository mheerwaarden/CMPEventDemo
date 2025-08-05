package com.github.mheerwaarden.eventdemo

import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.github.mheerwaarden.eventdemo.data.preferences.UserPreferencesRepository
import org.koin.compose.koinInject
import org.koin.core.component.KoinComponent

class MainActivity : ComponentActivity(), KoinComponent {

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Make sure that detection for unclosed resources shows a nice stack trace
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder(StrictMode.getVmPolicy())
                .detectLeakedClosableObjects()
                .penaltyLog() // Keep logging
                .penaltyDeath() // Crash on violation -- not recommended, debugging only
                .build()
        )

        setContent {
            Log.d("LocaleCheck", "Compose Context Locale: ${LocalContext.current.resources.configuration.locales[0].toLanguageTag()}")
            val userPreferencesRepository: UserPreferencesRepository = koinInject()
            Log.d("LocaleCheck", "UserPreferencesRepository preferences = ${userPreferencesRepository.preferences}")
            EventDemoApp(
                isHorizontalLayout = calculateWindowSizeClass(this).widthSizeClass != WindowWidthSizeClass.Compact,
                modifier = Modifier.fillMaxSize()
            )
        }
    }

}
