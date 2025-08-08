/*
 * Copyright (c) 2025. Marcel Van Heerwaarden
 * @Author Marcel van Heerwaarden
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.github.mheerwaarden.eventdemo.ui.components.calendar

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import com.github.mheerwaarden.eventdemo.Dimensions
import com.github.mheerwaarden.eventdemo.data.model.Event
import com.github.mheerwaarden.eventdemo.localization.dayMonthFormat
import com.github.mheerwaarden.eventdemo.localization.fullMonthYearFormat
import com.github.mheerwaarden.eventdemo.localization.toLocalizedString
import com.github.mheerwaarden.eventdemo.resources.Res
import com.github.mheerwaarden.eventdemo.resources.events
import com.github.mheerwaarden.eventdemo.resources.friday
import com.github.mheerwaarden.eventdemo.resources.hide_calendar
import com.github.mheerwaarden.eventdemo.resources.monday
import com.github.mheerwaarden.eventdemo.resources.next_month
import com.github.mheerwaarden.eventdemo.resources.previous_month
import com.github.mheerwaarden.eventdemo.resources.saturday
import com.github.mheerwaarden.eventdemo.resources.show_calendar
import com.github.mheerwaarden.eventdemo.resources.sunday
import com.github.mheerwaarden.eventdemo.resources.thursday
import com.github.mheerwaarden.eventdemo.resources.time
import com.github.mheerwaarden.eventdemo.resources.tuesday
import com.github.mheerwaarden.eventdemo.resources.wednesday
import com.github.mheerwaarden.eventdemo.ui.util.HtmlColors
import com.github.mheerwaarden.eventdemo.util.daysInMonth
import com.github.mheerwaarden.eventdemo.util.now
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
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
private const val PAGE_COUNT = 2 * MONTHS_IN_YEAR + 1

/*
 * Based on the example on https://androidkaleempatel.medium.com/creating-a-monthly-calendar-view-with-event-indicators-in-jetpack-compose-4dffcf0359ca
 */
@Composable
fun CalendarWithEvents(
    events: List<Event>,
    setPeriod: (LocalDate, LocalDate) -> Unit,
    navigateToEvent: (String) -> Unit,
    modifier: Modifier = Modifier,
    startDate: LocalDate = now().date,
    actions: @Composable () -> Unit = {},
    isHorizontal: Boolean = false,
    isExpanded: Boolean = true,
    onExpand: (Boolean) -> Unit = {},
) {
    // MutableState for selected date
    var selectedDay by remember { mutableIntStateOf(startDate.dayOfMonth) }

    // UI Layout
    val scrollScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = INITIAL_PAGE, pageCount = { PAGE_COUNT })
    println("CalendarWithEvents: current page: ${pagerState.currentPage}, start date: $startDate")
    HorizontalPager(
        modifier = modifier.fillMaxSize(),
        state = pagerState,
        verticalAlignment = Alignment.Top
    ) { page ->
        val month = LocalDate(startDate.year, startDate.month, startDate.dayOfMonth)
        val currentMonth = month.plus(page - INITIAL_PAGE, DateTimeUnit.MONTH)

        val selectedDateEvents = getEventsForDay(
            events = events,
            day = selectedDay,
            currentMonth = currentMonth.monthNumber
        )

        if (isHorizontal) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Dimensions.padding_small)
            ) {
                // Display calendar header with controls and calendar body
                Calendar(
                    currentMonth = currentMonth,
                    scrollScope = scrollScope,
                    selectedDay = selectedDay,
                    pagerState = pagerState,
                    actions = actions,
                    events = events,
                    setPeriod = setPeriod,
                    onSelectDay = { day -> selectedDay = day },
                    isExpanded = isExpanded,
                    onExpand = onExpand,
                    modifier = Modifier.weight(1f)
                )

                // Display events for selected date at the bottom
                EventList(
                    selectedDay = selectedDay,
                    currentMonth = currentMonth,
                    events = selectedDateEvents,
                    navigateToEvent = navigateToEvent,
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Dimensions.padding_small)
            ) {
                // Display calendar header with controls and calendar body
                Calendar(
                    currentMonth = currentMonth,
                    scrollScope = scrollScope,
                    selectedDay = selectedDay,
                    pagerState = pagerState,
                    actions = actions,
                    events = events,
                    setPeriod = setPeriod,
                    onSelectDay = { day -> selectedDay = day },
                    isExpanded = isExpanded,
                    onExpand = onExpand,
                )

                // Display events for selected date at the bottom
                EventList(
                    selectedDay = selectedDay,
                    currentMonth = currentMonth,
                    events = selectedDateEvents,
                    navigateToEvent = navigateToEvent
                )
            }
        }
    }
}

@Composable
private fun Calendar(
    currentMonth: LocalDate,
    scrollScope: CoroutineScope,
    selectedDay: Int,
    pagerState: PagerState,
    actions: @Composable () -> Unit,
    events: List<Event>,
    setPeriod: (LocalDate, LocalDate) -> Unit,
    onSelectDay: (Int) -> Unit,
    modifier: Modifier = Modifier,
    isExpanded: Boolean = true,
    onExpand: (Boolean) -> Unit = {},
) {
    Column(modifier = modifier) {

        // Calendar controls
        CalendarControls(
            currentMonth = currentMonth,
            isExpanded = isExpanded,
            onExpand = onExpand
        ) {
            scrollScope.launch {
                val newMonth = currentMonth.plus(it, DateTimeUnit.MONTH)
                if (selectedDay > newMonth.daysInMonth()) onSelectDay(newMonth.daysInMonth())
                pagerState.scrollToPage(pagerState.currentPage + it)
            }
        }

        AnimatedVisibility(isExpanded) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                // Actions, like filter buttons
                actions()

                // Display the calendar grid
                CalendarGrid(
                    events,
                    setPeriod,
                    currentMonth,
                    selectedDay
                ) { day -> onSelectDay(day) }
            }
        }
    }
}

@Composable
fun CalendarControls(
    currentMonth: LocalDate,
    isExpanded: Boolean,
    onExpand: (Boolean) -> Unit,
    toNextMonth: (Int) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Button to scroll to previous month
        IconButton(onClick = { toNextMonth(-1) }) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(Res.string.previous_month)
            )
        }
        // Display month and year
        Text(
            text = currentMonth.format(LocalDate.fullMonthYearFormat())
        )
        // Button to scroll to next month and button to expand/collapse calendar
        Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { toNextMonth(1) }) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = stringResource(Res.string.next_month)
                )
            }

            val icon = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore
            val description = if (isExpanded) Res.string.hide_calendar else Res.string.show_calendar
            IconButton(onClick = { onExpand(!isExpanded) }) {
                Icon(
                    imageVector = icon,
                    contentDescription = stringResource(description),
                )
            }
        }
    }
}

@Composable
fun CalendarGrid(
    events: List<Event>,
    setPeriod: (LocalDate, LocalDate) -> Unit,
    currentMonth: LocalDate,
    selectedDay: Int,
    onSelectDate: (Int) -> Unit,
) {
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
    println("CalendarGrid: call setPeriod with $firstDayOfMonth, $lastDayOfMonth")
    setPeriod(firstDayOfMonth, lastDayOfMonth.plus(1, DateTimeUnit.DAY))

    val weekdays = listOf(
        stringResource(Res.string.sunday).take(2),
        stringResource(Res.string.monday).take(2),
        stringResource(Res.string.tuesday).take(2),
        stringResource(Res.string.wednesday).take(2),
        stringResource(Res.string.thursday).take(2),
        stringResource(Res.string.friday).take(2),
        stringResource(Res.string.saturday).take(2),
    )
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
            val eventsForDay =
                getEventsForDay(events, day, currentMonth.monthNumber).sortedBy { it.startDateTime }
            val dotTextStyle = TextStyle(
                fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                lineHeight = MaterialTheme.typography.bodyLarge.fontSize * .8f,
            )

            // Display dots for each event
            val coloredEventDots = buildAnnotatedString {
                eventsForDay.forEach { event ->
                    withStyle(
                        style = SpanStyle(
                            color = event.htmlColor.color,
                            fontSize = dotTextStyle.fontSize,
                        )
                    ) {
                        append("• ")
                    }
                }
            }

            // Display the day and event dots
            Column(
                modifier = Modifier
                    .padding(Dimensions.padding_extra_small)
                    .clickable {
                        // On day click, update selected date events
                        onSelectDate(if (selectedDay == day) NO_SELECTION else day)
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
                Text(
                    text = coloredEventDots,
                    style = LocalTextStyle.current.merge(dotTextStyle),
                )
            }
        }
    }
}

private fun getEventsForDay(events: List<Event>, day: Int, currentMonth: Int) =
    events.filter { event ->
        val startDate = event.startDateTime
        startDate.dayOfMonth == day && startDate.monthNumber == currentMonth
    }

@Composable
fun EventList(
    selectedDay: Int,
    currentMonth: LocalDate,
    events: List<Event>,
    navigateToEvent: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val titleText = stringResource(Res.string.events)
    val columnTimeWeight = .2f // 20%
    val columnDescriptionWeight = .6f // 60%
    val dayMonthFormat = LocalDate.dayMonthFormat()
    LazyColumn(
        modifier = modifier
            .padding(top = Dimensions.padding_small)
            .fillMaxWidth(),
    ) {
        item(key = titleText) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.background(color = MaterialTheme.colorScheme.surfaceTint)
            ) {
                val foregroundColor = MaterialTheme.colorScheme.surface
                val selectedDate =
                    LocalDate(currentMonth.year, currentMonth.monthNumber, selectedDay)
                Text(
                    text = stringResource(Res.string.time),
                    color = foregroundColor,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    modifier = Modifier.weight(columnTimeWeight * 2)
                )
                Text(
                    text = if (selectedDay > 0) {
                        "$titleText ${selectedDate.format(dayMonthFormat)}"
                    } else {
                        ""
                    },
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
                    .clickable(onClick = { navigateToEvent(item.id) })
                    .background(
                        color = if (index % 2 == 0) {
                            MaterialTheme.colorScheme.surfaceContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
            ) {
                Text(
                    text = item.startDateTime.time.toLocalizedString(),
                    modifier = Modifier.weight(columnTimeWeight)
                )
                Text(
                    text = item.endDateTime.time.toLocalizedString(),
                    modifier = Modifier.weight(columnTimeWeight)
                )
                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(columnDescriptionWeight)
                ) {
                    Text(
                        text = "•",
                        color = item.htmlColor.color,
                        fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                    )
                    Text(text = item.description)
                }
            }
        }
    }
}

@Preview
@Composable
fun CalendarWithEventsScreenPreview(isHorizontal: Boolean = false) {
    val events = listOf(
        Event(
            id = "1",
            startDateTime = LocalDateTime(2024, 10, 5, 10, 0),
            description = "date 1a"
        ),
        Event(
            id = "2",
            startDateTime = LocalDateTime(2024, 10, 5, 10, 0),
            description = "date 1b",
            htmlColor = HtmlColors.RED
        ),
        Event(
            id = "3",
            startDateTime = LocalDateTime(2024, 10, 5, 10, 0),
            description = "date 1c",
            htmlColor = HtmlColors.BLUE
        ),
        Event(
            id = "4",
            startDateTime = LocalDateTime(2024, 10, 5, 10, 0),
            description = "date 1d",
            htmlColor = HtmlColors.AQUAMARINE
        ),
        Event(
            id = "5",
            startDateTime = LocalDateTime(2024, 10, 10, 10, 0),
            description = "date 2",
            htmlColor = HtmlColors.VIOLET
        ),
        Event(
            id = "6",
            startDateTime = LocalDateTime(2024, 10, 15, 10, 0),
            description = "date 3",
            htmlColor = HtmlColors.PURPLE
        ),
    )
    val startDate = LocalDate(2024, 10, 5)
    CalendarWithEvents(
        events = events,
        startDate = startDate,
        setPeriod = { _, _ -> },
        navigateToEvent = {},
        isHorizontal = isHorizontal,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray) // showBackground = true
    )
}