/*
 * Copyright (c) 2025. Marcel Van Heerwaarden
 * @Author Marcel van Heerwaarden
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.github.mheerwaarden.eventdemo.ui.navigation

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.github.mheerwaarden.eventdemo.ui.screen.about.AboutDestination
import com.github.mheerwaarden.eventdemo.ui.screen.about.AboutScreen
import com.github.mheerwaarden.eventdemo.ui.screen.event.EventCalendarDestination
import com.github.mheerwaarden.eventdemo.ui.screen.event.EventCalendarScreen
import com.github.mheerwaarden.eventdemo.ui.screen.event.EventDestination
import com.github.mheerwaarden.eventdemo.ui.screen.event.EventEditDestination
import com.github.mheerwaarden.eventdemo.ui.screen.event.EventEditScreen
import com.github.mheerwaarden.eventdemo.ui.screen.event.EventEntryDestination
import com.github.mheerwaarden.eventdemo.ui.screen.event.EventEntryScreen
import com.github.mheerwaarden.eventdemo.ui.screen.event.EventOverviewDestination
import com.github.mheerwaarden.eventdemo.ui.screen.event.EventOverviewScreen
import com.github.mheerwaarden.eventdemo.ui.screen.event.EventScreen
import com.github.mheerwaarden.eventdemo.ui.screen.settings.SettingsDestination
import com.github.mheerwaarden.eventdemo.ui.screen.settings.SettingsScreen
import com.github.mheerwaarden.eventdemo.util.format
import org.koin.compose.koinInject

/**
 * Provides Navigation graph for the application.
 */
@Composable
fun EventDemoAppNavHost(
    navController: NavHostController,
    isHorizontalLayout: Boolean,
    onUpdateTopAppBar: (String, (() -> Unit)?, @Composable (RowScope.() -> Unit)) -> Unit,
    modifier: Modifier = Modifier,
    startDestination: String = EventOverviewDestination.route,
) {
    NavHost(
        navController = navController, startDestination = startDestination, modifier = modifier
    ) {
        /* Event Screens */
        composable(route = EventOverviewDestination.route) {
            EventOverviewScreen(
                onUpdateTopAppBar = onUpdateTopAppBar,
                navigateToEvent = { id -> navController.navigate("${EventDestination.route}/${id}") },
                navigateToAddEvent = { date -> navController.navigate("${EventEntryDestination.route}/${date.format()}") },
                navigateToEditEvent = { id -> navController.navigate("${EventEditDestination.route}/${id}") },
                navigateToEventCalendar = { navController.navigate(EventCalendarDestination.route) },
                modifier = Modifier.fillMaxSize()
            )
        }
        composable(route = EventCalendarDestination.route) {
            EventCalendarScreen(
                onUpdateTopAppBar = onUpdateTopAppBar,
                navigateToEvent = { id -> navController.navigate("${EventDestination.route}/${id}") },
                navigateToEventOverview = { navController.navigate(EventOverviewDestination.route) },
                isHorizontal = isHorizontalLayout,
                modifier = Modifier.fillMaxSize(),
                settingsViewModel = koinInject()
            )
        }
        composable(
            route = EventDestination.routeWithArgs,
            arguments = listOf(navArgument(EventDestination.eventIdArg) {
                type = NavType.StringType
            })
        ) {
            EventScreen(
                onUpdateTopAppBar = onUpdateTopAppBar,
                navigateToEventOverview = { navController.navigate(EventOverviewDestination.route) },
                navigateToEditEvent = { id -> navController.navigate("${EventEditDestination.route}/${id}") },
                navigateBack = { navController.popBackStack() },
                modifier = Modifier.fillMaxSize(),
                settingsViewModel = koinInject()
            )
        }
        composable(
            route = EventEntryDestination.routeWithArgs,
            arguments = listOf(navArgument(EventEntryDestination.startDateArg) {
                type = NavType.StringType
            })
        ) {
            EventEntryScreen(
                onUpdateTopAppBar = onUpdateTopAppBar,
                isHorizontalLayout = isHorizontalLayout,
                navigateBack = { navController.popBackStack() },
                modifier = Modifier.fillMaxSize(),
                settingsViewModel = koinInject()
            )
        }
        composable(
            route = EventEditDestination.routeWithArgs,
            arguments = listOf(navArgument(EventEditDestination.eventIdArg) {
                type = NavType.StringType
            })
        ) {
            EventEditScreen(
                onUpdateTopAppBar = onUpdateTopAppBar,
                isHorizontalLayout = isHorizontalLayout,
                navigateBack = { navController.popBackStack() },
                modifier = Modifier.fillMaxSize(),
                settingsViewModel = koinInject()
            )
        }

        /* Menu screens */
        composable(route = SettingsDestination.route) {
            SettingsScreen(
                onUpdateTopAppBar = onUpdateTopAppBar,
                modifier = Modifier.fillMaxSize(),
                settingsViewModel = koinInject()
            )
        }
        composable(route = AboutDestination.route) {
            AboutScreen(
                onUpdateTopAppBar = onUpdateTopAppBar,
                modifier = Modifier.fillMaxSize()
            )
        }

    }
}
