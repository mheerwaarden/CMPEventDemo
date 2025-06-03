/*
 * Copyright (c) 2024. Marcel van Heerwaarden
 *
 * Copyright (C) 2019 The Android Open Source Project
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.mheerwaarden.eventdemo.util

import com.github.mheerwaarden.eventdemo.module.NameStyle
import com.github.mheerwaarden.eventdemo.module.localizedMonthNames
import com.github.mheerwaarden.eventdemo.module.toLocalizedDateString
import com.github.mheerwaarden.eventdemo.module.toLocalizedDateTimeString
import com.github.mheerwaarden.eventdemo.module.toLocalizedTimeString
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.daysUntil
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.periodUntil
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

// region Now

/** Now as Instant */
fun nowInstant(): Instant = Clock.System.now()

fun startOfMonthInstant(): Instant = startOfMonth().toInstant()

fun endOfMonthInstant(): Instant = endOfMonth().toInstant()

/** Now as LocalDateTime */
fun now(): LocalDateTime = nowInstant().toLocalDateTime(TimeZone.currentSystemDefault())

/** Now in UTC milliseconds */
fun nowMillis(): Long = nowInstant().toEpochMilliseconds()

fun startOfMonth(): LocalDate {
    val now = now()
    return LocalDate(now.year, now.month, 1)
}

fun endOfMonth(): LocalDate {
    return startOfMonth().plus(1, DateTimeUnit.MONTH)
}

// endregion

// region Conversion

/** Convert UTC milliseconds to LocalDateTime in the default time zone */
fun ofEpochMilli(utcMillis: Long): LocalDateTime =
    Instant.fromEpochMilliseconds(utcMillis).toLocalDateTime(TimeZone.currentSystemDefault())

/** Convert LocalDateTime to UTC milliseconds */
fun LocalDateTime.toEpochMilli(): Long =
    toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()

/** Convert Instant to LocalDateTime using the default time zone */
fun Instant.toLocalDateTime(): LocalDateTime = toLocalDateTime(TimeZone.currentSystemDefault())

/** Convert LocalDate to Instant using the default time zone */
fun LocalDate.toInstant(): Instant = atStartOfDayIn(TimeZone.currentSystemDefault())

// endregion

// region Custom formatting
private val regex = """(yyyy|yy|MM|dd|HH|mm|ss)|(.)""".toRegex()
private fun getDateTimeFormat(pattern: String): DateTimeFormat<LocalDateTime> = LocalDateTime.Format {
    regex.findAll(pattern).forEach { matchResult ->
        val token = matchResult.groupValues[1]
        val literal = matchResult.groupValues[2]

        when (token) {
            "yyyy" -> year()
            "yy" -> yearTwoDigits(2020)
            "MM" -> monthNumber()
            "dd" -> dayOfMonth()
            "HH" -> hour()
            "mm" -> minute()
            "ss" -> second()
            else -> {
                // This means it's a literal character
                if (literal.isNotEmpty()) {
                    char(literal.first())
                }
            }
        }
    }
}

private fun getDateFormat(pattern: String): DateTimeFormat<LocalDate> = LocalDate.Format {
    regex.findAll(pattern).forEach { matchResult ->
        val token = matchResult.groupValues[1]
        val literal = matchResult.groupValues[2]

        when (token) {
            "yyyy" -> year()
            "yy" -> yearTwoDigits(2020)
            "MM" -> monthNumber()
            "dd" -> dayOfMonth()
            else -> {
                // This means it's a literal character
                if (literal.isNotEmpty()) {
                    char(literal.first())
                }
            }
        }
    }
}

private fun getTimeFormat(pattern: String): DateTimeFormat<LocalTime> = LocalTime.Format {
    regex.findAll(pattern).forEach { matchResult ->
        val token = matchResult.groupValues[1]
        val literal = matchResult.groupValues[2]

        when (token) {
            "HH" -> hour()
            "mm" -> minute()
            "ss" -> second()
            else -> {
                // This means it's a literal character
                if (literal.isNotEmpty()) {
                    char(literal.first())
                }
            }
        }
    }
}

/** Format a LocalDateTime according to the given pattern */
fun LocalDateTime.format(pattern: String): String = format(getDateTimeFormat(pattern))
fun LocalDate.format(pattern: String): String = format(getDateFormat(pattern))
fun LocalTime.format(pattern: String): String = format(getTimeFormat(pattern))

// endregion

// region Default formatting and parsing
val DEFAULT_DATE_FORMAT by lazy {
    LocalDate.Format { year(); char('-'); monthNumber(); char('-'); dayOfMonth() }
}

val DEFAULT_TIME_FORMAT by lazy { LocalTime.Format { hour(); char(':'); minute() } }

/** Short default date format for formatting only */
val DEFAULT_INSTANT_DATE_FORMAT by lazy {
    DateTimeComponents.Format { year(); char('-'); monthNumber(); char('-'); dayOfMonth() }
}

/** Short default date and time format for parsing and formatting */
val DEFAULT_INSTANT_DATETIME_FORMAT by lazy {
    DateTimeComponents.Format {
        year(); char('-'); monthNumber(); char('-'); dayOfMonth()
        char(' ')
        hour(); char(':'); minute()
    }
}

/** Format UTC milliseconds as date and time according to the short default format */
fun formatUtcMillis(utcMillis: Long?): String {
    if (utcMillis == null) return ""
    val dateTime = ofEpochMilli(utcMillis)
    return dateTime.formatDateTime()
}

/** Format a LocalDateTime according to the short default format */
fun LocalDateTime.formatDateTime(): String = "${formatDate()} ${formatTime()}"

/** Format a LocalDateTime as a date according to the short default format */
fun LocalDateTime.formatDate(): String = date.format()

/** Format a LocalDateTime as a time according to the short default format */
fun LocalDateTime.formatTime(): String = time.format()


/** Format a LocalDate according to the short default format */
fun LocalDate.format(): String = DEFAULT_DATE_FORMAT.format(this)

/** Format a LocalTime according to the short default format */
fun LocalTime.format(): String = DEFAULT_TIME_FORMAT.format(this)

/** Format the time component of an Instant according to the short default format */
fun Instant.formatTime(): String = toLocalDateTime().formatTime()

/** Parse a date string according to the short default format */
fun String.parseDate(): LocalDate = DEFAULT_DATE_FORMAT.parse(this)

// endregion

// region Localized formatting

fun LocalDateTime.toLocalizedDateTimeString(): String = toLocalizedDateTimeString(this)
fun LocalDate.toLocalizedDateString(): String = toLocalizedDateString(this)
fun LocalTime.toLocalizedTimeString(): String = toLocalizedTimeString(this)
val fullMonthNames by lazy { MonthNames(localizedMonthNames(NameStyle.FULL)) }
val shortMonthNames by lazy { MonthNames(localizedMonthNames(NameStyle.ABBREVIATED)) }

// endregion

// region Operations

/**
 * Adds a specified amount of time to this [LocalDateTime].
 *
 * @param value The amount of time to add.
 * @param unit  The unit of time to add (e.g., [DateTimeUnit.DAY], [DateTimeUnit.MINUTE]).
 * @return A new [LocalDateTime] with the specified amount of time added.
 * @throws IllegalArgumentException if an unsupported [DateTimeUnit] is used.
 */
fun LocalDateTime.plus(value: Int, unit: DateTimeUnit): LocalDateTime {
    return when (unit) {
        is DateTimeUnit.DateBased -> {
            // For date-based units, use LocalDate.plus
            LocalDateTime(date.plus(value, unit), time)
        }

        is DateTimeUnit.TimeBased -> {
            // For time-based units, convert to Instant, add, and convert back
            val timeZone = TimeZone.currentSystemDefault()
            val instant = this.toInstant(timeZone)
            val newInstant = instant.plus(value, unit, timeZone)
            newInstant.toLocalDateTime(timeZone)
        }

    }
}

/**
 * @return The number of days in the month of this date.
 */
fun LocalDate.daysInMonth(): Int {
    val firstDayOfMonth = LocalDate(year, month, 1)
    val firstDayOfNextMonth = firstDayOfMonth.plus(1, DateTimeUnit.MONTH)
    return firstDayOfMonth.daysUntil(firstDayOfNextMonth)
}

/**
 * @return The number of days in the month of this date-time.
 */
fun LocalDateTime.daysInMonth(): Int = date.daysInMonth()

/**
 * Calculates the number of days between this [LocalDate] and the [otherDate].
 *
 * The result is inclusive of the end date and exclusive of the start date.
 * For example, if this date is January 1, 2023, and [otherDate] is January 3, 2023,
 * this function will return 3.
 *
 * @param otherDate The end date.
 * @return The number of days between this date and [otherDate], inclusive of the end date.
 */
fun LocalDate.daysBetween(otherDate: LocalDate) =
    periodUntil(otherDate.plus(1, DateTimeUnit.DAY)).days

/**
 * Returns the date of the previous or same occurrence of the specified [dayOfWeek].
 *
 * @param dayOfWeek The desired day of the week.
 * @return The LocalDate of the previous or same [dayOfWeek].
 */
fun LocalDate.previousOrSame(dayOfWeek: DayOfWeek): LocalDate {
    val daysToSubtract = getPreviousOrSameOffset(dayOfWeek)
    return this.minus(daysToSubtract, DateTimeUnit.DAY)
}

/**
 * Calculates the number of days to go back from the current date to reach the previous or same
 * occurrence of the specified [dayOfWeek].
 *
 * @param dayOfWeek The desired day of the week.
 * @return The number of days to subtract.
 */
private fun LocalDate.getPreviousOrSameOffset(
    dayOfWeek: DayOfWeek
): Int {
    val currentDayOfWeek = this.dayOfWeek
    return when {
        currentDayOfWeek == dayOfWeek -> 0
        currentDayOfWeek.isoDayNumber > dayOfWeek.isoDayNumber ->
            currentDayOfWeek.isoDayNumber - dayOfWeek.isoDayNumber

        else -> currentDayOfWeek.isoDayNumber + (7 - dayOfWeek.isoDayNumber)
    }
}

// endregion

// region Extended properties

/**
 * @return The ISO 8601 week number for the current date.
 */
fun LocalDate.weekNumber(): Int {
    val firstDayOfYear = LocalDate(year, 1, 1)
    val offset = firstDayOfYear.getPreviousOrSameOffset(DayOfWeek.THURSDAY)
    val daysSinceFirstDayOfWeek = dayOfYear - offset
    return (daysSinceFirstDayOfWeek / 7) + 1
}

/**
 * @return true if the current date is in a leap year.
 */
fun LocalDate.isLeapYear(): Boolean {
    return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
}

// endregion