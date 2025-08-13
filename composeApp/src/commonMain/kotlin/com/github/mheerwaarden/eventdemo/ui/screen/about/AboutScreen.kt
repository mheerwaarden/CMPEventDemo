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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.github.mheerwaarden.eventdemo.AppInfo
import com.github.mheerwaarden.eventdemo.Dimensions
import com.github.mheerwaarden.eventdemo.PlatformAppInfo
import com.github.mheerwaarden.eventdemo.getPlatformInfo
import com.github.mheerwaarden.eventdemo.resources.Res
import com.github.mheerwaarden.eventdemo.resources.about
import com.github.mheerwaarden.eventdemo.resources.about_event_demo
import com.github.mheerwaarden.eventdemo.resources.app_id
import com.github.mheerwaarden.eventdemo.resources.platform
import com.github.mheerwaarden.eventdemo.resources.version
import com.github.mheerwaarden.eventdemo.ui.navigation.NavigationDestination
import com.github.mheerwaarden.eventdemo.ui.theme.EventDemoAppTheme
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

object AboutDestination : NavigationDestination {
    override val route = "about"
    override val titleRes = Res.string.about
}

@Composable
fun AboutScreen(
    modifier: Modifier = Modifier,
    onUpdateTopAppBar: (String, (() -> Unit)?, @Composable (RowScope.() -> Unit)) -> Unit = { _, _, _ -> },
    appInfo: AppInfo = koinInject(),
) {
    onUpdateTopAppBar(stringResource(AboutDestination.titleRes), null) {}

    Column(
        modifier = modifier
    ) {
        Text(
            text = stringResource(Res.string.about_event_demo),
            style = MaterialTheme.typography.titleLarge,
        )
        PlatformDetailRow(Res.string.version, "${appInfo.versionName} (${appInfo.versionCode})")
        PlatformDetailRow(Res.string.app_id, appInfo.appId)
        PlatformDetailRow(Res.string.platform, getPlatformInfo().name)
        Spacer(modifier = Modifier.height(Dimensions.padding_medium))
        Text(
            text = "The event app is a demo project to show how to build an app that runs on different platforms."
        )
    }

}

@Composable
private fun PlatformDetailRow(
    labelResId: StringResource, detail: String?, modifier: Modifier = Modifier,
) {
    if (detail.isNullOrBlank()) return
    Row(modifier = modifier.fillMaxWidth()) {
        Text(stringResource(labelResId), fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.weight(1f))
        Text(text = detail)
    }
}

@Preview
@Composable
fun AboutScreenPreview(
) {
    EventDemoAppTheme {
        AboutScreen(
            appInfo = PlatformAppInfo(
                appId = "com.github.mheerwaarden.eventdemo",
                versionName = "1.0.0",
                versionCode = "1"
            )
        )
    }
}