/*
 * Copyright (c) 2025. Marcel Van Heerwaarden
 * @Author Marcel van Heerwaarden
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.github.mheerwaarden.eventdemo.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.TimePickerLayoutType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.github.mheerwaarden.eventdemo.localization.DateTimeFormatter
import com.github.mheerwaarden.eventdemo.localization.toLocalizedString
import com.github.mheerwaarden.eventdemo.resources.Res
import com.github.mheerwaarden.eventdemo.resources.select_time
import com.github.mheerwaarden.eventdemo.resources.show_time_picker
import com.github.mheerwaarden.eventdemo.util.format
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource


data class TimeFieldPreferences(
    val is24Hour: Boolean = false,
    val isUseKeyboard: Boolean = false,
    val onToggleKeyboard: (Boolean) -> Unit = { _ -> },
    val isHorizontalLayout: Boolean = false,
) {
    constructor(
        dateTimeFormatter: DateTimeFormatter,
        isUseKeyboard: Boolean = false,
        onToggleKeyboard: (Boolean) -> Unit = { _ -> },
        isHorizontalLayout: Boolean = false,
    ) : this(
        is24Hour = dateTimeFormatter.is24HourFormat(),
        isUseKeyboard = isUseKeyboard,
        onToggleKeyboard = onToggleKeyboard,
        isHorizontalLayout = isHorizontalLayout
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeField(
    currentTime: LocalTime,
    onTimeChange: (LocalTime) -> Unit,
    modifier: Modifier = Modifier,
    preferences: TimeFieldPreferences = TimeFieldPreferences(),
    labelId: StringResource = Res.string.select_time
) {
    // String value of the date
    var time by rememberSaveable { mutableStateOf("") }
    time = currentTime.toLocalizedString()
    DialogField(
        label = stringResource(labelId),
        value = time,
        modifier = modifier.fillMaxWidth(),
        isRequired = true,
        trailingIcon = {
            Icon(
                imageVector = Icons.Filled.Schedule,
                contentDescription = stringResource(Res.string.show_time_picker),
            )
        },
        onShowDialog = { onClose ->
            ShowTimePickerDialog(
                currentTime = currentTime,
                is24Hour = preferences.is24Hour,
                isUseKeyboard = preferences.isUseKeyboard,
                onSetTime = { hour, minute ->
                    val localTime = LocalTime(hour, minute)
                    time = localTime.format()
                    onTimeChange(localTime)
                    onClose()
                },
                onToggleKeyboard = preferences.onToggleKeyboard,
                onDismiss = { onClose() },
                layoutType = if (preferences.isHorizontalLayout) TimePickerLayoutType.Horizontal else TimePickerLayoutType.Vertical
            )
        }
    )
}
