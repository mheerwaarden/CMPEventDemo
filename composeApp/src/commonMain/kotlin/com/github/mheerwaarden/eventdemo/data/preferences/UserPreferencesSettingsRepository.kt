package com.github.mheerwaarden.eventdemo.data.preferences

import co.touchlab.kermit.Logger
import com.russhwolf.settings.Settings
import com.russhwolf.settings.set

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class UserPreferencesSettingsRepository(
    private val settings: Settings,
    private val logger: Logger,
) : UserPreferencesRepository {
    //region Keys
    companion object {
        private const val KEY_IS_READ_ONLY = "is_read_only"
        private const val KEY_DATE_PICKER_USES_KEYBOARD = "date_picker_uses_keyboard"
        private const val KEY_TIME_PICKER_USES_KEYBOARD = "time_picker_uses_keyboard"
        private const val KEY_CALENDAR_EXPANDED = "calendar_expanded"
        private const val KEY_USE_CRANE_CALENDAR = "use_crane_calendar"
        private const val KEY_LOCALE_TAG = "locale_tag"
        private const val KEY_USE_POCKETBASE = "use_pocketbase"
        private const val KEY_POCKETBASE_URL = "pocketbase_url"
    }
    //endregion

    //region Flow
    private val _preferences: MutableStateFlow<UserPreferences> =
            MutableStateFlow(loadPreferences())
    override val preferences: Flow<UserPreferences> = _preferences.asStateFlow()
    //endregion

    //region Load
    private fun loadPreferences(): UserPreferences {
        println("UserPreferencesSettingsRepository: loadPreferences")
        return try {
            UserPreferences(
                isReadOnly = settings.getBoolean(KEY_IS_READ_ONLY, false),
                datePickerUsesKeyboard = settings.getBoolean(KEY_DATE_PICKER_USES_KEYBOARD, false),
                timePickerUsesKeyboard = settings.getBoolean(KEY_TIME_PICKER_USES_KEYBOARD, false),
                isCalendarExpanded = settings.getBoolean(KEY_CALENDAR_EXPANDED, true),
                useCraneCalendar = settings.getBoolean(KEY_USE_CRANE_CALENDAR, false),
                localeTag = settings.getString(KEY_LOCALE_TAG, DEFAULT_LOCALE_FROM_PLATFORM),
                usePocketBase = settings.getBoolean(KEY_USE_POCKETBASE, false),
                pocketBaseUrl = settings.getString(KEY_POCKETBASE_URL, ""),
            )

        } catch (e: Exception) {
            logger.e(throwable = e) { "Error loading user preferences" }
            // Return default preferences in case of error
            UserPreferences()
        }
    }
    //endregion

    //region Save
    override suspend fun saveReadOnlyPreference(isReadOnly: Boolean) {
        try {
            settings[KEY_IS_READ_ONLY] = isReadOnly
            updatePreferences { it.copy(isReadOnly = isReadOnly) }
        } catch (e: Exception) {
            logger.e(throwable = e) { "Error saving isReadOnly preference" }
        }
    }

    override suspend fun saveDatePickerUsesKeyboard(useKeyboard: Boolean) {
        try {
            settings[KEY_DATE_PICKER_USES_KEYBOARD] = useKeyboard
            updatePreferences { it.copy(datePickerUsesKeyboard = useKeyboard) }
        } catch (e: Exception) {
            logger.e(throwable = e) { "Error saving datePickerUsesKeyboard preference" }
        }
    }

    override suspend fun saveTimePickerUsesKeyboard(useKeyboard: Boolean) {
        try {
            settings[KEY_TIME_PICKER_USES_KEYBOARD] = useKeyboard
            updatePreferences { it.copy(timePickerUsesKeyboard = useKeyboard) }
        } catch (e: Exception) {
            logger.e(throwable = e) { "Error saving timePickerUsesKeyboard preference" }
        }
    }

    override suspend fun saveCalendarExpanded(isExpanded: Boolean) {
        try {
            settings[KEY_CALENDAR_EXPANDED] = isExpanded
            updatePreferences { it.copy(isCalendarExpanded = isExpanded) }
        } catch (e: Exception) {
            logger.e(throwable = e) { "Error saving isCalendarExpanded preference" }
        }
    }

    override suspend fun saveUseCraneCalendar(useCraneCalendar: Boolean) {
        try {
            settings[KEY_USE_CRANE_CALENDAR] = useCraneCalendar
            updatePreferences { it.copy(useCraneCalendar = useCraneCalendar) }
        } catch (e: Exception) {
            logger.e(throwable = e) { "Error saving useCraneCalendar preference" }
        }
    }

    override suspend fun saveLocalePreference(localeTag: String) {
        try {
            settings[KEY_LOCALE_TAG] = localeTag
            updatePreferences { it.copy(localeTag = localeTag) }
        } catch (e: Exception) {
            logger.e(throwable = e) { "Error saving locale preference" }
        }
    }

    override suspend fun saveUsePocketBase(usePocketBase: Boolean) {
        try {
            settings[KEY_USE_POCKETBASE] = usePocketBase
            updatePreferences { it.copy(usePocketBase = usePocketBase) }
        } catch (e: Exception) {
            logger.e(throwable = e) { "Error saving usePocketBase preference" }
        }
    }

    override suspend fun savePocketBaseUrl(url: String) {
        try {
            settings[KEY_POCKETBASE_URL] = url
            updatePreferences { it.copy(pocketBaseUrl = url) }
        } catch (e: Exception) {
            logger.e(throwable = e) { "Error saving pocketBaseUrl preference" }
        }
    }

    //endregion

    //region Update
    private fun updatePreferences(transform: (UserPreferences) -> UserPreferences) {
        _preferences.update(transform)
    }
    //endregion
}