/*
 * Copyright (c) 2025. Marcel Van Heerwaarden
 * @Author Marcel van Heerwaarden
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.github.mheerwaarden.eventdemo.ui.screen.event

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.mheerwaarden.eventdemo.Dimensions
import com.github.mheerwaarden.eventdemo.data.database.DummyEventRepository
import com.github.mheerwaarden.eventdemo.data.model.Event
import com.github.mheerwaarden.eventdemo.localization.toLocalizedString
import com.github.mheerwaarden.eventdemo.resources.Res
import com.github.mheerwaarden.eventdemo.resources.add_extra_event
import com.github.mheerwaarden.eventdemo.resources.cancel_event
import com.github.mheerwaarden.eventdemo.resources.change_event
import com.github.mheerwaarden.eventdemo.resources.event_calendar
import com.github.mheerwaarden.eventdemo.resources.event_overview
import com.github.mheerwaarden.eventdemo.ui.AppViewModelProvider
import com.github.mheerwaarden.eventdemo.ui.navigation.NavigationDestination
import com.github.mheerwaarden.eventdemo.ui.screen.AddItemButton
import com.github.mheerwaarden.eventdemo.ui.screen.EditItemButtons
import com.github.mheerwaarden.eventdemo.ui.theme.EventDemoAppTheme
import com.github.mheerwaarden.eventdemo.ui.util.DISABLED_ICON_OPACITY
import com.github.mheerwaarden.eventdemo.util.now
import com.github.mheerwaarden.eventdemo.util.plus
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.max

object EventOverviewDestination : NavigationDestination {
    override val route = "event_overview"
    override val titleRes = Res.string.event_overview
}

@Composable
fun EventOverviewScreen(
    onUpdateTopAppBar: (String, (() -> Unit)?, @Composable (RowScope.() -> Unit)) -> Unit,
    navigateToEvent: (String) -> Unit,
    navigateToAddEvent: (LocalDate) -> Unit,
    navigateToEditEvent: (String) -> Unit,
    navigateToEventCalendar: () -> Unit,
    modifier: Modifier = Modifier,
    eventViewModel: EventViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val eventUiState by eventViewModel.eventUiState.collectAsState()
    val preferencesState by eventViewModel.preferencesState.collectAsState()

    val title = stringResource(EventOverviewDestination.titleRes)
    onUpdateTopAppBar(title, null) {
        val foregroundColor = MaterialTheme.colorScheme.primary
        IconButton(
            onClick = navigateToEventCalendar,
            colors = IconButtonColors(
                containerColor = Color.Transparent,
                contentColor = foregroundColor,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = foregroundColor.copy(alpha = DISABLED_ICON_OPACITY)
            ),
        ) {
            Icon(
                imageVector = Icons.Filled.CalendarMonth,
                contentDescription = stringResource(Res.string.event_calendar),
            )
        }
    }

    EventOverviewBody(
        eventList = eventUiState,
        isReadOnly = preferencesState.isReadOnly,
        deleteEvent = eventViewModel::deleteEvent,
        navigateToEvent = navigateToEvent,
        navigateToAddEvent = navigateToAddEvent,
        navigateToEditEvent = navigateToEditEvent,
        modifier = modifier,
    )
}

private class OverviewConfig(colorScheme: ColorScheme) {
    val columnTimeWeight = .15f // 30% for both time columns together
    val columnEventTypeWeight = .3f // 30% for the worker column
    val columnDescriptionWeight = .4f // 40%
    val editIconButtonColors = IconButtonColors(
        containerColor = Color.Transparent,
        contentColor = colorScheme.surfaceTint,
        disabledContainerColor = Color.Transparent,
        disabledContentColor = colorScheme.surfaceTint.copy(alpha = DISABLED_ICON_OPACITY)
    )
    val headerColor = colorScheme.surfaceTint
    val dateColor = colorScheme.surface
    val evenColor = colorScheme.surfaceContainer
    val oddColor = colorScheme.surfaceVariant
}

@Composable
private fun EventOverviewBody(
    eventList: List<Event>,
    isReadOnly: Boolean,
    deleteEvent: (String) -> Unit,
    navigateToEvent: (String) -> Unit,
    navigateToAddEvent: (LocalDate) -> Unit,
    navigateToEditEvent: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Return early if the event list is empty
    if (eventList.isEmpty()) return

    val overviewConfig = OverviewConfig(MaterialTheme.colorScheme)

    // While grouping events by date, determine start index of overview
    var currentIndex = 0
    var startIndex = -1
    val now = now()
    val groupedEvents: Map<LocalDate, List<Event>> = buildMap {
        eventList.forEach { event ->
            val startDate = event.startDateTime
            val endDate = event.endDateTime

            if (startDate <= now && startIndex == -1) {
                // Start the overview at the number of events plus the number of headers
                startIndex = currentIndex + size
            }
            currentIndex++

            // Iterate through each date from start to end, adding the event for each date
            var currentDate = startDate
            while (currentDate <= endDate) {
                val currentStart =
                    if (currentDate == startDate) event.startDateTime else currentDate
                val currentEnd =
                    if (currentDate == endDate) event.endDateTime else currentDate.plus(
                        1, DateTimeUnit.DAY
                    )

                // Add the event to the list for this date, creating the list if it doesn't exist
                val currentEvents = getOrElse(currentDate.date) { emptyList() }.toMutableList()
                currentEvents.add(
                    event.copy(
                        startDateTime = currentStart,
                        endDateTime = currentEnd
                    )
                )
                put(currentDate.date, currentEvents)

                // Move to the next day
                currentDate = currentDate.plus(1, DateTimeUnit.DAY)
            }
        }
    }

    val lazyListState = rememberLazyListState(initialFirstVisibleItemIndex = max(startIndex, 0))
    LazyColumn(state = lazyListState, modifier = modifier) {
        groupedEvents.forEach { entry ->
            stickyHeader { EventHeader(entry.key, isReadOnly, overviewConfig, navigateToAddEvent) }
            itemsIndexed(entry.value) { index, event ->
                EventRow(
                    index = index,
                    event = event,
                    isReadOnly = isReadOnly,
                    overviewConfig = overviewConfig,
                    navigateToEvent = navigateToEvent,
                    deleteEvent = deleteEvent,
                    navigateToEditEvent = navigateToEditEvent
                )
            }
        }
    }
}

@Composable
private fun EventHeader(
    startDate: LocalDate,
    isReadOnly: Boolean,
    overviewConfig: OverviewConfig,
    navigateToAddEvent: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
            .background(color = overviewConfig.headerColor)
            .padding(Dimensions.padding_small)
    ) {
        Text(
            text = startDate.toLocalizedString(),
            color = overviewConfig.dateColor,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f).padding(Dimensions.padding_small)
        )
        if (!isReadOnly) {
            AddItemButton(
                navigateToAddScreen = { navigateToAddEvent(startDate) },
                foregroundColor = overviewConfig.dateColor,
                contentDescription = Res.string.add_extra_event
            )
        }
    }
}

@Composable
private fun EventRow(
    index: Int,
    event: Event,
    isReadOnly: Boolean,
    overviewConfig: OverviewConfig,
    navigateToEvent: (String) -> Unit,
    deleteEvent: (String) -> Unit,
    navigateToEditEvent: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
            .background(color = if (index % 2 == 0) overviewConfig.evenColor else overviewConfig.oddColor)
            .padding(Dimensions.padding_small)
            .clickable { navigateToEvent(event.id) }
    ) {
        Text(
            text = event.startDateTime.time.toLocalizedString(),
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(overviewConfig.columnTimeWeight)
        )
        Text(
            text = event.endDateTime.time.toLocalizedString(),
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(overviewConfig.columnTimeWeight)
        )
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(overviewConfig.columnEventTypeWeight)
        ) {
            Text(
                text = "•",
                color = event.htmlColor.color,
                fontSize = MaterialTheme.typography.headlineLarge.fontSize
            )
            Text(text = stringResource(event.eventType.text))
        }
        Text(
            text = event.description,
            modifier = Modifier.weight(overviewConfig.columnDescriptionWeight)
        )
        if (!isReadOnly) {
            EditItemButtons(
                event = event,
                editIconButtonColors = overviewConfig.editIconButtonColors,
                onDelete = deleteEvent,
                navigateToEditScreen = navigateToEditEvent,
                editContentDescription = Res.string.change_event,
                deleteContentDescription = Res.string.cancel_event
            )
        }
    }
}

@Preview
@Composable
fun EventOverviewScreenPreview() {
    val events = DummyEventRepository().getDefaultEvents(6)
    EventDemoAppTheme {
        EventOverviewBody(
            eventList = events,
            isReadOnly = false,
            navigateToEvent = {},
            deleteEvent = {},
            navigateToAddEvent = {},
            navigateToEditEvent = {},
            modifier = Modifier.fillMaxSize().background(Color.LightGray) // showBackground = true
        )
    }
}

@Preview
@Composable
fun EventOverviewScreenReadOnlyPreview() {
    val events = DummyEventRepository().getDefaultEvents(6)
    EventDemoAppTheme {
        EventOverviewBody(
            eventList = events,
            isReadOnly = true,
            navigateToEvent = {},
            deleteEvent = {},
            navigateToAddEvent = {},
            navigateToEditEvent = {},
            modifier = Modifier.fillMaxSize().background(Color.LightGray) // showBackground = true
        )
    }
}
