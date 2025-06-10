package com.github.mheerwaarden.eventdemo.i18n

import androidx.lifecycle.viewModelScope
import com.github.mheerwaarden.eventdemo.data.preferences.DEFAULT_LOCALE
import com.github.mheerwaarden.eventdemo.data.preferences.DEFAULT_LOCALE_FROM_PLATFORM
import com.github.mheerwaarden.eventdemo.data.preferences.UserPreferences
import com.github.mheerwaarden.eventdemo.data.preferences.UserPreferencesRepository
import com.github.mheerwaarden.eventdemo.localization.PlatformLocaleManager
import com.github.mheerwaarden.eventdemo.ui.screen.LoadingViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Platform-specific function to get the current system's locale tag (e.g., "en-US", "fr-CA").
 * This function will be implemented by each platform (`actual` implementation).
 */
expect fun getPlatformSystemLocaleTag(): String?

/**
 * Platform-specific function to apply the chosen locale tag to the application's environment.
 * Passing null will result in using the system's default locale.
 * Passing an invalid locale tag will not change the current locale of the system.
 * On Android, this would call AppCompatDelegate.setApplicationLocales().
 * On Web, this might involve a page reload or other framework-specific actions.
 * On other platforms, it might set the default locale or do nothing if not applicable.
 *
 * @param localeTag The BCP 47 language tag to apply. If null, it implies that the application
 *                  should revert to using the system's default locale. An invalid tag will be
 *                  ignored.
 */
expect fun applyPlatformLocale(localeTag: String?)

/**
 * ViewModel responsible for managing the application's locale state and preferences.
 * It interacts with the UserPreferencesRepository and platform-specific functions.
 */
class LocaleViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
) : LoadingViewModel(), KoinComponent {
    private val platformLocaleManager: PlatformLocaleManager by inject()

    /**
     * A StateFlow representing the currently effective application locale tag.
     * This will be either the user's preferred language (read from settings)
     * or the system locale tag if the preference is set to "System".
     *
     * This is updated when setPreferredAppLocale is called.
     *
     * Changes to this StateFlow trigger recomposition in Composables
     * observing it, causing resources like stringResource and
     * painterResource to be resolved for the new locale.
     */
    var preferredLocaleState: StateFlow<String> = MutableStateFlow(DEFAULT_LOCALE)

    override suspend fun loadState() {
        try {
            // 1. Suspend while fetching the first preference value to ensure initial state is correct.
            val initialPreferences = userPreferencesRepository.preferences.first()

            // 2. Change the platform locale to the user's preferred locale.
            val initialLocale = getEffectiveLocale(initialPreferences)
            setPreferredAppLocale(initialLocale) //(getInitialLocale())

            // 3. Set up the ongoing StateFlow to listen for subsequent changes
            preferredLocaleState = userPreferencesRepository.preferences
                .map { preferences -> getEffectiveLocale(preferences) }
                .distinctUntilChanged()
                .stateIn(
                    scope = viewModelScope,
                    started = WhileSubscribed(TIMEOUT_MILLIS),
                    initialValue = initialLocale
                )
        } catch (e: Throwable) { // Catch Throwable to see everything
            println("LocaleViewModel: Exception in loadState: ${e::class.simpleName} - ${e.message}")
            println("LocaleViewModel: Stack trace for developer: \n${e.stackTraceToString()}")
            throw e
        }
    }

    private fun getEffectiveLocale(preferences: UserPreferences) =
        if (preferences.localeTag == DEFAULT_LOCALE_FROM_PLATFORM) {
            platformLocaleManager.getPlatformLocaleTag() ?: DEFAULT_LOCALE
        } else {
            preferences.localeTag
        }

    /**
     * Sets the user's preferred application locale.
     * This updates the preference in the repository and triggers the platform-specific
     * function to apply the locale.
     *
     * @param localeTag The BCP 47 language tag to set as preferred.
     *                  Passing null, an empty string, or "System" will result in
     *                  using the system's default locale.
     */
    fun setPreferredAppLocale(localeTag: String?) {
        viewModelScope.launch {
            // Save the preference to the repository.
            userPreferencesRepository.saveLocalePreference(
                if (localeTag.isNullOrBlank()) DEFAULT_LOCALE_FROM_PLATFORM else localeTag
            )

            // Apply the locale to the platform.
            // If the preference is now "System", then the platform should be told to use its
            // system default, which is represented by "null". Otherwise, apply the specific
            // chosen locale.
            platformLocaleManager.setPlatformLocale(
                if (localeTag.isNullOrBlank() || localeTag == DEFAULT_LOCALE_FROM_PLATFORM) null else localeTag
            )
        }
    }

}