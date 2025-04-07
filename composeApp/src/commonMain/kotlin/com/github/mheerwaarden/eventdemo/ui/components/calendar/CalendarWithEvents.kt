/*
 * Copyright (c) 2025. Marcel Van Heerwaarden
 * @Author Marcel van Heerwaarden
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.github.mheerwaarden.eventdemo.ui.components.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import com.github.mheerwaarden.eventdemo.Dimensions
import com.github.mheerwaarden.eventdemo.data.model.Event
import com.github.mheerwaarden.eventdemo.resources.Res
import com.github.mheerwaarden.eventdemo.resources.events
import com.github.mheerwaarden.eventdemo.resources.time
import com.github.mheerwaarden.eventdemo.util.INSTANT_DATETIME_FORMAT
import com.github.mheerwaarden.eventdemo.util.daysInMonth
import com.github.mheerwaarden.eventdemo.util.formatTime
import com.github.mheerwaarden.eventdemo.util.now
import com.github.mheerwaarden.eventdemo.util.toLocalDateTime
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.plus
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

private const val DAYS_IN_WEEK = 7
private const val NO_SELECTION = -2
private const val DAY_OUTSIDE_MONTH = -1
private const val MONTHS_IN_YEAR = 12

// Allow scrolling one year back and one year ahead
private const val INITIAL_PAGE = MONTHS_IN_YEAR + 1
private const val PAGE_COUNT = 2 * MONTHS_IN_YEAR * 1


/*
 * Based on the example on https://androidkaleempatel.medium.com/creating-a-monthly-calendar-view-with-event-indicators-in-jetpack-compose-4dffcf0359ca
 */
@Composable
fun CalendarWithEvents(
    events: List<Event>,
    setPeriod: (LocalDate, LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    startDate: LocalDate = now().date,
    actions: @Composable () -> Unit = {}
) {
    // MutableState for selected date events
    var selectedDateEvents by remember { mutableStateOf<List<Event>>(listOf()) }

    // UI Layout
    val scrollScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = INITIAL_PAGE, pageCount = { PAGE_COUNT })
    HorizontalPager(
        modifier = modifier.fillMaxSize(),
        state = pagerState,
        verticalAlignment = Alignment.Top
    ) { page ->
        val month = LocalDate(startDate.year, startDate.month, startDate.dayOfMonth)
        month.plus(page - INITIAL_PAGE, DateTimeUnit.MONTH)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimensions.padding_small)
        ) {
            // Calendar controls
            CalendarControls(month) {
                scrollScope.launch {
                    pagerState.scrollToPage(pagerState.currentPage + it)
                }
            }

            actions()

            // Calendar grid with events
            val daysInMonth = month.daysInMonth()
            val days = (1..daysInMonth).toList()

            CalendarGrid(events, setPeriod, month) { eventsForDay ->
                selectedDateEvents = eventsForDay
            }

            // Display events for selected date at the bottom
            EventList(selectedDateEvents)
        }
    }
}

@Composable
fun CalendarControls(currentMonth: LocalDate, toNextMonth: (Int) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        IconButton(onClick = { toNextMonth(-1) }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month")
        }
        // Display month and year
        Text(
            text = "${currentMonth.month.name} ${currentMonth.year}"
        )
        IconButton(onClick = { toNextMonth(1) }) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month")
        }
    }
}

@Composable
fun CalendarGrid(
    events: List<Event>,
    setPeriod: (LocalDate, LocalDate) -> Unit,
    currentMonth: LocalDate,
    onSelectDate: (List<Event>) -> Unit,
) {
    var selectedDay by rememberSaveable { mutableIntStateOf(NO_SELECTION) }
    val daysInMonth = currentMonth.daysInMonth()
    val firstDayOfMonth = LocalDate(currentMonth.year, currentMonth.month, 1)
    val lastDayOfMonth = LocalDate(currentMonth.year, currentMonth.month, daysInMonth)
    val startingDayOfWeek =
            firstDayOfMonth.dayOfWeek.isoDayNumber - 1 // Adjusting for zero-based indexing
    val days = (1..daysInMonth).toList()
    val paddingDaysBefore = List(startingDayOfWeek) { DAY_OUTSIDE_MONTH }
    val paddingDaysAfter =
            List((DAYS_IN_WEEK - (startingDayOfWeek + daysInMonth) % DAYS_IN_WEEK) % DAYS_IN_WEEK) {
                DAY_OUTSIDE_MONTH
            }
    val allDays = paddingDaysBefore + days + paddingDaysAfter
    setPeriod(firstDayOfMonth, lastDayOfMonth)

    val weekdays = listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")
    LazyVerticalGrid(columns = GridCells.Fixed(DAYS_IN_WEEK)) {
        items(DAYS_IN_WEEK) { index ->
            Text(
                text = weekdays[index],
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(Dimensions.padding_extra_small)
            )
        }
        items(allDays.size) { index ->
            val day = allDays[index]
            val textColor = if (day <= 0) Color.Transparent else Color.Black
            val currentDay = if (day <= 0) "" else day.toString()

            // Determine the events for this day
            val eventsForDay = events.filter { event ->
                val startDate = event.startInstant.toLocalDateTime()
                startDate.dayOfMonth == day && startDate.month == currentMonth.month
            }

            // Display dots for each event
            val coloredEventDots = buildAnnotatedString {
                eventsForDay.forEach { event ->
                    withStyle(style = SpanStyle(color = event.color, fontSize = MaterialTheme.typography.headlineLarge.fontSize)) {
                        append("• ")
                    }
                }
            }

            // Display the day and event dots
            Column(
                modifier = Modifier
                    .padding(Dimensions.padding_extra_small)
                    .clickable {
                        selectedDay = if (selectedDay == day) NO_SELECTION else day
                        // On day click, update selected date events
                        onSelectDate(eventsForDay)
                    }
                    .background(
                        if (selectedDay == day) MaterialTheme.colorScheme.surfaceContainerHighest else Color.Transparent
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center) {
                Text(
                    text = currentDay,
                    color = textColor,
                )
                Text(text = coloredEventDots)
            }
        }
    }
}

@Composable
fun EventList(events: List<Event>) {
    val titleText = stringResource(Res.string.events)
    val columnTimeWeight = .2f // 20%
    val columnDescriptionWeight = .6f // 60%
    LazyColumn(
        modifier = Modifier
            .padding(top = Dimensions.padding_small)
            .fillMaxWidth(),
    ) {
        item(key = titleText) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.background(color = MaterialTheme.colorScheme.surfaceTint)
            ) {
                val foregroundColor = MaterialTheme.colorScheme.surface
                Text(
                    text = stringResource(Res.string.time),
                    color = foregroundColor,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    modifier = Modifier.weight(columnTimeWeight)
                )
                Text(
                    text = titleText,
                    color = foregroundColor,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    modifier = Modifier.weight(columnDescriptionWeight)
                )
            }
        }
        itemsIndexed(
            items = events,
            key = { _, item -> "${item.getTypeNameResId()}_${item.id}" }
        ) { index, item ->
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = { /* navigateToEvent(item.id) */ })
                    .background(
                        color = if (index % 2 == 0) {
                            MaterialTheme.colorScheme.surfaceContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
            ) {
                Text(
                    text = item.startInstant.formatTime(),
                    modifier = Modifier.weight(columnTimeWeight)
                )
                Text(
                    text = item.endInstant.formatTime(),
                    modifier = Modifier.weight(columnTimeWeight)
                )
                Text(text = item.description, modifier = Modifier.weight(columnDescriptionWeight))
            }
        }
    }
}

@Preview
@Composable
fun CalendarWithEventsScreenPreview() {
    val events = listOf(
        Event(
            id = 1,
            startInstant = Instant.parse("2024-10-05 10:00", INSTANT_DATETIME_FORMAT),
            description = "date 1a"
        ),
        Event(
            id = 2,
            startInstant = Instant.parse("2024-10-05 10:00", INSTANT_DATETIME_FORMAT),
            description = "date 1b"
        ),
        Event(
            id = 3,
            startInstant = Instant.parse("2024-10-05 10:00", INSTANT_DATETIME_FORMAT),
            description = "date 1c"
        ),
        Event(
            id = 4,
            startInstant = Instant.parse("2024-10-05 10:00", INSTANT_DATETIME_FORMAT),
            description = "date 1c"
        ),
        Event(
            id = 5,
            startInstant = Instant.parse("2024-10-10 10:00", INSTANT_DATETIME_FORMAT),
            description = "date 2"
        ),
        Event(
            id = 6,
            startInstant = Instant.parse("2024-10-15 10:00", INSTANT_DATETIME_FORMAT),
            description = "date 3"
        ),
    )
    val startDate = LocalDate(2024, 10, 1)
    CalendarWithEvents(
        events = events,
        setPeriod = { _, _ -> },
        startDate = startDate,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray) // showBackground = true
    )
}