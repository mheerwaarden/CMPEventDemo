/*
 * Copyright (c) 2025. Marcel Van Heerwaarden
 * @Author Marcel van Heerwaarden
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.github.mheerwaarden.eventdemo.ui.screen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface LoadingState {
    data object Loading : LoadingState
    data object Success : LoadingState
    data class Failure(val error: Throwable) : LoadingState
}

abstract class LoadingViewModel : ViewModel() {
    var loadingState: LoadingState by mutableStateOf(LoadingState.Loading)

    init {
        load()
    }

    /**
     * Initial data load in the uiState with progress indication and error handling.
     */
    fun load() {
        loadingState = LoadingState.Loading
        viewModelScope.launch {
            try {
                withContext(Dispatchers.Default) {
                    loadState()
                }
                loadingState = LoadingState.Success
            } catch (e: Exception) {
                loadingState = LoadingState.Failure(e)
            }
        }
    }

    /**
     * Override to load the uiState in the view model. The loadingState is updated by default.
     * The LoadingScreen handles the progress indicator and allows a retry on error messages.
     */
    abstract suspend fun loadState()

    companion object {
        const val TIMEOUT_MILLIS = 5_000L
    }

}