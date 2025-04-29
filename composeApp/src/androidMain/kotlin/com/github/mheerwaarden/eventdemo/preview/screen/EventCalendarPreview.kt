package com.github.mheerwaarden.eventdemo.preview.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.github.mheerwaarden.eventdemo.ui.screen.event.EventCalendarLandscapeScreenPreview
import com.github.mheerwaarden.eventdemo.ui.screen.event.EventCalendarScreenPreview

@Preview(showBackground = true)
@Composable
fun EventCalendarPreview() {
    EventCalendarScreenPreview()
}

@Preview(showBackground = true, widthDp = 800, heightDp = 600)
@Composable
fun EventCalendarLandscapePreview() {
    EventCalendarLandscapeScreenPreview()
}