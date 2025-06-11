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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.mheerwaarden.eventdemo.Dimensions
import com.github.mheerwaarden.eventdemo.data.database.DummyEventRepository
import com.github.mheerwaarden.eventdemo.data.model.EventCategory
import com.github.mheerwaarden.eventdemo.data.model.EventType
import com.github.mheerwaarden.eventdemo.resources.Res
import com.github.mheerwaarden.eventdemo.resources.add_event_title
import com.github.mheerwaarden.eventdemo.resources.color
import com.github.mheerwaarden.eventdemo.resources.contact
import com.github.mheerwaarden.eventdemo.resources.date_passed_set_to_today
import com.github.mheerwaarden.eventdemo.resources.description
import com.github.mheerwaarden.eventdemo.resources.event_category
import com.github.mheerwaarden.eventdemo.resources.event_type
import com.github.mheerwaarden.eventdemo.resources.is_online
import com.github.mheerwaarden.eventdemo.resources.location
import com.github.mheerwaarden.eventdemo.resources.notes
import com.github.mheerwaarden.eventdemo.resources.save_action
import com.github.mheerwaarden.eventdemo.resources.select_end_time
import com.github.mheerwaarden.eventdemo.resources.select_start_time
import com.github.mheerwaarden.eventdemo.ui.AppViewModelProvider
import com.github.mheerwaarden.eventdemo.ui.components.BooleanInputField
import com.github.mheerwaarden.eventdemo.ui.components.CraneCalendarField
import com.github.mheerwaarden.eventdemo.ui.components.DateField
import com.github.mheerwaarden.eventdemo.ui.components.DateFieldPreferences
import com.github.mheerwaarden.eventdemo.ui.components.InputField
import com.github.mheerwaarden.eventdemo.ui.components.SelectionField
import com.github.mheerwaarden.eventdemo.ui.components.TimeField
import com.github.mheerwaarden.eventdemo.ui.components.TimeFieldPreferences
import com.github.mheerwaarden.eventdemo.ui.navigation.NavigationDestination
import com.github.mheerwaarden.eventdemo.ui.screen.LoadingScreen
import com.github.mheerwaarden.eventdemo.ui.screen.settings.SettingsViewModel
import com.github.mheerwaarden.eventdemo.ui.theme.EventDemoAppTheme
import com.github.mheerwaarden.eventdemo.ui.util.HtmlColors
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

object EventEntryDestination : NavigationDestination {
    override val route = "event_entry"
    override val titleRes = Res.string.add_event_title
    const val startDateArg = "startDateId"
    val routeWithArgs = "$route/{$startDateArg}"
}

@Composable
fun EventEntryScreen(
    onUpdateTopAppBar: (String, (() -> Unit)?, @Composable (RowScope.() -> Unit)) -> Unit,
    isHorizontalLayout: Boolean,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    eventViewModel: EventEntryViewModel = viewModel(factory = AppViewModelProvider.Factory),
    settingsViewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    onUpdateTopAppBar(stringResource(EventEntryDestination.titleRes), null) {}

    val eventUiState = eventViewModel.eventUiState
    LoadingScreen(loadingViewModel = settingsViewModel) {
        val preferences by settingsViewModel.settingsUiState.collectAsState()
        var isStartTimeAutoUpdated by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            isStartTimeAutoUpdated = eventViewModel.initialDate < eventUiState.startDateTime.date
        }

        EventEntryBody(
            eventUiState = eventUiState,
            isStartTimeAutoUpdated = isStartTimeAutoUpdated,
            onDateChange = { localStartDateTime, localEndDateTime ->
                eventViewModel.updateEventDate(localStartDateTime, localEndDateTime)
                isStartTimeAutoUpdated = false
            },
            onStartTimeChange = eventViewModel::updateEventStartTime,
            onEndTimeChange = eventViewModel::updateEventEndTime,
            onStateChange = eventViewModel::updateState,
            onSaveClick = {
                eventViewModel.addEvent()
                navigateBack()
            },
            dateFieldPreferences = DateFieldPreferences(
                isUseKeyboard = preferences.datePickerUsesKeyboard,
                onToggleKeyboard = settingsViewModel::setDatePickerUsesKeyboard
            ),
            timeFieldPreferences = TimeFieldPreferences(
                isUseKeyboard = preferences.timePickerUsesKeyboard,
                onToggleKeyboard = settingsViewModel::setTimePickerUsesKeyboard,
                isHorizontalLayout = isHorizontalLayout
            ),
            useCraneCalendar = preferences.useCraneCalendar,
            modifier = modifier.padding(Dimensions.padding_small)
        )
    }
}

@Composable
fun EventEntryBody(
    eventUiState: EventUiState,
    isStartTimeAutoUpdated: Boolean,
    onDateChange: (LocalDate?, LocalDate?) -> Unit,
    onStartTimeChange: (LocalTime) -> Unit,
    onEndTimeChange: (LocalTime) -> Unit,
    onStateChange: (EventUiState) -> Unit,
    onSaveClick: () -> Unit,
    dateFieldPreferences: DateFieldPreferences,
    timeFieldPreferences: TimeFieldPreferences,
    useCraneCalendar: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(Dimensions.padding_large)
    ) {
        if (isStartTimeAutoUpdated) {
            Text(
                text = stringResource(Res.string.date_passed_set_to_today),
                color = MaterialTheme.colorScheme.tertiary
            )
        }
        EventInputForm(
            eventUiState = eventUiState,
            onDateChange = onDateChange,
            onStartTimeChange = onStartTimeChange,
            onEndTimeChange = onEndTimeChange,
            onStateChange = onStateChange,
            dateFieldPreferences = dateFieldPreferences,
            timeFieldPreferences = timeFieldPreferences,
            useCraneCalendar = useCraneCalendar,
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
    onDateChange: (LocalDate?, LocalDate?) -> Unit,
    onStartTimeChange: (LocalTime) -> Unit,
    onEndTimeChange: (LocalTime) -> Unit,
    onStateChange: (EventUiState) -> Unit,
    dateFieldPreferences: DateFieldPreferences,
    timeFieldPreferences: TimeFieldPreferences,
    useCraneCalendar: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Dimensions.padding_small)
    ) {
        if (useCraneCalendar) {
            CraneCalendarField(
                startDate = eventUiState.startDateTime.date,
                endDate = eventUiState.endDateTime.date,
                onDateChange = onDateChange,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            // Default: Material Design date picker dialog.
            DateField(
                currentDate = eventUiState.startDateTime.date,
                onDateChange = { onDateChange(it, null) },
                modifier = Modifier.fillMaxWidth(),
                preferences = dateFieldPreferences,
            )
        }
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
            onValueChange = { onStateChange(eventUiState.copy(description = it)) },
            modifier = Modifier.fillMaxWidth(),
        )
        InputField(
            labelId = Res.string.location,
            value = eventUiState.location ?: "",
            onValueChange = { onStateChange(eventUiState.copy(location = it)) },
            modifier = Modifier.fillMaxWidth(),
        )
        BooleanInputField(
            labelId = Res.string.is_online,
            value = eventUiState.isOnline,
            onValueChange = { onStateChange(eventUiState.copy(isOnline = it)) },
            isSwitch = false,
            modifier = Modifier.fillMaxWidth(),
        )
        InputField(
            labelId = Res.string.contact,
            value = eventUiState.contact ?: "",
            onValueChange = { onStateChange(eventUiState.copy(contact = it)) },
            modifier = Modifier.fillMaxWidth(),
        )
        InputField(
            labelId = Res.string.notes,
            value = eventUiState.notes ?: "",
            onValueChange = { onStateChange(eventUiState.copy(notes = it)) },
            modifier = Modifier.fillMaxWidth(),
        )
        SelectionField(
            label = stringResource(Res.string.event_type),
            currentItem = eventUiState.eventType,
            onGetItems = { EventType.entries },
            onGetKey = { it.ordinal },
            onGetDisplayName = { stringResource(it.text) },
            onChange = { onStateChange(eventUiState.copy(eventType = it)) },
            modifier = Modifier.fillMaxWidth(),
            isRequired = false,
        )
        SelectionField(
            label = stringResource(Res.string.event_category),
            currentItem = eventUiState.eventCategory,
            onGetItems = { EventCategory.entries },
            onGetKey = { it.ordinal },
            onGetDisplayName = { stringResource(it.text) },
            onChange = { onStateChange(eventUiState.copy(eventCategory = it)) },
            modifier = Modifier.fillMaxWidth(),
            isRequired = false,
        )
        SelectionField(
            label = stringResource(Res.string.color),
            currentItem = eventUiState.htmlColor,
            onGetItems = { HtmlColors.entries },
            onGetKey = { it.ordinal },
            onGetDisplayName = {
                buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            color = it.color,
                            fontSize = MaterialTheme.typography.headlineLarge.fontSize
                        )
                    ) {
                        append("• ")
                    }
                    append(it.text)
                }
            },
            onChange = { onStateChange(eventUiState.copy(htmlColor = it)) },
            modifier = Modifier.fillMaxWidth(),
            isRequired = false,
        )
    }
}

@Preview
@Composable
fun EventEntryScreenPreview() {
    val event = DummyEventRepository().getDefaultEvents(1).first()
    EventDemoAppTheme {
        EventEntryBody(
            eventUiState = event.toEventUiState(),
            isStartTimeAutoUpdated = true,
            onDateChange = { _, _ -> },
            onStartTimeChange = {},
            onEndTimeChange = {},
            onStateChange = {},
            onSaveClick = {},
            dateFieldPreferences = DateFieldPreferences(),
            timeFieldPreferences = TimeFieldPreferences(),
            useCraneCalendar = false,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.LightGray) // showBackground = true
        )
    }
}