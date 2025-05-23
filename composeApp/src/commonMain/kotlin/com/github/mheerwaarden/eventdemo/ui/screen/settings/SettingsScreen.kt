/*
 * Copyright (c) 2025. Marcel Van Heerwaarden
 * @Author Marcel van Heerwaarden
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.github.mheerwaarden.eventdemo.ui.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.mheerwaarden.eventdemo.Dimensions
import com.github.mheerwaarden.eventdemo.i18n.AppLanguage
import com.github.mheerwaarden.eventdemo.i18n.LocaleViewModel
import com.github.mheerwaarden.eventdemo.resources.Res
import com.github.mheerwaarden.eventdemo.resources.language
import com.github.mheerwaarden.eventdemo.resources.settings
import com.github.mheerwaarden.eventdemo.resources.use_crane_calendar
import com.github.mheerwaarden.eventdemo.resources.use_keyboard_for_date_input
import com.github.mheerwaarden.eventdemo.resources.use_keyboard_for_time_input
import com.github.mheerwaarden.eventdemo.ui.AppViewModelProvider
import com.github.mheerwaarden.eventdemo.ui.components.BooleanInputField
import com.github.mheerwaarden.eventdemo.ui.components.SelectionField
import com.github.mheerwaarden.eventdemo.ui.navigation.NavigationDestination
import com.github.mheerwaarden.eventdemo.ui.screen.LoadingScreen
import com.github.mheerwaarden.eventdemo.ui.theme.EventDemoAppTheme
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

object SettingsDestination : NavigationDestination {
    override val route = "settings"
    override val titleRes = Res.string.settings
}

@Composable
fun SettingsScreen(
    onUpdateTopAppBar: (String, (() -> Unit)?, @Composable (RowScope.() -> Unit)) -> Unit,
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory),
    localeViewModel: LocaleViewModel = koinInject()
) {
    onUpdateTopAppBar(stringResource(SettingsDestination.titleRes), null) {}

    LoadingScreen(loadingViewModels = listOf(settingsViewModel, localeViewModel)) {
        val settingsUiState by settingsViewModel.settingsUiState.collectAsState()

        SettingsBody(
            settingsUiState = settingsUiState,
            language = AppLanguage.entries.firstOrNull { settingsUiState.localeTag.startsWith(it.code) } ?: AppLanguage.System,
            setDatePickerUsesKeyboard = settingsViewModel::setDatePickerUsesKeyboard,
            setTimePickerUsesKeyboard = settingsViewModel::setTimePickerUsesKeyboard,
            setUseCraneCalendar = settingsViewModel::setUseCraneCalendar,
            setLanguage = localeViewModel::setPreferredAppLocale,
            modifier = modifier.padding(Dimensions.padding_small)
        )
    }
}

@Composable
fun SettingsBody(
    settingsUiState: SettingsUiState,
    language: AppLanguage,
    setDatePickerUsesKeyboard: (Boolean) -> Unit,
    setTimePickerUsesKeyboard: (Boolean) -> Unit,
    setUseCraneCalendar: (Boolean) -> Unit,
    setLanguage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        BooleanInputField(
            labelId = Res.string.use_keyboard_for_date_input,
            value = settingsUiState.datePickerUsesKeyboard,
            onValueChange = setDatePickerUsesKeyboard,
            isSwitch = true,
            modifier = Modifier.fillMaxWidth()
        )
        BooleanInputField(
            labelId = Res.string.use_keyboard_for_time_input,
            value = settingsUiState.timePickerUsesKeyboard,
            onValueChange = setTimePickerUsesKeyboard,
            isSwitch = true,
            modifier = Modifier.fillMaxWidth()
        )
        BooleanInputField(
            labelId = Res.string.use_crane_calendar,
            value = settingsUiState.useCraneCalendar,
            onValueChange = setUseCraneCalendar,
            isSwitch = true,
            modifier = Modifier.fillMaxWidth()
        )
        SelectionField(
            label = stringResource(Res.string.language),
            currentItem = language,
            onGetItems = { AppLanguage.entries },
            onGetKey = { it.ordinal },
            onGetDisplayName = { stringResource(it.stringRes) },
            onChange = { setLanguage(it.code) },
            modifier = Modifier.fillMaxWidth(),
            isRequired = false,
        )
    }
}

@Preview
@Composable
fun SettingsScreenPreview() {
    EventDemoAppTheme {
        SettingsBody(
            settingsUiState = SettingsUiState(
                datePickerUsesKeyboard = true,
                timePickerUsesKeyboard = false
            ),
            language = AppLanguage.English,
            setDatePickerUsesKeyboard = {},
            setTimePickerUsesKeyboard = {},
            setUseCraneCalendar = {},
            setLanguage = {},
            modifier = Modifier
                .fillMaxSize()
                .background(Color.LightGray) // showBackground = true
        )
    }
}