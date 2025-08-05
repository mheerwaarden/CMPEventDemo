package com.github.mheerwaarden.eventdemo.ui.screen.event

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.mheerwaarden.eventdemo.Dimensions
import com.github.mheerwaarden.eventdemo.data.database.DummyEventRepository
import com.github.mheerwaarden.eventdemo.data.model.Event
import com.github.mheerwaarden.eventdemo.resources.Res
import com.github.mheerwaarden.eventdemo.resources.event_calendar
import com.github.mheerwaarden.eventdemo.resources.event_overview
import com.github.mheerwaarden.eventdemo.ui.AppViewModelProvider
import com.github.mheerwaarden.eventdemo.ui.components.calendar.CalendarWithEvents
import com.github.mheerwaarden.eventdemo.ui.navigation.NavigationDestination
import com.github.mheerwaarden.eventdemo.ui.screen.LoadingScreen
import com.github.mheerwaarden.eventdemo.ui.screen.settings.SettingsViewModel
import com.github.mheerwaarden.eventdemo.ui.theme.EventDemoAppTheme
import com.github.mheerwaarden.eventdemo.ui.util.DISABLED_ICON_OPACITY
import com.github.mheerwaarden.eventdemo.util.now
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

object EventCalendarDestination : NavigationDestination {
    override val route = "event_calendar"
    override val titleRes = Res.string.event_calendar
}

@Composable
fun EventCalendarScreen(
    onUpdateTopAppBar: (String, (() -> Unit)?, @Composable (RowScope.() -> Unit)) -> Unit,
    navigateToEventOverview: () -> Unit,
    navigateToEvent: (String) -> Unit,
    modifier: Modifier = Modifier,
    isHorizontal: Boolean = false,
    eventViewModel: EventCalendarViewModel = viewModel(factory = AppViewModelProvider.Factory),
    settingsViewModel: SettingsViewModel
) {
    val eventUiState by eventViewModel.eventUiState.collectAsState()
    LoadingScreen(loadingViewModel = settingsViewModel) {
        val preferences by settingsViewModel.settingsUiState.collectAsState()

        val title = stringResource(EventCalendarDestination.titleRes)
        onUpdateTopAppBar(title, null) {
            val foregroundColor = MaterialTheme.colorScheme.primary
            IconButton(
                onClick = navigateToEventOverview,
                colors = IconButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = foregroundColor,
                    disabledContainerColor = Color.Transparent,
                    disabledContentColor = foregroundColor.copy(alpha = DISABLED_ICON_OPACITY)
                ),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.List,
                    contentDescription = stringResource(Res.string.event_overview),
                )
            }
        }

        EventCalendarBody(
            events = eventUiState,
            setPeriod = eventViewModel::setPeriod,
            currentFilter = eventViewModel.calendarUiState.eventFilter,
            setFilter = eventViewModel::setFilter,
            isHorizontal = isHorizontal,
            navigateToEvent = navigateToEvent,
            isExpanded = preferences.isCalendarExpanded,
            onExpand = settingsViewModel::setCalendarExpanded,
            modifier = modifier,
        )
    }
}

@Composable
fun EventCalendarBody(
    events: List<Event>,
    setPeriod: (LocalDate, LocalDate) -> Unit,
    navigateToEvent: (String) -> Unit,
    modifier: Modifier = Modifier,
    startDate: LocalDate = now().date,
    currentFilter: EventFilter = EventFilter.GENERAL,
    setFilter: (EventFilter) -> Unit = {},
    isHorizontal: Boolean = false,
    isExpanded: Boolean = true,
    onExpand: (Boolean) -> Unit = {}
) {
    CalendarWithEvents(
        events = events,
        startDate = startDate,
        setPeriod = setPeriod,
        actions = { FilterButtons(currentFilter = currentFilter, setFilter = setFilter) },
        isHorizontal = isHorizontal,
        navigateToEvent = navigateToEvent,
        isExpanded = isExpanded,
        onExpand = onExpand,
        modifier = modifier,
    )
}

@Composable
fun FilterButtons(currentFilter: EventFilter, setFilter: (EventFilter) -> Unit) {
    // Preserve vertical screen estate
    Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.padding_small)) {
        EventFilter.entries.forEach { entry ->
            FilterChip(
                selected = currentFilter == entry,
                onClick = { setFilter(entry) },
                label = { Text(stringResource(entry.text)) },
                leadingIcon = if (currentFilter == entry) {
                    {
                        Icon(
                            imageVector = Icons.Outlined.Done,
                            contentDescription = "Selected filter",
                            modifier = Modifier.size(FilterChipDefaults.IconSize),
                        )
                    }
                } else {
                    null
                },
            )
        }
    }
    HorizontalDivider()
}

@Preview
@Composable
fun EventCalendarScreenPreview() {
    val events = DummyEventRepository().getDefaultEvents(7)
    EventDemoAppTheme {
        EventCalendarBody(
            events = events,
            // The first event is in the previous month
            startDate = events[1].startDateTime.date,
            setPeriod = { _, _ -> },
            navigateToEvent = {},
            modifier = Modifier.fillMaxSize().background(Color.LightGray) // showBackground = true
        )
    }
}

@Preview
@Composable
fun EventCalendarLandscapeScreenPreview() {
    val events = DummyEventRepository().getDefaultEvents(7)
    EventDemoAppTheme {
        EventCalendarBody(
            events = events,
            // The first event is in the previous month
            startDate = events[1].startDateTime.date,
            setPeriod = { _, _ -> },
            isHorizontal = true,
            navigateToEvent = {},
            modifier = Modifier.fillMaxSize().background(Color.LightGray) // showBackground = true
        )
    }
}
