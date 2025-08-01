/*
 * Copyright 2022 The Android Open Source Project
 * Copyright (c) 2025. Marcel Van Heerwaarden
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.mheerwaarden.eventdemo.ui.components.cranecalendar

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.github.mheerwaarden.eventdemo.localization.LocalizedFormatter
import com.github.mheerwaarden.eventdemo.localization.NameStyle
import com.github.mheerwaarden.eventdemo.ui.components.cranecalendar.model.CalendarState
import com.github.mheerwaarden.eventdemo.ui.components.cranecalendar.model.CalendarUiState
import com.github.mheerwaarden.eventdemo.ui.components.cranecalendar.model.Month
import com.github.mheerwaarden.eventdemo.ui.theme.EventDemoAppTheme
import com.github.mheerwaarden.eventdemo.util.now
import com.github.mheerwaarden.eventdemo.util.previousOrSame
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.plus
import org.jetbrains.compose.ui.tooling.preview.Preview

/** Calendar based on the Crane sample from the Android Open Source Project */
@Composable
fun Calendar(
    calendarState: CalendarState,
    onDayClicked: (date: LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues()
) {
    val calendarUiState = calendarState.calendarUiState.value
    val numberSelectedDays = calendarUiState.numberSelectedDays.toInt()

    val selectedAnimationPercentage = remember(numberSelectedDays) {
        Animatable(0f)
    }
    // Start a Launch Effect when the number of selected days change.
    // using .animateTo() we animate the percentage selection from 0f - 1f
    LaunchedEffect(numberSelectedDays) {
        if (calendarUiState.hasSelectedDates) {
            val animationSpec: TweenSpec<Float> = tween(
                durationMillis =
                    (numberSelectedDays.coerceAtLeast(0) * DURATION_MILLIS_PER_DAY)
                        .coerceAtMost(2000),
                easing = EaseOutQuart
            )
            selectedAnimationPercentage.animateTo(
                targetValue = 1f,
                animationSpec = animationSpec
            )
        }
    }

    val selectedStartDate = calendarState.calendarUiState.value.selectedStartDate

    // Calculate the initial scroll index
    val initialVisibleMonthIndex = remember(selectedStartDate) {
        val startDate = selectedStartDate ?: now().date
        // Find the index of the month that matches the selected start date's month and year
        val startIndex = calendarState.listMonths.indexOfFirst {
            it.yearMonth.year == startDate.year && it.yearMonth.monthNumber == startDate.monthNumber
        }
        // Each call to itemsCalendarMonth adds items: header, daysOfWeek, weeks, spacer.
        // The items are added sequentially for each month in listMonths.
        // The index of the header for the i-th month (0-indexed in listMonths) is the sum of the item counts of the previous i - 1 months.
        // Number of items for month j = 1 (header) + 1 (daysOfWeek) + listMonths[j].weeks.size = 2 + listMonths[j].weeks.size
        var headerIndex = 0
        for (i in 0 until startIndex) {
            headerIndex += 2 + calendarState.listMonths[i].weeks.size
        }
        // This is the index of the header for the start month
        headerIndex
    }

    val lazyListState =
        rememberLazyListState(initialFirstVisibleItemIndex = initialVisibleMonthIndex)
    Surface(modifier = modifier.background(Color.Transparent)) {
        LazyColumn(
            state = lazyListState,
            modifier = modifier.consumeWindowInsets(contentPadding),
            contentPadding = contentPadding
        ) {
            calendarState.listMonths.forEach { month ->
                itemsCalendarMonth(
                    calendarUiState = calendarUiState,
                    onDayClicked = onDayClicked,
                    selectedPercentageProvider = { selectedAnimationPercentage.value },
                    month = month
                )
            }

            item(key = "bottomSpacer") {
                Spacer(
                    modifier = Modifier.windowInsetsBottomHeight(
                        WindowInsets.navigationBars
                    )
                )
            }
        }
    }
}

private fun LazyListScope.itemsCalendarMonth(
    calendarUiState: CalendarUiState,
    onDayClicked: (LocalDate) -> Unit,
    selectedPercentageProvider: () -> Float,
    month: Month
) {
    item(month.yearMonth.month.name + month.yearMonth.year + "header") {
        MonthHeader(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.surfaceVariant)
                .padding(start = 32.dp, end = 32.dp, top = 16.dp),
            month = MonthNames(LocalizedFormatter.dateTimeFormatter.localizedMonthNames(NameStyle.FULL))
                .names[month.yearMonth.monthNumber - 1],
            year = month.yearMonth.year.toString()
        )
    }

    // Expanding width and centering horizontally
    val contentModifier = Modifier
        .fillMaxWidth()
        .wrapContentWidth(Alignment.CenterHorizontally)
    item(month.yearMonth.month.name + month.yearMonth.year + "daysOfWeek") {
        DaysOfWeek(modifier = contentModifier)
    }

    // A custom key needs to be given to these items so that they can be found in tests that
    // need scrolling. The format of the key is ${year/month/weekNumber}. Thus,
    // the key for the fourth week of December 2020 is "2020/12/4"
    itemsIndexed(month.weeks, key = { index, _ ->
        month.yearMonth.year.toString() +
                "/" +
                month.yearMonth.monthNumber +
                "/" +
                (index + 1).toString()
    }) { _, week ->
        val beginningWeek = week.yearMonth.atDay(1).plus(week.number, DateTimeUnit.WEEK)
        val currentDay = beginningWeek.previousOrSame(DayOfWeek.MONDAY)

        if (calendarUiState.hasSelectedPeriodOverlap(
                currentDay,
                currentDay.plus(6, DateTimeUnit.WEEK)
            )
        ) {
            WeekSelectionPill(
                state = calendarUiState,
                currentWeekStart = currentDay,
                widthPerDay = CELL_SIZE,
                week = week,
                selectedPercentageTotalProvider = selectedPercentageProvider
            )
        }
        Week(
            calendarUiState = calendarUiState,
            modifier = contentModifier,
            week = week,
            onDayClicked = onDayClicked
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Preview
@Composable
fun DayPreview() {
    EventDemoAppTheme {
        Calendar(
            CalendarState(),
            onDayClicked = { },
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Gray) // showBackground = true
        )
    }
}
