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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.mheerwaarden.eventdemo.resources.Res
import com.github.mheerwaarden.eventdemo.resources.settings
import com.github.mheerwaarden.eventdemo.ui.AppViewModelProvider
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
    onUpdateTopAppBar: (String, @Composable (RowScope.() -> Unit)) -> Unit,
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    onUpdateTopAppBar(stringResource(SettingsDestination.titleRes)) {}

    LoadingScreen(loadingViewModel = settingsViewModel) {
        val settingsUiState by settingsViewModel.settingsUiState.collectAsState()

        SettingsBody(
            settingsUiState = settingsUiState,
            setDatePickerUsesKeyboard = settingsViewModel::setDatePickerUsesKeyboard,
            setTimePickerUsesKeyboard = settingsViewModel::setTimePickerUsesKeyboard,
            modifier = modifier
        )
    }
}

@Composable
fun SettingsBody(
    settingsUiState: SettingsUiState,
    setDatePickerUsesKeyboard: (Boolean) -> Unit,
    setTimePickerUsesKeyboard: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier.fillMaxWidth()
        ) {
            Text(text = "Use keyboard for date input")
            Switch(
                checked = settingsUiState.datePickerUsesKeyboard,
                onCheckedChange = setDatePickerUsesKeyboard
            )
        }
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier.fillMaxWidth()
        ) {
            Text(text = "Use keyboard for time input")
            Switch(
                checked = settingsUiState.timePickerUsesKeyboard,
                onCheckedChange = setTimePickerUsesKeyboard
            )
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
                timePickerUsesKeyboard = false
            ),
            setDatePickerUsesKeyboard = {},
            setTimePickerUsesKeyboard = {},
            modifier = Modifier
                .fillMaxSize()
                .background(Color.LightGray) // showBackground = true
        )
    }
}