/*
 * Copyright (c) 2025. Marcel Van Heerwaarden
 * @Author Marcel van Heerwaarden
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.github.mheerwaarden.eventdemo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun EventDemoAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is only available on Android 12+
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> eventDemoDarkColorScheme
        else -> eventDemoLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
