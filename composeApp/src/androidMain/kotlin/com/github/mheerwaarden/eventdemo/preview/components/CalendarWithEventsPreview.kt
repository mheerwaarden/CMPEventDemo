package com.github.mheerwaarden.eventdemo.preview.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.github.mheerwaarden.eventdemo.ui.components.calendar.CalendarWithEventsScreenPreview


@Preview(showBackground = true, group = "Preview")
@Composable
fun CalendarWithEventsPreview() {
    CalendarWithEventsScreenPreview()
}

@Preview(showBackground = true, group = "Preview", widthDp = 800, heightDp = 600)
@Composable
fun CalendarWithEventsLandscapePreview() {
    CalendarWithEventsScreenPreview(isHorizontal = true)
}