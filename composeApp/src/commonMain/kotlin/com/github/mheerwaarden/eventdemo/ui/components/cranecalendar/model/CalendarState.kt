/*
 * Copyright 2022 The Android Open Source Project
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

package com.github.mheerwaarden.eventdemo.ui.components.cranecalendar.model

import androidx.compose.runtime.mutableStateOf
import com.github.mheerwaarden.eventdemo.util.now
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.periodUntil

class CalendarState(
    startDate: LocalDate? = null,
    endDate: LocalDate? = null,
) {
    val calendarUiState = mutableStateOf(
        CalendarUiState(
            selectedStartDate = startDate,
            selectedEndDate = endDate,
        )
    )
    val listMonths: List<Month>

    // Defaulting to starting at 1/01 of start year
    private val calendarStartDate: LocalDate = LocalDate(startDate?.year ?: now().year, 1, 1)

    // Defaulting to 2 years from end date.
    private val calendarEndDate: LocalDate =
            LocalDate((endDate?.year ?: calendarStartDate.year) + 2, 12, 31)

    private val periodBetweenCalendarStartEnd: DatePeriod =
            calendarStartDate.periodUntil(calendarEndDate)

    init {
        val tempListMonths = mutableListOf<Month>()
        var startYearMonth = YearMonth.from(calendarStartDate)
        for (numberMonth in 0..periodBetweenCalendarStartEnd.toTotalMonths()) {
            val numberWeeks = startYearMonth.getNumberWeeks()
            val listWeekItems = mutableListOf<Week>()
            for (week in 0 until numberWeeks) {
                listWeekItems.add(
                    Week(
                        number = week,
                        yearMonth = startYearMonth
                    )
                )
            }
            val month = Month(startYearMonth, listWeekItems)
            tempListMonths.add(month)
            startYearMonth = startYearMonth.plusMonths(1)
        }
        listMonths = tempListMonths.toList()
    }

    fun setSelectedDay(newDate: LocalDate) {
        calendarUiState.value = updateSelectedDay(newDate)
    }

    private fun updateSelectedDay(newDate: LocalDate): CalendarUiState {
        val currentState = calendarUiState.value
        val selectedStartDate = currentState.selectedStartDate
        val selectedEndDate = currentState.selectedEndDate

        return when {
            selectedStartDate == null && selectedEndDate == null -> {
                currentState.setDates(newDate, null)
            }

            selectedStartDate != null && selectedEndDate != null -> {
                val animationDirection = if (newDate < selectedStartDate) {
                    AnimationDirection.BACKWARDS
                } else {
                    AnimationDirection.FORWARDS
                }
                this.calendarUiState.value = currentState.copy(
                    selectedStartDate = null,
                    selectedEndDate = null,
                    animateDirection = animationDirection
                )
                updateSelectedDay(newDate = newDate)
            }

            selectedStartDate == null -> {
                if (selectedEndDate == null) {
                    currentState
                } else if (newDate < selectedEndDate) {
                    currentState.copy(animateDirection = AnimationDirection.BACKWARDS)
                        .setDates(newDate, selectedEndDate)
                } else if (newDate > selectedEndDate) {
                    currentState.copy(animateDirection = AnimationDirection.FORWARDS)
                        .setDates(selectedEndDate, newDate)
                } else {
                    currentState
                }
            }

            else -> {
                if (newDate < selectedStartDate) {
                    currentState.copy(animateDirection = AnimationDirection.BACKWARDS)
                        .setDates(newDate, selectedStartDate)
                } else if (newDate > selectedStartDate) {
                    currentState.copy(animateDirection = AnimationDirection.FORWARDS)
                        .setDates(selectedStartDate, newDate)
                } else {
                    currentState
                }
            }
        }
    }

    companion object {
        const val DAYS_IN_WEEK = 7
    }
}

fun DatePeriod.toTotalMonths() = months + years * 12