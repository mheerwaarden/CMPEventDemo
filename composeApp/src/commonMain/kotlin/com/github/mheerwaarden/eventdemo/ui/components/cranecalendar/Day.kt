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

package com.github.mheerwaarden.eventdemo.ui.components.cranecalendar

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import com.github.mheerwaarden.eventdemo.ui.components.cranecalendar.model.CalendarUiState
import com.github.mheerwaarden.eventdemo.ui.components.cranecalendar.model.YearMonth
import kotlinx.datetime.LocalDate

@Composable
internal fun DayOfWeekHeading(day: String) {
    DayContainer {
        Text(
            text = day,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxSize()
                .wrapContentHeight(Alignment.CenterVertically),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
private fun DayContainer(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: () -> Unit = { },
    onClickEnabled: Boolean = true,
    backgroundColor: Color = Color.Transparent,
    onClickLabel: String? = null,
    content: @Composable () -> Unit
) {
    val stateDescriptionLabel =
        if (selected) "Selected" else "Not selected"
//        stringResource(if (selected) Res.string.state_descr_selected else Res.string.state_descr_not_selected)
    Box(
        modifier = modifier
            .size(width = CELL_SIZE, height = CELL_SIZE)
            .pointerInput(Any()) {
                detectTapGestures {
                    onClick()
                }
            }
            .then(
                if (onClickEnabled) {
                    modifier.semantics {
                        stateDescription = stateDescriptionLabel
                        onClick(label = onClickLabel, action = null)
                    }
                } else {
                    modifier.clearAndSetSemantics { }
                }
            )
            .background(backgroundColor)
    ) {
        content()
    }
}

@Composable
internal fun Day(
    day: LocalDate,
    calendarState: CalendarUiState,
    onDayClicked: (LocalDate) -> Unit,
    month: YearMonth,
    modifier: Modifier = Modifier
) {
    val selected = calendarState.isDateInSelectedPeriod(day)
    DayContainer(
        modifier = modifier.semantics {
            text = AnnotatedString(
                "${month.month.name.lowercase().capitalize(Locale.current)} " +
                    "${day.dayOfMonth} ${month.year}"
            )
            dayStatusProperty = selected
        },
        selected = selected,
        onClick = { onDayClicked(day) },
        onClickLabel = "Select" //stringResource(id = Res.string.click_label_select)
    ) {

        Text(
            text = day.dayOfMonth.toString(),
            color = if (selected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center)
                // Parent will handle semantics
                .clearAndSetSemantics {},
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

val DayStatusKey = SemanticsPropertyKey<Boolean>("DayStatusKey")
var SemanticsPropertyReceiver.dayStatusProperty by DayStatusKey
