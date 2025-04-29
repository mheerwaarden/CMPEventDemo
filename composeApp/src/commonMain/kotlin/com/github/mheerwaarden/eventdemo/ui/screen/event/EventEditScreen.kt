/*
 * Copyright (c) 2025. Marcel Van Heerwaarden
 * @Author Marcel van Heerwaarden
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.github.mheerwaarden.eventdemo.ui.screen.event

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.mheerwaarden.eventdemo.Dimensions
import com.github.mheerwaarden.eventdemo.resources.Res
import com.github.mheerwaarden.eventdemo.resources.edit_event_title
import com.github.mheerwaarden.eventdemo.ui.AppViewModelProvider
import com.github.mheerwaarden.eventdemo.ui.components.DateFieldPreferences
import com.github.mheerwaarden.eventdemo.ui.components.TimeFieldPreferences
import com.github.mheerwaarden.eventdemo.ui.navigation.NavigationDestination
import com.github.mheerwaarden.eventdemo.ui.screen.LoadingScreen
import com.github.mheerwaarden.eventdemo.ui.screen.settings.SettingsViewModel
import org.jetbrains.compose.resources.stringResource

object EventEditDestination : NavigationDestination {
    override val route = "event_edit"
    override val titleRes = Res.string.edit_event_title
    const val eventIdArg = "eventId"
    val routeWithArgs = "$route/{$eventIdArg}"
}

@Composable
fun EventEditScreen(
    onUpdateTopAppBar: (String, @Composable (RowScope.() -> Unit)) -> Unit,
    isHorizontalLayout: Boolean,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    eventViewModel: EventEditViewModel = viewModel(factory = AppViewModelProvider.Factory),
    settingsViewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    onUpdateTopAppBar(stringResource(EventEditDestination.titleRes)) {}

    LoadingScreen(loadingViewModel = settingsViewModel) {
        val preferences by settingsViewModel.settingsUiState.collectAsState()

        EventEntryBody(
            eventUiState = eventViewModel.eventUiState,
            isStartTimeAutoUpdated = false,
            onDescriptionChange = eventViewModel::updateDescription,
            onDateChange = eventViewModel::updateEventDate,
            onStartTimeChange = eventViewModel::updateEventStartTime,
            onEndTimeChange = eventViewModel::updateEventEndTime,
            onSaveClick = {
                eventViewModel.updateEvent()
                navigateBack()
            },
            dateFieldPreferences = DateFieldPreferences(
                isUseKeyboard = preferences.datePickerUsesKeyboard,
                onToggleKeyboard = settingsViewModel::setDatePickerUsesKeyboard
            ),
            timeFieldPreferences = TimeFieldPreferences(
                is24Hour = true,
                isUseKeyboard = preferences.timePickerUsesKeyboard,
                onToggleKeyboard = settingsViewModel::setTimePickerUsesKeyboard,
                isHorizontalLayout = isHorizontalLayout
            ),
            modifier = modifier.padding(Dimensions.padding_small),
        )
    }
}
