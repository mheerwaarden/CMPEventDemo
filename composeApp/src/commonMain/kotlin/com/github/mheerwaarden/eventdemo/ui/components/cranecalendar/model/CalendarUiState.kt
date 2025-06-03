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

import com.github.mheerwaarden.eventdemo.util.daysBetween
import com.github.mheerwaarden.eventdemo.util.shortMonthNames
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.char
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlin.math.abs

data class CalendarUiState(
    val selectedStartDate: LocalDate? = null,
    val selectedEndDate: LocalDate? = null,
    val animateDirection: AnimationDirection? = null,
) {

    val numberSelectedDays: Float
        get() {
            if (selectedStartDate == null) return 0f
            if (selectedEndDate == null) return 1f
            return selectedStartDate.daysBetween(selectedEndDate).toFloat()
        }

    val hasSelectedDates: Boolean
        get() = selectedStartDate != null || selectedEndDate != null

    val selectedDatesFormatted: String
        get() {
            if (selectedStartDate == null) return ""
            var result = MONTH_FORMAT.format(selectedStartDate)
            if (selectedEndDate != null) {
                result += " - ${MONTH_FORMAT.format(selectedEndDate)}"
            }
            return result
        }

    fun hasSelectedPeriodOverlap(start: LocalDate, end: LocalDate): Boolean {
        if (!hasSelectedDates) return false
        if (selectedStartDate == null && selectedEndDate == null) return false
        if (selectedStartDate == start || selectedStartDate == end) return true
        if (selectedEndDate == null) {
            return selectedStartDate!! >= start && selectedStartDate <= end
        }
        return selectedStartDate != null && end >= selectedStartDate && start <= selectedEndDate
    }

    fun isDateInSelectedPeriod(date: LocalDate): Boolean {
        if (selectedStartDate == null) return false
        if (selectedStartDate == date) return true
        if (selectedEndDate == null) return false
        if ((date < selectedStartDate) || (date > selectedEndDate)) return false
        return true
    }

    fun getNumberSelectedDaysInWeek(currentWeekStartDate: LocalDate, month: YearMonth): Int {
        var countSelected = 0
        var currentDate = currentWeekStartDate
        for (i in 0 until CalendarState.DAYS_IN_WEEK) {
            if (isDateInSelectedPeriod(currentDate) && currentDate.month == month.month) {
                countSelected++
            }
            currentDate = currentDate.plus(1, DateTimeUnit.DAY)
        }
        return countSelected
    }

    /**
     * Returns the number of selected days from the start or end of the week, depending on direction.
     */
    fun selectedStartOffset(currentWeekStartDate: LocalDate, yearMonth: YearMonth): Int {
        return if (animateDirection == null || animateDirection.isForwards()) {
            var startDate = currentWeekStartDate
            var startOffset = 0
            for (i in 0 until CalendarState.DAYS_IN_WEEK) {
                if (!isDateInSelectedPeriod(startDate) || startDate.month != yearMonth.month) {
                    startOffset++
                } else {
                    break
                }
                startDate = startDate.plus(1, DateTimeUnit.DAY)
            }
            startOffset
        } else {
            var startDate =
                currentWeekStartDate.plus(CalendarState.DAYS_IN_WEEK - 1, DateTimeUnit.DAY)
            var startOffset = 0

            for (i in 0 until CalendarState.DAYS_IN_WEEK) {
                if (!isDateInSelectedPeriod(startDate) || startDate.month != yearMonth.month) {
                    startOffset++
                } else {
                    break
                }
                startDate = startDate.minus(1, DateTimeUnit.DAY)
            }
            CalendarState.DAYS_IN_WEEK - startOffset
        }
    }

    fun isLeftHighlighted(beginningWeek: LocalDate?, month: YearMonth): Boolean =
        if (beginningWeek == null || month.monthNumber != beginningWeek.monthNumber) {
            false
        } else {
            val beginningWeekSelected = isDateInSelectedPeriod(beginningWeek)
            val lastDayPreviousWeek = beginningWeek.minus(1, DateTimeUnit.DAY)
            isDateInSelectedPeriod(lastDayPreviousWeek) && beginningWeekSelected
        }

    fun isRightHighlighted(
        beginningWeek: LocalDate?,
        month: YearMonth,
    ): Boolean {
        val lastDayOfTheWeek = beginningWeek?.plus(6, DateTimeUnit.DAY)
        return if (lastDayOfTheWeek != null) {
            if (month.monthNumber != lastDayOfTheWeek.monthNumber) {
                false
            } else {
                val lastDayOfTheWeekSelected = isDateInSelectedPeriod(lastDayOfTheWeek)
                val firstDayNextWeek = lastDayOfTheWeek.plus(1, DateTimeUnit.DAY)
                isDateInSelectedPeriod(firstDayNextWeek) && lastDayOfTheWeekSelected
            }
        } else {
            false
        }
    }

    fun dayDelay(currentWeekStartDate: LocalDate): Int {
        if (selectedStartDate == null && selectedEndDate == null) return 0
        // if selected week contains start date, don't have any delay
        val endWeek = currentWeekStartDate.plus(6, DateTimeUnit.DAY)
        return if (animateDirection != null && animateDirection.isBackwards()) {
            if (selectedEndDate != null &&
                ((selectedEndDate < currentWeekStartDate) || (selectedEndDate > endWeek))
            ) {
                // selected end date is not in current week - return actual days calc difference
                abs(endWeek.daysBetween(selectedEndDate))
            } else {
                0
            }
        } else {
            if (selectedStartDate != null &&
                ((selectedStartDate < currentWeekStartDate) || (selectedStartDate > endWeek))
            ) {
                // selected start date is not in current week
                abs(currentWeekStartDate.daysBetween(selectedStartDate))
            } else {
                0
            }
        }
    }

    fun monthOverlapSelectionDelay(
        currentWeekStartDate: LocalDate,
        week: Week,
    ): Int {
        return if (animateDirection?.isBackwards() == true) {
            val endWeek = currentWeekStartDate.plus(6, DateTimeUnit.DAY)
            val isStartInADifferentMonth = endWeek.month != week.yearMonth.month
            if (isStartInADifferentMonth) {
                var currentDate = endWeek
                var offset = 0
                for (i in 0 until CalendarState.DAYS_IN_WEEK) {
                    if (currentDate.monthNumber != week.yearMonth.monthNumber &&
                        isDateInSelectedPeriod(currentDate)
                    ) {
                        offset++
                    }
                    currentDate = currentDate.minus(1, DateTimeUnit.DAY)
                }
                offset
            } else {
                0
            }
        } else {
            val isStartInADifferentMonth = currentWeekStartDate.month != week.yearMonth.month
            return if (isStartInADifferentMonth) {
                var currentDate = currentWeekStartDate
                var offset = 0
                for (i in 0 until CalendarState.DAYS_IN_WEEK) {
                    if (currentDate.monthNumber != week.yearMonth.monthNumber &&
                        isDateInSelectedPeriod(currentDate)
                    ) {
                        offset++
                    }
                    currentDate = currentDate.plus(1, DateTimeUnit.DAY)
                }
                offset
            } else {
                0
            }
        }
    }

    fun setDates(newFrom: LocalDate?, newTo: LocalDate?): CalendarUiState {
        return if (newTo == null) {
            copy(selectedStartDate = newFrom)
        } else {
            copy(selectedStartDate = newFrom, selectedEndDate = newTo)
        }
    }

    companion object {
        private val MONTH_FORMAT by lazy {
            LocalDate.Format { monthName(shortMonthNames); char(' '); year(); }
        }
    }
}
