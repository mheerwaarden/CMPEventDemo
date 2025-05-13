package com.github.mheerwaarden.eventdemo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.github.mheerwaarden.eventdemo.resources.Res
import com.github.mheerwaarden.eventdemo.resources.cancel
import com.github.mheerwaarden.eventdemo.resources.ok
import com.github.mheerwaarden.eventdemo.resources.select_start_end_date
import com.github.mheerwaarden.eventdemo.ui.components.cranecalendar.Calendar
import com.github.mheerwaarden.eventdemo.ui.components.cranecalendar.CraneCalendarViewModel
import com.github.mheerwaarden.eventdemo.ui.components.cranecalendar.model.CalendarState
import com.github.mheerwaarden.eventdemo.ui.theme.EventDemoAppTheme
import com.github.mheerwaarden.eventdemo.util.now
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ShowCraneCalendarDialog(
    startDate: LocalDate? = null,
    endDate: LocalDate? = null,
    onDateChange: (LocalDate?, LocalDate?) -> Unit,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {},
    craneCalendarViewModel: CraneCalendarViewModel = CraneCalendarViewModel(
        startDate = startDate,
        endDate = endDate
    )
) {
    CraneCalendarDialog(
        calendarState = craneCalendarViewModel.calendarState,
        onDateChange = onDateChange,
        modifier = modifier,
        onDaySelected = craneCalendarViewModel::onDaySelected,
        onDismiss = onDismiss
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CraneCalendarDialog(
    calendarState: CalendarState,
    onDateChange: (LocalDate?, LocalDate?) -> Unit,
    modifier: Modifier = Modifier,
    onDaySelected: (date: LocalDate) -> Unit,
    onDismiss: () -> Unit = {},
) {
    BasicAlertDialog(
        properties = DialogProperties(usePlatformDefaultWidth = true),
        modifier = modifier,
        onDismissRequest = onDismiss,
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceTint,
        ) {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.surfaceTint,
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = stringResource(Res.string.select_start_end_date),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) { contentPadding ->
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Calendar(
                        calendarState = calendarState,
                        onDayClicked = { dateClicked ->
                            onDaySelected(dateClicked)
                        },
                        modifier = Modifier.weight(1f),
                        contentPadding = contentPadding
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text(
                                stringResource(Res.string.cancel),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        TextButton(
                            onClick = {
                                val calendarUiState = calendarState.calendarUiState.value
                                onDateChange(calendarUiState.selectedStartDate, calendarUiState.selectedEndDate)
                            },
                        ) {
                            Text(
                                stringResource(Res.string.ok),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}


@Preview
@Composable
fun CraneCalendarDialogPreview() {
    EventDemoAppTheme {
        val startDate = now().date
        val endDate = startDate.plus(2, DateTimeUnit.DAY)
        CraneCalendarDialog(
            calendarState = CalendarState(startDate, endDate),
            onDateChange = { _, _ -> },
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Gray),
            onDaySelected = { },
        )
    }
}