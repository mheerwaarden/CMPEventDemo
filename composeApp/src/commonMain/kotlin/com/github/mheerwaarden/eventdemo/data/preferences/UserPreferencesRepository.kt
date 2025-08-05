/*
 * Copyright (c) 2025. Marcel Van Heerwaarden
 * @Author Marcel van Heerwaarden
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.github.mheerwaarden.eventdemo.data.preferences

import com.github.mheerwaarden.eventdemo.ui.screen.LoadingState
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    /** Return a flow that keeps the actual values of settings in the UserPreference object */
    val preferences: Flow<UserPreferences>
    /** Return a flow that keeps the loading state of the settings */
    val loadingState: Flow<LoadingState>

    fun loadPreferences()
    
    suspend fun saveReadOnlyPreference(isReadOnly: Boolean)
    suspend fun saveDatePickerUsesKeyboard(useKeyboard: Boolean)
    suspend fun saveTimePickerUsesKeyboard(useKeyboard: Boolean)
    suspend fun saveCalendarExpanded(isExpanded: Boolean)
    suspend fun saveUseCraneCalendar(useCraneCalendar: Boolean)
    suspend fun saveLocalePreference(localeTag: String)
    suspend fun saveUsePocketBase(usePocketBase: Boolean)
    suspend fun savePocketBaseUrl(url: String)
}