/*
 * Copyright (c) 2025. Marcel Van Heerwaarden
 * @Author Marcel van Heerwaarden
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.github.mheerwaarden.eventdemo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TimePickerLayoutType
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.github.mheerwaarden.eventdemo.resources.Res
import com.github.mheerwaarden.eventdemo.resources.cancel
import com.github.mheerwaarden.eventdemo.resources.clock
import com.github.mheerwaarden.eventdemo.resources.keyboard
import com.github.mheerwaarden.eventdemo.resources.ok
import com.github.mheerwaarden.eventdemo.resources.select_time
import com.github.mheerwaarden.eventdemo.ui.theme.EventDemoAppTheme
import com.github.mheerwaarden.eventdemo.util.now
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Show Material3 TimePicker in a Dialog
 *
 * The dialog is visible as long as it is part of the composition hierarchy. In order to let the
 * user dismiss the Dialog, the implementation of onDismissRequest should contain a way to remove
 * the dialog from the composition hierarchy.
 *
 * Parameter layoutType should be set according to the orientation of the device
 *
 * onTimeSet is only called when the OK button is clicked, followed by the onDismiss.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowTimePickerDialog(
    modifier: Modifier = Modifier,
    currentTime: LocalTime = now().time,
    is24Hour: Boolean = true,
    isUseKeyboard: Boolean = false,
    onSetTime: (hour: Int, minute: Int) -> Unit = { _, _ -> },
    onToggleKeyboard: (Boolean) -> Unit = { _ -> },
    onDismiss: () -> Unit = {},
    layoutType: TimePickerLayoutType = TimePickerDefaults.layoutType(),
) {
    // State for managing material3's time picker
    val timePickerState =
            rememberTimePickerState(
                initialHour = currentTime.hour,
                initialMinute = currentTime.minute,
                is24Hour = is24Hour
            )
    // Toggle between the clock and the keyboard input
    var isKeyboardInput by rememberSaveable { mutableStateOf(isUseKeyboard) }

    TimePickerDialog(
        title = stringResource(Res.string.select_time),
        toggleKeyboardButton = {
            IconButton(onClick = {
                isKeyboardInput = !isKeyboardInput
                onToggleKeyboard(isKeyboardInput)
            }) {
                if (isKeyboardInput) {
                    Icon(
                        Icons.Filled.Schedule,
                        contentDescription = stringResource(Res.string.clock)
                    )
                } else {
                    Icon(
                        Icons.Filled.Keyboard,
                        contentDescription = stringResource(Res.string.keyboard)
                    )
                }
            }
        },
        onDismissRequest = { onDismiss() },
        confirmButton = {
            TextButton(
                onClick = {
                    onSetTime(timePickerState.hour, timePickerState.minute)
                    onDismiss()
                }
            ) { Text(stringResource(Res.string.ok)) }
        },
        dismissButton = {
            TextButton(
                onClick = { onDismiss() }
            ) { Text(stringResource(Res.string.cancel)) }
        },
        modifier = modifier
    ) {
        if (isKeyboardInput) {
            TimeInput(state = timePickerState)
        } else {
            TimePicker(state = timePickerState, layoutType = layoutType)
        }
    }
}

/**
 * Dialog to render Material3 time picker
 */
@Composable
private fun TimePickerDialog(
    title: String,
    onDismissRequest: () -> Unit,
    toggleKeyboardButton: @Composable (() -> Unit),
    confirmButton: @Composable (() -> Unit),
    dismissButton: @Composable (() -> Unit)?,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        ),
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier = modifier
                .width(IntrinsicSize.Min)
                .height(IntrinsicSize.Min)
                .background(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = containerColor
                ),
            color = containerColor,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    text = title,
                    style = MaterialTheme.typography.labelMedium
                )
                content()
                Row(
                    modifier = Modifier
                        .height(40.dp)
                        .fillMaxWidth()
                ) {
                    toggleKeyboardButton()
                    Spacer(modifier = Modifier.weight(1f))
                    dismissButton?.invoke()
                    confirmButton()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun MaterialTimePickerDialogVerticalPreview() {
    EventDemoAppTheme {
        ShowTimePickerDialog(
            layoutType = TimePickerLayoutType.Vertical,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.LightGray) // showBackground = true
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun MaterialTimePickerDialogHorizontalPreview() {
    EventDemoAppTheme {
        ShowTimePickerDialog(
            layoutType = TimePickerLayoutType.Horizontal,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.LightGray) // Preview parameter: showBackground = true
                .width(800.dp)// Preview parameter: widthDp = 800
        )
    }
}