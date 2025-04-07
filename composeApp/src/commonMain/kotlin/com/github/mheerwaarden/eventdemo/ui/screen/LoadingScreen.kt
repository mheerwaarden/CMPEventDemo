/*
 * Copyright (c) 2025. Marcel Van Heerwaarden
 * @Author Marcel van Heerwaarden
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.github.mheerwaarden.eventdemo.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.mheerwaarden.eventdemo.Dimensions
import com.github.mheerwaarden.eventdemo.resources.Res
import com.github.mheerwaarden.eventdemo.resources.error
import com.github.mheerwaarden.eventdemo.resources.ic_broken_image
import com.github.mheerwaarden.eventdemo.resources.loading
import com.github.mheerwaarden.eventdemo.resources.retry
import com.github.mheerwaarden.eventdemo.resources.unknown_error
import com.github.mheerwaarden.eventdemo.ui.components.ProgressIndicator
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun LoadingScreen(
    loadingViewModel: LoadingViewModel,
    modifier: Modifier = Modifier,
    successContent: @Composable () -> Unit,
) {
    val msgPrefix = "LoadingScreen for ${loadingViewModel::class.simpleName}:"
    when (val result = loadingViewModel.loadingState) {
        is LoadingState.Loading -> {
            /* Show progress indicator */
            ProgressScreen(action = Res.string.loading, modifier = modifier)
        }

        is LoadingState.Success -> {
            /* UiState is updated successfully, display data */
            successContent()
        }

        is LoadingState.Failure -> {
            /* Handle error */
            ErrorScreen(
                message = result.error.message ?: stringResource(Res.string.unknown_error),
                retryAction = { loadingViewModel.load() },
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
            )
        }
    }
}

@Composable
fun ProgressScreen(action: StringResource, modifier: Modifier = Modifier) {
    ProgressIndicator(
        action = action,
        modifier = modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center)
    )
}

@Composable
fun ErrorScreen(message: String, retryAction: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Image(
            painter = painterResource(resource = Res.drawable.ic_broken_image),
            contentDescription = stringResource(Res.string.error),
            modifier = Modifier.size(Dimensions.error_image_size)
        )
        Text(text = message, modifier = Modifier.padding(16.dp))
        Button(onClick = retryAction) {
            Text(stringResource(Res.string.retry))
        }
    }
}
