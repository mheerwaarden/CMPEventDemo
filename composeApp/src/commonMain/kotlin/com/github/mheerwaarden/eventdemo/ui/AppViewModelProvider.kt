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
import com.github.mheerwaarden.eventdemo.ui.screen.event.EventCalendarViewModel
import com.github.mheerwaarden.eventdemo.ui.screen.event.EventEditViewModel
import com.github.mheerwaarden.eventdemo.ui.screen.event.EventEntryViewModel
import com.github.mheerwaarden.eventdemo.ui.screen.event.EventOverviewViewModel
import com.github.mheerwaarden.eventdemo.ui.screen.event.EventViewModel
import org.koin.core.component.KoinComponent

object AppViewModelProvider : KoinComponent {
    val Factory = viewModelFactory {
        /* region Event ViewModels */
        val appContainer = EventDemoApplication.container
        initializer {
            EventOverviewViewModel(
                appContainer.eventRepository
            )
        }
        initializer {
            EventCalendarViewModel(
                appContainer.eventRepository,
            )
        }
        initializer {
            EventEntryViewModel(
                this.createSavedStateHandle(),
                appContainer.eventRepository,
            )
        }
        initializer {
            EventEditViewModel(
                this.createSavedStateHandle(),
                appContainer.eventRepository,
            )
        }

        initializer {
            EventViewModel(
                this.createSavedStateHandle(),
                appContainer.eventRepository,
            )
        }
        // endregion

    }
}

