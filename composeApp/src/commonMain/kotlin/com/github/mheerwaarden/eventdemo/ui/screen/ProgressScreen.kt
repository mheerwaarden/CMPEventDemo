package com.github.mheerwaarden.eventdemo.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.github.mheerwaarden.eventdemo.ui.components.ProgressIndicator
import org.jetbrains.compose.resources.StringResource

@Composable
fun ProgressScreen(action: StringResource, modifier: Modifier = Modifier, name: String) {
    ProgressIndicator(
        modifier.fillMaxSize().wrapContentSize(Alignment.Center),
        action, name
    )
}