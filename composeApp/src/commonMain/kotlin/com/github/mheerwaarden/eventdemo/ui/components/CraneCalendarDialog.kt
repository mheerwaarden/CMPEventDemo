package com.github.mheerwaarden.eventdemo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.plus
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ShowCraneCalendarDialog(
    startDate: LocalDateTime? = null,
    endDate: LocalDateTime? = null,
    onDateChange: (LocalDateTime?, LocalDateTime?) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceTint,
    onDismiss: () -> Unit = {},
    craneCalendarViewModel: CraneCalendarViewModel =
        CraneCalendarViewModel(
            startDate = startDate?.date,
            endDate = endDate?.date
        )
) {
    val calendarState = remember {
        craneCalendarViewModel.calendarState
    }
    CraneCalendarDialog(
        startDate = startDate,
        endDate = endDate,
        calendarState = calendarState,
        onDateChange = onDateChange,
        modifier = modifier,
        containerColor = containerColor,
        onDaySelected = craneCalendarViewModel::onDaySelected,
        onDismiss = onDismiss
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CraneCalendarDialog(
    startDate: LocalDateTime? = null,
    endDate: LocalDateTime? = null,
    calendarState: CalendarState,
    onDateChange: (LocalDateTime?, LocalDateTime?) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceTint,
    onDaySelected: (date: LocalDate) -> Unit,
    onDismiss: () -> Unit = {},
) {
    var showDialog by remember { mutableStateOf(true) }
    if (showDialog) {
        BasicAlertDialog(
            properties = DialogProperties(usePlatformDefaultWidth = true),
            modifier = modifier,
            onDismissRequest = {
                onDismiss()
                showDialog = false
            },
        ) {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = containerColor,
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    TopAppBar(
                        title = { Text(
                            text = stringResource(Res.string.select_start_end_date),
                            color = MaterialTheme.colorScheme.onPrimary
                        ) },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = containerColor,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        expandedHeight = TopAppBarDefaults.TopAppBarExpandedHeight
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Calendar(
                        calendarState = calendarState,
                        onDayClicked = { dateClicked ->
                            onDaySelected(dateClicked)
                        },
                        modifier = Modifier.weight(1f).background(color = containerColor)
                    )
                    HorizontalDivider(color = Color.Gray, modifier = Modifier.padding(vertical = 4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = {
                            onDismiss()
                            showDialog = false
                        }) {
                            Text(stringResource(Res.string.cancel),
                                color = MaterialTheme.colorScheme.onPrimary)
                        }
                        TextButton(
                            onClick = {
                                val calendarUiState = calendarState.calendarUiState.value
                                val selectedStartDate =
                                    if (calendarUiState.selectedStartDate == null) {
                                        null
                                    } else {
                                        LocalDateTime(
                                            calendarUiState.selectedStartDate,
                                            LocalTime(startDate?.hour ?: 0, startDate?.minute ?: 0)
                                        )
                                    }
                                val selectedEndDate = if (calendarUiState.selectedEndDate == null) {
                                    null
                                } else {
                                    LocalDateTime(
                                        calendarUiState.selectedEndDate,
                                        LocalTime(endDate?.hour ?: 0, endDate?.minute ?: 0)
                                    )
                                }
                                onDateChange(selectedStartDate, selectedEndDate)
                                showDialog = false
                            },
                        ) {
                            Text(stringResource(Res.string.ok),
                                color = MaterialTheme.colorScheme.onPrimary)
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
        val startDate = now()
        val endDate = LocalDateTime(startDate.date.plus(2, DateTimeUnit.DAY), startDate.time)
        CraneCalendarDialog(
            startDate = startDate,
            endDate = endDate,
            calendarState = CalendarState(startDate.date, endDate.date),
            onDateChange = { _, _ -> },
            onDaySelected = { },
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Gray),
        )
    }
}