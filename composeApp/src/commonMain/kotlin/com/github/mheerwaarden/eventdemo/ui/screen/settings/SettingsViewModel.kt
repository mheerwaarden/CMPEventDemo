/*
 * Copyright (c) 2025. Marcel Van Heerwaarden
 * @Author Marcel van Heerwaarden
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.github.mheerwaarden.eventdemo.ui.screen.settings

import androidx.lifecycle.viewModelScope
import com.github.mheerwaarden.eventdemo.data.preferences.UserPreferencesRepository
import com.github.mheerwaarden.eventdemo.ui.screen.LoadingViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
) : LoadingViewModel() {
    var settingsUiState: StateFlow<SettingsUiState> = MutableStateFlow(SettingsUiState())

    override suspend fun loadState() {
        settingsUiState = userPreferencesRepository.preferences.map { preferences ->
            SettingsUiState(
                isReadOnly = preferences.isReadOnly,
                datePickerUsesKeyboard = preferences.datePickerUsesKeyboard,
                timePickerUsesKeyboard = preferences.timePickerUsesKeyboard,
                isCalendarExpanded = preferences.isCalendarExpanded,
                useCraneCalendar = preferences.useCraneCalendar,
            )
        }.stateIn(
            scope = viewModelScope,
            started = WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = SettingsUiState()
        )
    }

    // Prevent interference with an already running preference update
    private var updatePreferenceJob: Job? = null

    fun setDatePickerUsesKeyboard(useKeyboard: Boolean) {
        updatePreferenceJob?.cancel()
        updatePreferenceJob = viewModelScope.launch {
            userPreferencesRepository.saveDatePickerUsesKeyboard(useKeyboard)
        }
    }

    fun setTimePickerUsesKeyboard(useKeyboard: Boolean) {
        updatePreferenceJob?.cancel()
        updatePreferenceJob = viewModelScope.launch {
            userPreferencesRepository.saveTimePickerUsesKeyboard(useKeyboard)
        }
    }

    fun setCalendarExpanded(isExpanded: Boolean) {
        updatePreferenceJob?.cancel()
        updatePreferenceJob = viewModelScope.launch {
            userPreferencesRepository.saveCalendarExpanded(isExpanded)
        }
    }

    fun setUseCraneCalendar(useCraneCalendar: Boolean) {
        updatePreferenceJob?.cancel()
        updatePreferenceJob = viewModelScope.launch {
            userPreferencesRepository.saveUseCraneCalendar(useCraneCalendar)
        }
    }

}

/**
 * Represents the settings which the user can edit within the app.
 */
data class SettingsUiState(
    val isReadOnly: Boolean = true,
    val datePickerUsesKeyboard: Boolean = false,
    val timePickerUsesKeyboard: Boolean = false,
    val isCalendarExpanded: Boolean = true,
    val useCraneCalendar: Boolean = false,
    val useDynamicColor: Boolean = false,
    val useDarkTheme: Boolean = false,
)
