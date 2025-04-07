/*
 * Copyright (c) 2025. Marcel Van Heerwaarden
 * @Author Marcel van Heerwaarden
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.github.mheerwaarden.eventdemo.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import com.github.mheerwaarden.eventdemo.Dimensions
import com.github.mheerwaarden.eventdemo.ui.theme.EventDemoDarkPalette
import com.github.mheerwaarden.eventdemo.ui.theme.EventDemoLightPalette

enum class HomeButtonColorType {
    PRIMARY,
    SECONDARY,
    TERTIARY,
    QUATERNARY,
}

@Composable
fun HomeButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colorType: HomeButtonColorType = HomeButtonColorType.PRIMARY
) {
    Button(
        onClick = onClick,
        colors = getButtonColor(colorType),
        shape = RectangleShape,
        modifier = modifier.sizeIn(minHeight = Dimensions.home_button_height)
    ) {
        Text(text)
    }
}

@Composable
fun getButtonColor(
    colorType: HomeButtonColorType,
    darkTheme: Boolean = isSystemInDarkTheme()
): ButtonColors = when (colorType) {
    HomeButtonColorType.PRIMARY -> ButtonDefaults.buttonColors(
        containerColor = if (darkTheme) EventDemoDarkPalette.Primary80 else EventDemoLightPalette.Primary80,
        contentColor = if (darkTheme) EventDemoDarkPalette.Primary20 else EventDemoLightPalette.Primary20,
        disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        disabledContentColor = MaterialTheme.colorScheme.surfaceContainer,
    )

    HomeButtonColorType.SECONDARY -> ButtonDefaults.buttonColors(
        containerColor = if (darkTheme) EventDemoDarkPalette.Primary60 else EventDemoLightPalette.Primary60,
        contentColor = if (darkTheme) EventDemoDarkPalette.Primary20 else EventDemoLightPalette.Primary20,
        disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        disabledContentColor = MaterialTheme.colorScheme.surfaceContainer,
    )

    HomeButtonColorType.TERTIARY -> ButtonDefaults.buttonColors(
        containerColor = if (darkTheme) EventDemoDarkPalette.Primary35 else EventDemoLightPalette.Primary35,
        contentColor = if (darkTheme) EventDemoDarkPalette.Primary90 else EventDemoLightPalette.Primary90,
        disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        disabledContentColor = MaterialTheme.colorScheme.surfaceContainer,
    )

    HomeButtonColorType.QUATERNARY -> ButtonDefaults.buttonColors(
        containerColor = if (darkTheme) EventDemoDarkPalette.Primary20 else EventDemoLightPalette.Primary20,
        contentColor = if (darkTheme) EventDemoDarkPalette.Primary80 else EventDemoLightPalette.Primary80,
        disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        disabledContentColor = MaterialTheme.colorScheme.surfaceContainer,
    )
}

