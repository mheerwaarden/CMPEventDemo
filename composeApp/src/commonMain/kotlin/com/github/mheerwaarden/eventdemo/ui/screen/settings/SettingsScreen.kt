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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.github.mheerwaarden.eventdemo.Dimensions
import com.github.mheerwaarden.eventdemo.data.preferences.PocketBaseClientType
import com.github.mheerwaarden.eventdemo.resources.Res
import com.github.mheerwaarden.eventdemo.resources.database_url
import com.github.mheerwaarden.eventdemo.resources.language
import com.github.mheerwaarden.eventdemo.resources.settings
import com.github.mheerwaarden.eventdemo.resources.use_crane_calendar
import com.github.mheerwaarden.eventdemo.resources.use_database
import com.github.mheerwaarden.eventdemo.resources.use_keyboard_for_date_input
import com.github.mheerwaarden.eventdemo.resources.use_keyboard_for_time_input
import com.github.mheerwaarden.eventdemo.ui.components.BooleanInputField
import com.github.mheerwaarden.eventdemo.ui.components.InputField
import com.github.mheerwaarden.eventdemo.ui.components.SelectionField
import com.github.mheerwaarden.eventdemo.ui.localization.AppLanguage
import com.github.mheerwaarden.eventdemo.ui.navigation.NavigationDestination
import com.github.mheerwaarden.eventdemo.ui.screen.LoadingScreen
import com.github.mheerwaarden.eventdemo.ui.theme.EventDemoAppTheme
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

object SettingsDestination : NavigationDestination {
    override val route = "settings"
    override val titleRes = Res.string.settings
}

@Composable
fun SettingsScreen(
    onUpdateTopAppBar: (String, (() -> Unit)?, @Composable (RowScope.() -> Unit)) -> Unit,
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel
) {
    onUpdateTopAppBar(stringResource(SettingsDestination.titleRes), null) {}

    LoadingScreen(loadingViewModel = settingsViewModel) {
        val settingsUiState by settingsViewModel.settingsUiState.collectAsState()

        SettingsBody(
            settingsUiState = settingsUiState,
            language = AppLanguage.entries.firstOrNull { settingsUiState.localeTag.startsWith(it.code) }
                ?: AppLanguage.System,
            setDatePickerUsesKeyboard = settingsViewModel::setDatePickerUsesKeyboard,
            setTimePickerUsesKeyboard = settingsViewModel::setTimePickerUsesKeyboard,
            setUseCraneCalendar = settingsViewModel::setUseCraneCalendar,
            setLanguage = settingsViewModel::setLocale,
            setUsesPocketBase = settingsViewModel::setUsePocketBase,
            setPocketBaseUrl = settingsViewModel::setPocketBaseUrl,
            setPocketBaseClientType = settingsViewModel::setPocketBaseClientType,
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
    setUsesPocketBase: (Boolean) -> Unit,
    setPocketBaseUrl: (String) -> Unit,
    setPocketBaseClientType: (PocketBaseClientType) -> Unit,
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
        BooleanInputField(
            labelId = Res.string.use_database,
            value = settingsUiState.usePocketBase,
            onValueChange = setUsesPocketBase,
            isSwitch = true,
            modifier = Modifier.fillMaxWidth()
        )
        InputField(
            labelId = Res.string.database_url,
            value = settingsUiState.pocketBaseUrl,
            onValueChange = setPocketBaseUrl,
            modifier = Modifier.fillMaxWidth(),
        )
        PocketBaseClientTypeSelector(
            selectedType = settingsUiState.pocketBaseClientType,
            onTypeSelected = setPocketBaseClientType
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

@Composable
fun PocketBaseClientTypeSelector(
    selectedType: PocketBaseClientType,
    onTypeSelected: (PocketBaseClientType) -> Unit
) {
    val implementedTypes = listOf(PocketBaseClientType.KTOR_ONLY)
    Column {
        Text("PocketBase Client Implementation:")

        PocketBaseClientType.entries.forEach { type ->
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedType == type,
                    onClick = { onTypeSelected(type) },
                    enabled = implementedTypes.contains(type)
                )
                Text(
                    text = when (type) {
                        PocketBaseClientType.KTOR_ONLY -> "Ktor Only"
                        PocketBaseClientType.POCKETBASE_KOTLIN_KTOR_WEB -> "PocketBase-Kotlin + Ktor Web"
                        PocketBaseClientType.POCKETBASE_KOTLIN_JS_WEB -> "PocketBase-Kotlin + JS Web"
                        PocketBaseClientType.POCKETBASE_KOTLIN_ONLY -> "PocketBase-Kotlin Only"
                    },
                    modifier = Modifier.padding(start = Dimensions.padding_small)
                )
            }
        }
    }
}

@Preview
@Composable
fun SettingsScreenPreview() {
    EventDemoAppTheme {
        SettingsBody(
            settingsUiState = SettingsUiState(
                datePickerUsesKeyboard = true,
                timePickerUsesKeyboard = false,
            ),
            language = AppLanguage.English,
            setDatePickerUsesKeyboard = {},
            setTimePickerUsesKeyboard = {},
            setUseCraneCalendar = {},
            setLanguage = {},
            setUsesPocketBase = {},
            setPocketBaseUrl = {},
            setPocketBaseClientType = {},
            modifier = Modifier
                .fillMaxSize()
                .background(Color.LightGray) // showBackground = true
        )
    }
}