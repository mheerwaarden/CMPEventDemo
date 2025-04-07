package com.github.mheerwaarden.eventdemo.ui.components.cranecalendar.model

import com.github.mheerwaarden.eventdemo.util.weekNumber
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.minus
import kotlinx.datetime.plus

class YearMonth(year: Int, month: Int) {
    private val date = LocalDate(year, month, 1)
    val year: Int
        get() = date.year
    val month: Month
        get() = date.month
    val monthNumber: Int
        get() = date.monthNumber

    /** @Return a YearMonth that is the requested number of months later than the current month */
    fun plusMonths(value: Int): YearMonth {
        val newDate = date.plus(value, DateTimeUnit.MONTH)
        return YearMonth(newDate.year, newDate.monthNumber)
    }


    /** @return the LocalDate at the requested day in the current month */
    fun atDay(dayOfMonth: Int) : LocalDate = LocalDate(year, month, dayOfMonth)

    /** @return The number of weeks in the current month. */
    fun getNumberWeeks(): Int {
        val firstDayOfMonth = date
        val lastDayOfMonth = date.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)
        val firstWeekNumber = firstDayOfMonth.weekNumber()
        val lastWeekNumber = lastDayOfMonth.weekNumber()
        return lastWeekNumber - firstWeekNumber + 1
    }

    companion object {
        fun from(date: LocalDate) = YearMonth(date.year, date.monthNumber)
    }
}
