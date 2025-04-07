/*
 * Copyright (c) 2025. Marcel Van Heerwaarden
 * @Author Marcel van Heerwaarden
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.github.mheerwaarden.eventdemo.ui.navigation

import androidx.navigation.NavHostController
import com.github.mheerwaarden.eventdemo.ui.screen.about.AboutDestination
import com.github.mheerwaarden.eventdemo.ui.screen.settings.SettingsDestination

interface MenuNavigator {
    fun navigateToSettings()
    fun navigateToAbout()
}

class MenuNavigatorImpl(private val navController: NavHostController) : MenuNavigator {
    override fun navigateToSettings() {
        navController.navigate(SettingsDestination.route)
    }

    override fun navigateToAbout() {
        navController.navigate(AboutDestination.route)
    }
}