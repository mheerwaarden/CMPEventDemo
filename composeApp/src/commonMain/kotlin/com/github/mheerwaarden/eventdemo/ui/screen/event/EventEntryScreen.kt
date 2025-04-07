/*
 * Copyright (c) 2025. Marcel Van Heerwaarden
 * @Author Marcel van Heerwaarden
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.github.mheerwaarden.eventdemo.ui.screen.event

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.mheerwaarden.eventdemo.Dimensions
import com.github.mheerwaarden.eventdemo.data.database.DummyEventRepository
import com.github.mheerwaarden.eventdemo.resources.Res
import com.github.mheerwaarden.eventdemo.resources.add_event_title
import com.github.mheerwaarden.eventdemo.resources.description
import com.github.mheerwaarden.eventdemo.resources.save_action
import com.github.mheerwaarden.eventdemo.resources.select_end_time
import com.github.mheerwaarden.eventdemo.resources.select_start_time
import com.github.mheerwaarden.eventdemo.ui.AppViewModelProvider
import com.github.mheerwaarden.eventdemo.ui.components.DateField
import com.github.mheerwaarden.eventdemo.ui.components.DateFieldPreferences
import com.github.mheerwaarden.eventdemo.ui.components.InputField
import com.github.mheerwaarden.eventdemo.ui.components.TimeField
import com.github.mheerwaarden.eventdemo.ui.components.TimeFieldPreferences
import com.github.mheerwaarden.eventdemo.ui.navigation.NavigationDestination
import com.github.mheerwaarden.eventdemo.ui.screen.LoadingScreen
import com.github.mheerwaarden.eventdemo.ui.screen.settings.SettingsViewModel
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

object EventEntryDestination : NavigationDestination {
    override val route = "event_entry"
    override val titleRes = Res.string.add_event_title
}

@Composable
fun EventEntryScreen(
    onUpdateTopAppBar: (String, @Composable (RowScope.() -> Unit)) -> Unit,
    isHorizontalLayout: Boolean,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    eventViewModel: EventEntryViewModel = viewModel(factory = AppViewModelProvider.Factory),
    settingsViewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    onUpdateTopAppBar(stringResource(EventEntryDestination.titleRes)) {}

    LoadingScreen(loadingViewModel = settingsViewModel) {
        val preferences by settingsViewModel.settingsUiState.collectAsState()

        EventEntryBody(
            eventUiState = eventViewModel.eventUiState,
            onDescriptionChange = eventViewModel::updateDescription,
            onDateChange = eventViewModel::updateEventDate,
            onStartTimeChange = eventViewModel::updateEventStartTime,
            onEndTimeChange = eventViewModel::updateEventEndTime,
            onSaveClick = {
                eventViewModel.addEvent()
                navigateBack()
            },
            dateFieldPreferences = DateFieldPreferences(
                isUseKeyboard = preferences.datePickerUsesKeyboard,
                onToggleKeyboard = settingsViewModel::setDatePickerUsesKeyboard
            ),
            timeFieldPreferences = TimeFieldPreferences(
                context = koinInject(),
                isUseKeyboard = preferences.timePickerUsesKeyboard,
                onToggleKeyboard = settingsViewModel::setTimePickerUsesKeyboard,
                isHorizontalLayout = isHorizontalLayout
            ),
            modifier = modifier.padding(Dimensions.padding_small)
        )
    }
}

@Composable
fun EventEntryBody(
    eventUiState: EventUiState,
    onDescriptionChange: (String) -> Unit,
    onDateChange: (LocalDateTime) -> Unit,
    onStartTimeChange: (LocalTime) -> Unit,
    onEndTimeChange: (LocalTime) -> Unit,
    onSaveClick: () -> Unit,
    dateFieldPreferences: DateFieldPreferences,
    timeFieldPreferences: TimeFieldPreferences,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Dimensions.padding_large)
    ) {
        EventInputForm(
            eventUiState = eventUiState,
            onDescriptionChange = onDescriptionChange,
            onDateChange = onDateChange,
            onStartTimeChange = onStartTimeChange,
            onEndTimeChange = onEndTimeChange,
            dateFieldPreferences = dateFieldPreferences,
            timeFieldPreferences = timeFieldPreferences,
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = onSaveClick,
            enabled = eventUiState.isEntryValid,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(Res.string.save_action))
        }
    }
}

@Composable
fun EventInputForm(
    eventUiState: EventUiState,
    onDescriptionChange: (String) -> Unit,
    onDateChange: (LocalDateTime) -> Unit,
    onStartTimeChange: (LocalTime) -> Unit,
    onEndTimeChange: (LocalTime) -> Unit,
    dateFieldPreferences: DateFieldPreferences,
    timeFieldPreferences: TimeFieldPreferences,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Dimensions.padding_medium)
    ) {
        DateField(
            currentDate = eventUiState.startDateTime,
            onDateChange = { onDateChange(it) },
            modifier = Modifier.fillMaxWidth(),
            preferences = dateFieldPreferences,
        )
        TimeField(
            currentTime = eventUiState.startDateTime.time,
            onTimeChange = { localTime -> onStartTimeChange(localTime) },
            modifier = Modifier.fillMaxWidth(),
            preferences = timeFieldPreferences,
            labelId = Res.string.select_start_time
        )
        TimeField(
            currentTime = eventUiState.endDateTime.time,
            onTimeChange = { localTime -> onEndTimeChange(localTime) },
            modifier = Modifier.fillMaxWidth(),
            preferences = timeFieldPreferences,
            labelId = Res.string.select_end_time
        )
        InputField(
            labelId = Res.string.description,
            value = eventUiState.description,
            onValueChange = { onDescriptionChange(it) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview
@Composable
fun EventEntryScreenPreview() {
    val event = DummyEventRepository().getDummyEvents(1).first()
    EventEntryBody(
        eventUiState = event.toEventUiState(),
        onDescriptionChange = {},
        onDateChange = {},
        onStartTimeChange = {},
        onEndTimeChange = {},
        onSaveClick = {},
        dateFieldPreferences = DateFieldPreferences(),
        timeFieldPreferences = TimeFieldPreferences(),
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray) // showBackground = true
    )
}