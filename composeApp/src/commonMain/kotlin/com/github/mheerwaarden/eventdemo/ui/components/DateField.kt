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
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.github.mheerwaarden.eventdemo.localization.toLocalizedString
import com.github.mheerwaarden.eventdemo.resources.Res
import com.github.mheerwaarden.eventdemo.resources.cancel
import com.github.mheerwaarden.eventdemo.resources.ok
import com.github.mheerwaarden.eventdemo.resources.select_date
import com.github.mheerwaarden.eventdemo.resources.show_date_picker
import com.github.mheerwaarden.eventdemo.util.format
import com.github.mheerwaarden.eventdemo.util.ofEpochMilli
import com.github.mheerwaarden.eventdemo.util.toInstant
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

data class DateFieldPreferences(
    val isUseKeyboard: Boolean = false,
    val onToggleKeyboard: (Boolean) -> Unit = { _ -> },
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateField(
    currentDate: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    preferences: DateFieldPreferences = DateFieldPreferences(),
    labelId: StringResource = Res.string.select_date
) {
    // String value of the date
    var date by rememberSaveable { mutableStateOf("") }
    date = currentDate.toLocalizedString()

    DialogField(
        label = stringResource(labelId),
        value = date,
        modifier = modifier.fillMaxWidth(),
        isRequired = true,
        trailingIcon = {
            Icon(
                Icons.Filled.EditCalendar,
                contentDescription = stringResource(Res.string.show_date_picker),
            )
        },
        onShowDialog = { onClose ->
            // State for managing date picker
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = currentDate.toInstant().toEpochMilliseconds(),
                initialDisplayMode = if (preferences.isUseKeyboard) DisplayMode.Input else DisplayMode.Picker
            )
            DatePickerDialog(
                onDismissRequest = { closeDialog(preferences, datePickerState, onClose) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val selectedUtcMillis = datePickerState.selectedDateMillis
                            if (selectedUtcMillis != null) {
                                val selectedDate = ofEpochMilli(selectedUtcMillis).date
                                date = selectedDate.format()
                                onDateChange(selectedDate)
                            }
                            closeDialog(preferences, datePickerState, onClose)
                        }
                    ) {
                        Text(stringResource(Res.string.ok))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { closeDialog(preferences, datePickerState, onClose) }
                    ) {
                        Text(stringResource(Res.string.cancel))
                    }
                },
            ) { DatePicker(state = datePickerState) }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
private fun closeDialog(
    preferences: DateFieldPreferences,
    datePickerState: DatePickerState,
    close: () -> Unit,
) {
    preferences.onToggleKeyboard(datePickerState.displayMode == DisplayMode.Input)
    close()
}