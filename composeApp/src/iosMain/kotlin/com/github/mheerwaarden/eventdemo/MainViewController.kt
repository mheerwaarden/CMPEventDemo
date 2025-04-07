package com.github.mheerwaarden.eventdemo

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() = ComposeUIViewController { EventDemoApp(modifier = Modifier.fillMaxSize()) }