package com.github.mheerwaarden.eventdemo.preview.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.github.mheerwaarden.eventdemo.ui.components.calendar.CalendarWithEventsScreenPreview
import com.github.mheerwaarden.eventdemo.ui.theme.EventDemoAppTheme


@Preview(showBackground = true, group = "Preview")
@Composable
fun CalendarWithEventsPreview() {
    EventDemoAppTheme {
        CalendarWithEventsScreenPreview()
    }
}

@Preview(showBackground = true, group = "Preview", widthDp = 800, heightDp = 600)
@Composable
fun CalendarWithEventsLandscapePreview() {
    EventDemoAppTheme {
        CalendarWithEventsScreenPreview(isHorizontal = true)
    }
}