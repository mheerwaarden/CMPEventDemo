package com.github.mheerwaarden.eventdemo.data.preferences

import co.touchlab.kermit.Logger
import com.github.mheerwaarden.eventdemo.data.DataLoadingState
import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update

class UserPreferencesSettingsRepository(
    private val settings: Settings,
    private val logger: Logger,
    defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
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
        private const val KEY_POCKETBASE_CLIENT_TYPE = "pocketbase_client_type"
    }
    //endregion Keys

    //region Flow
    private val _preferences = MutableStateFlow(UserPreferences.DEFAULTS)
    override val preferences = _preferences as StateFlow<UserPreferences>

    override val loadingState: Flow<DataLoadingState> = flow {
        try {
            println("Settings loadingState: Emit loading...")
            emit(DataLoadingState.Loading)
            println("Settings loadingState: Loading initial preferences...")
            loadInitialPreferences()
            println("Settings loadingState: Emit success...")
            emit(DataLoadingState.Success)
            println("Settings loadingState: Done emitting")
        } catch (e: Exception) {
            println("Settings loadingState: Emit error")
            emit(DataLoadingState.Error(e))
        }
    }.flowOn(defaultDispatcher)

    override fun prepareReload() {
        // Nothing to do, collecting the flow again will return the latest values
    }

    //endregion Flow

    //region Load
    private fun loadInitialPreferences() {
        val loadedPreferences = UserPreferences(
            isReadOnly = settings.getBoolean(
                KEY_IS_READ_ONLY, UserPreferences.DEFAULTS.isReadOnly
            ),
            datePickerUsesKeyboard = settings.getBoolean(
                KEY_DATE_PICKER_USES_KEYBOARD, UserPreferences.DEFAULTS.datePickerUsesKeyboard
            ),
            timePickerUsesKeyboard = settings.getBoolean(
                KEY_TIME_PICKER_USES_KEYBOARD, UserPreferences.DEFAULTS.timePickerUsesKeyboard
            ),
            isCalendarExpanded = settings.getBoolean(
                KEY_CALENDAR_EXPANDED, UserPreferences.DEFAULTS.isCalendarExpanded
            ),
            useCraneCalendar = settings.getBoolean(
                KEY_USE_CRANE_CALENDAR, UserPreferences.DEFAULTS.useCraneCalendar
            ),
            localeTag = settings.getString(
                KEY_LOCALE_TAG, UserPreferences.DEFAULTS.localeTag
            ),
            usePocketBase = settings.getBoolean(
                KEY_USE_POCKETBASE, UserPreferences.DEFAULTS.usePocketBase
            ),
            pocketBaseUrl = settings.getString(
                KEY_POCKETBASE_URL, UserPreferences.DEFAULTS.pocketBaseUrl
            ),
            pocketBaseClientType = enumValueOf(
                settings.getString(
                    KEY_POCKETBASE_CLIENT_TYPE,
                    UserPreferences.DEFAULTS.pocketBaseClientType.name
                )
            )
        )
        _preferences.value = loadedPreferences
    }

    //endregion Load

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

    override suspend fun savePocketBaseClientType(pocketBaseClientType: PocketBaseClientType) {
        try {
            settings[KEY_POCKETBASE_CLIENT_TYPE] = pocketBaseClientType.name
            updatePreferences { it.copy(pocketBaseClientType = pocketBaseClientType) }
        } catch (e: Exception) {
            logger.e(throwable = e) { "Error saving pocketBaseClientType preference" }
        }
    }

    //endregion Save

    //region Update
    private fun updatePreferences(transform: (UserPreferences) -> UserPreferences) {
        _preferences.update(transform)
    }
    //endregion Update
}