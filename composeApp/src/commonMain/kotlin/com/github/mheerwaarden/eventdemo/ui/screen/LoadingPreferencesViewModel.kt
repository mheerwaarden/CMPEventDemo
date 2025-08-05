/*
 * Copyright (c) 2025. Marcel Van Heerwaarden
 * @Author Marcel van Heerwaarden
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.github.mheerwaarden.eventdemo.ui.screen

import androidx.lifecycle.viewModelScope
import com.github.mheerwaarden.eventdemo.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn

abstract class LoadingPreferencesViewModel(
    preferencesRepository: UserPreferencesRepository
) : LoadingViewModel() {
    val newLoadingState: StateFlow<LoadingState> =
        preferencesRepository.loadingState
            .onEach { loadingState = it }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = LoadingState.Loading
            )

    override suspend fun loadState() {
        println("LoadingPreferencesViewModel skipping loadState")
    }
 }