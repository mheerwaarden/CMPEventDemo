/*
 * Copyright (c) 2025. Marcel Van Heerwaarden
 * @Author Marcel van Heerwaarden
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.github.mheerwaarden.eventdemo.ui.screen.about

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.github.mheerwaarden.eventdemo.resources.Res
import com.github.mheerwaarden.eventdemo.resources.about
import com.github.mheerwaarden.eventdemo.resources.about_event_demo
import com.github.mheerwaarden.eventdemo.ui.navigation.NavigationDestination
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

object AboutDestination : NavigationDestination {
    override val route = "about"
    override val titleRes = Res.string.about
}

@Preview
@Composable
fun AboutScreen(
    onUpdateTopAppBar: (String, @Composable (RowScope.() -> Unit)) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
) {
    onUpdateTopAppBar(stringResource(AboutDestination.titleRes)) {}

    Column(
        modifier = modifier
    ) {
        Text(
            text = stringResource(Res.string.about_event_demo),
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = "The event app is a demo project to show how to build an app that runs on different platforms."
        )
    }
}