package com.github.mheerwaarden.eventdemo.preview.screen


import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.github.mheerwaarden.eventdemo.ui.components.CraneCalendarDialogPreview
import com.github.mheerwaarden.eventdemo.ui.components.cranecalendar.CalendarScreen
import com.github.mheerwaarden.eventdemo.ui.components.cranecalendar.CraneCalendarViewModel
import com.github.mheerwaarden.eventdemo.ui.components.cranecalendar.DayPreview
import com.github.mheerwaarden.eventdemo.ui.theme.EventDemoAppTheme
import com.github.mheerwaarden.eventdemo.util.now
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus

@Preview
@Composable
fun CraneCalendarPreview() {
    EventDemoAppTheme {
        val now = now()
        CalendarScreen(
            onBackPressed = { },
            craneCalendarViewModel = CraneCalendarViewModel(now.date, now.date.plus(2, DateTimeUnit.DAY))
        )
    }
}

@Preview
@Composable
fun CraneDayPreview() {
    DayPreview()
}

@Preview
@Composable
fun CraneDialogPreview() {
    CraneCalendarDialogPreview()
}
