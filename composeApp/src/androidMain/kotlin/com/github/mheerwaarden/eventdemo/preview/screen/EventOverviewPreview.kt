package com.github.mheerwaarden.eventdemo.preview.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.github.mheerwaarden.eventdemo.ui.screen.event.EventOverviewScreenPreview
import com.github.mheerwaarden.eventdemo.ui.screen.event.EventOverviewScreenReadOnlyPreview

@Preview
@Composable
fun EventOverviewPreview() {
    EventOverviewScreenPreview()
}

@Preview
@Composable
fun EventOverviewReadOnlyPreview() {
    EventOverviewScreenReadOnlyPreview()
}