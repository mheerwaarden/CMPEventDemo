package com.github.mheerwaarden.eventdemo.data.preferences

import co.touchlab.kermit.Logger
import co.touchlab.kermit.StaticConfig
import co.touchlab.kermit.platformLogWriter
import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class UserPreferencesSettingsRepositoryTest {

    // Use a TestDispatcher for testing coroutines
    private val testDispatcher = StandardTestDispatcher()

    // Use MapSettings for in-memory testing
    private lateinit var settings: Settings
    private lateinit var logger: Logger
    private lateinit var repository: UserPreferencesSettingsRepository

    @BeforeTest
    fun setup() {
        // Set the main dispatcher for coroutines
        Dispatchers.setMain(testDispatcher)
        // Initialize MapSettings
        settings = MapSettings()
        // Initialize logger
        logger = Logger(config = StaticConfig(logWriterList = listOf(platformLogWriter())), "Test")
        // Initialize the repository
        repository = UserPreferencesSettingsRepository(settings, logger)
    }

    @AfterTest
    fun tearDown() {
        // Reset the main dispatcher
        Dispatchers.resetMain()
    }

    @Test
    fun `initial preferences are default`() = runTest(testDispatcher) {
        // When
        val preferences = repository.preferences.first()

        // Then
        assertEquals(UserPreferences(), preferences)
    }

    @Test
    fun `saveReadOnlyPreference saves and updates flow`() = runTest(testDispatcher) {
        // Given
        val isReadOnly = true

        // When
        repository.saveReadOnlyPreference(isReadOnly)
        val preferences = repository.preferences.first()

        // Then
        assertEquals(isReadOnly, preferences.isReadOnly)
    }

    @Test
    fun `saveDatePickerUsesKeyboard saves and updates flow`() = runTest(testDispatcher) {
        // Given
        val useKeyboard = true

        // When
        repository.saveDatePickerUsesKeyboard(useKeyboard)
        val preferences = repository.preferences.first()

        // Then
        assertEquals(useKeyboard, preferences.datePickerUsesKeyboard)
    }

    @Test
    fun `saveTimePickerUsesKeyboard saves and updates flow`() = runTest(testDispatcher) {
        // Given
        val useKeyboard = true

        // When
        repository.saveTimePickerUsesKeyboard(useKeyboard)
        val preferences = repository.preferences.first()

        // Then
        assertEquals(useKeyboard, preferences.timePickerUsesKeyboard)
    }

    @Test
    fun `multiple saves update flow correctly`() = runTest(testDispatcher) {
        // Given
        val isReadOnly = true
        val datePickerUsesKeyboard = true
        val timePickerUsesKeyboard = true

        // When
        repository.saveReadOnlyPreference(isReadOnly)
        repository.saveDatePickerUsesKeyboard(datePickerUsesKeyboard)
        repository.saveTimePickerUsesKeyboard(timePickerUsesKeyboard)
        val preferences = repository.preferences.first()

        // Then
        assertEquals(isReadOnly, preferences.isReadOnly)
        assertEquals(datePickerUsesKeyboard, preferences.datePickerUsesKeyboard)
        assertEquals(timePickerUsesKeyboard, preferences.timePickerUsesKeyboard)
    }

    @Test
    fun `loadPreferences returns saved values`() = runTest(testDispatcher) {
        // Given
        val isReadOnly = true
        val datePickerUsesKeyboard = true
        val timePickerUsesKeyboard = true
        repository.saveReadOnlyPreference(isReadOnly)
        repository.saveDatePickerUsesKeyboard(datePickerUsesKeyboard)
        repository.saveTimePickerUsesKeyboard(timePickerUsesKeyboard)

        // When
        val newRepository = UserPreferencesSettingsRepository(settings, logger)
        val preferences = newRepository.preferences.first()

        // Then
        assertEquals(isReadOnly, preferences.isReadOnly)
        assertEquals(datePickerUsesKeyboard, preferences.datePickerUsesKeyboard)
        assertEquals(timePickerUsesKeyboard, preferences.timePickerUsesKeyboard)
    }
}