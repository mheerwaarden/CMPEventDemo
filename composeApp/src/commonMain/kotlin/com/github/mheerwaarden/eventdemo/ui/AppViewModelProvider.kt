/*
 * Copyright (c) 2025. Marcel Van Heerwaarden
 * @Author Marcel van Heerwaarden
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.github.mheerwaarden.eventdemo.ui

import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.github.mheerwaarden.eventdemo.EventDemoApplication
import com.github.mheerwaarden.eventdemo.data.preferences.UserPreferencesRepository
import com.github.mheerwaarden.eventdemo.ui.screen.event.EventEditViewModel
import com.github.mheerwaarden.eventdemo.ui.screen.event.EventEntryViewModel
import com.github.mheerwaarden.eventdemo.ui.screen.event.EventViewModel
import com.github.mheerwaarden.eventdemo.ui.screen.settings.SettingsViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object AppViewModelProvider : KoinComponent {
    private val userPreferencesRepository: UserPreferencesRepository by inject<UserPreferencesRepository>()
    val Factory = viewModelFactory {

        /* region Event ViewModels */
        initializer {
            val appContainer = EventDemoApplication.container
            EventViewModel(
                appContainer.eventRepository,
                userPreferencesRepository
            )
        }
        initializer {
            val appContainer = EventDemoApplication.container
            EventEntryViewModel(
                appContainer.eventRepository,
            )
        }
        initializer {
            val appContainer = EventDemoApplication.container
            EventEditViewModel(
                this.createSavedStateHandle(),
                appContainer.eventRepository,
            )
        }
        // endregion
        
        /* region Settings ViewModel */
        initializer {
            SettingsViewModel(userPreferencesRepository)
        }
        // endregion
    }
}

