/*
 * Copyright (c) 2025. Marcel Van Heerwaarden
 * @Author Marcel van Heerwaarden
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.github.mheerwaarden.eventdemo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.github.mheerwaarden.eventdemo.resources.Res
import com.github.mheerwaarden.eventdemo.resources.about
import com.github.mheerwaarden.eventdemo.resources.app_name
import com.github.mheerwaarden.eventdemo.resources.back_button
import com.github.mheerwaarden.eventdemo.resources.close
import com.github.mheerwaarden.eventdemo.resources.more
import com.github.mheerwaarden.eventdemo.resources.preferences
import com.github.mheerwaarden.eventdemo.ui.localization.AppEnvironment
import com.github.mheerwaarden.eventdemo.ui.navigation.EventDemoAppNavHost
import com.github.mheerwaarden.eventdemo.ui.navigation.MenuNavigator
import com.github.mheerwaarden.eventdemo.ui.navigation.MenuNavigatorImpl
import com.github.mheerwaarden.eventdemo.ui.screen.LoadingScreen
import com.github.mheerwaarden.eventdemo.ui.screen.about.AboutDestination
import com.github.mheerwaarden.eventdemo.ui.pocketbaseservice.auth.AuthViewModel
import com.github.mheerwaarden.eventdemo.ui.pocketbaseservice.auth.LoginScreen
import com.github.mheerwaarden.eventdemo.ui.screen.event.EventOverviewDestination
import com.github.mheerwaarden.eventdemo.ui.pocketbaseservice.eventscreen.EventsScreen
import com.github.mheerwaarden.eventdemo.ui.pocketbaseservice.eventscreen.EventsViewModel
import com.github.mheerwaarden.eventdemo.ui.screen.settings.SettingsViewModel
import com.github.mheerwaarden.eventdemo.ui.theme.EventDemoAppTheme
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import com.github.mheerwaarden.eventdemo.data.pocketbaseservice.createPocketBaseService
import com.github.mheerwaarden.eventdemo.data.preferences.PocketBaseClientType


@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun EventDemoApp(
    modifier: Modifier = Modifier,
    startDestination: String = EventOverviewDestination.route,
) {
    val windowClass = calculateWindowSizeClass()
    val isHorizontalLayout = windowClass.widthSizeClass != WindowWidthSizeClass.Compact

    EventDemoAppTheme {
        AppEnvironment(localeViewModel = koinInject()) {
            println("Starting DemoChooser")
            DemoChooser(
                startDestination = startDestination,
                isHorizontalLayout = isHorizontalLayout,
                modifier = modifier,
                settingsViewModel = koinInject()
            )
        }
    }
}

@Composable
fun DemoChooser(
    startDestination: String,
    isHorizontalLayout: Boolean,
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel
) {
    println("DemoChooser started")
    LoadingScreen(loadingViewModel = settingsViewModel, modifier = modifier) {
        val preferences by settingsViewModel.settingsUiState.collectAsState()
        if (preferences.usePocketBase) {
            PocketBaseEvents(
                pocketBaseUrl = preferences.pocketBaseUrl,
                newPocketBaseClientType = preferences.pocketBaseClientType,
                modifier = modifier
            )
        } else {
            ThemedLocalizedApp(
                startDestination = startDestination,
                isHorizontalLayout = isHorizontalLayout,
                modifier = modifier
            )
        }
    }
}

@Composable
fun PocketBaseEvents(
    pocketBaseUrl: String,
    newPocketBaseClientType: PocketBaseClientType,
    modifier: Modifier = Modifier
) {
    val pocketBaseService = remember(key1 = pocketBaseUrl, key2 = newPocketBaseClientType) {
        println("PocketBaseEvents: Create new PocketBaseService with type $newPocketBaseClientType and url $pocketBaseUrl")
        createPocketBaseService(pocketBaseUrl, newPocketBaseClientType)
    }
    val authViewModel = remember(key1 = pocketBaseService) {
        AuthViewModel(pocketBaseService)
    }
    val eventsViewModel = remember(key1 = pocketBaseService) {
        EventsViewModel(pocketBaseService)
    }
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()

    if (isAuthenticated) {
        eventsViewModel.initializeAfterLogin()
        EventsScreen(eventsViewModel, modifier)
    } else {
        LoginScreen(authViewModel, modifier)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemedLocalizedApp(
    startDestination: String,
    isHorizontalLayout: Boolean,
    modifier: Modifier = Modifier
) {
    val appName = stringResource(resource = Res.string.app_name)
    var title by rememberSaveable { mutableStateOf(appName) }
    val updatedTitle by remember { derivedStateOf { title } }

    // Additional action icons shown on the top app bar
    val defaultAction: @Composable (RowScope.() -> Unit) = {}
    val actions = remember { mutableStateOf(defaultAction) }
    // When the screen shows a dialog, a close action must be provided.
    val defaultCloseAction = { }
    val closeAction = remember { mutableStateOf(defaultCloseAction) }

    val navController = rememberNavController()
    // The scroll state of the overview must not be applied to the other screens
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: startDestination
    val scrollBehavior = if (currentRoute == EventOverviewDestination.route) {
        TopAppBarDefaults.enterAlwaysScrollBehavior()
    } else {
        null
    }

    Scaffold(
        modifier = if (scrollBehavior != null) modifier.nestedScroll(scrollBehavior.nestedScrollConnection) else modifier,
        topBar = {
            EventDemoAppBar(
                menuNavigator = MenuNavigatorImpl(navController),
                title = updatedTitle,
                canNavigateBack = navController.previousBackStackEntry != null,
                closeDialog = if (closeAction.value == defaultCloseAction) null else closeAction.value,
                scrollBehavior = scrollBehavior,
                navigateUp = { navController.navigateUp() },
                actions = actions.value
            )
        },
    ) { innerPadding ->
        EventDemoAppNavHost(
            navController = navController,
            startDestination = startDestination,
            isHorizontalLayout = isHorizontalLayout,
            onUpdateTopAppBar = { newTitle, newCloseDialog, newActions ->
                title = newTitle
                closeAction.value = newCloseDialog ?: defaultCloseAction
                actions.value = newActions
            },
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        )
    }
}

/**
 * App bar to display title and conditionally display the back navigation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDemoAppBar(
    menuNavigator: MenuNavigator,
    scrollBehavior: TopAppBarScrollBehavior?,
    title: String,
    canNavigateBack: Boolean,
    closeDialog: (() -> Unit)?,
    modifier: Modifier = Modifier,
    navigateUp: () -> Unit = {},
    actions: @Composable (RowScope.() -> Unit) = {},
    colors: TopAppBarColors = TopAppBarDefaults.centerAlignedTopAppBarColors(),
) {
    // The expanded state of the dropdown menu.
    var expanded by remember { mutableStateOf(false) }

    CenterAlignedTopAppBar(
        title = { Text(title) },
        modifier = modifier,
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            if (closeDialog != null) {
                IconButton(onClick = {
                    closeDialog()
                    navigateUp()
                }) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(Res.string.close)
                    )
                }
            } else if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(Res.string.back_button)
                    )
                }
            }
        },
        actions = {
            actions()
            IconButton(onClick = { expanded = true }) {
                Icon(Icons.Filled.MoreVert, contentDescription = stringResource(Res.string.more))
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(Res.string.preferences)) },
                    onClick = { menuNavigator.navigateToSettings(); expanded = false }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(Res.string.about)) },
                    onClick = { menuNavigator.navigateToAbout(); expanded = false }
                )
            }
        },
        colors = colors
    )
}

@Preview
@Composable
fun EventDemoAppScreenPreview() {
    EventDemoAppTheme {
        EventDemoApp(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.LightGray),
            startDestination = AboutDestination.route // showBackground = true
        )
    }
}
