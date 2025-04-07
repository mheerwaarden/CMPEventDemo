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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.github.mheerwaarden.eventdemo.resources.Res
import com.github.mheerwaarden.eventdemo.resources.add_extra_event
import com.github.mheerwaarden.eventdemo.resources.cancel_event
import com.github.mheerwaarden.eventdemo.resources.change_event
import com.github.mheerwaarden.eventdemo.resources.event_overview
import com.github.mheerwaarden.eventdemo.ui.AppViewModelProvider
import com.github.mheerwaarden.eventdemo.ui.navigation.NavigationDestination
import com.github.mheerwaarden.eventdemo.ui.screen.AddItemButton
import com.github.mheerwaarden.eventdemo.ui.screen.EditItemButtons
import com.github.mheerwaarden.eventdemo.ui.theme.EventDemoAppTheme
import com.github.mheerwaarden.eventdemo.ui.util.DISABLED_ICON_OPACITY
import com.github.mheerwaarden.eventdemo.util.formatDate
import com.github.mheerwaarden.eventdemo.util.formatTime
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

object EventOverviewDestination : NavigationDestination {
    override val route = "event_overview"
    override val titleRes = Res.string.event_overview
}

@Composable
fun EventOverviewScreen(
    onUpdateTopAppBar: (String, @Composable (RowScope.() -> Unit)) -> Unit,
    navigateToAddEvent: () -> Unit,
    navigateToEditEvent: (Long) -> Unit,
    modifier: Modifier = Modifier,
    eventViewModel: EventViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val eventUiState by eventViewModel.eventUiState.collectAsState()
    val preferencesState by eventViewModel.preferencesState.collectAsState()

    val title = stringResource(EventOverviewDestination.titleRes)
    onUpdateTopAppBar(title) {}

    val isReadOnly = preferencesState.isReadOnly
    EventOverviewBody(
        eventScheme = eventUiState,
        isReadOnly = isReadOnly,
        deleteEvent = eventViewModel::deleteEvent,
        navigateToAddEvent = navigateToAddEvent,
        navigateToEditEvent = navigateToEditEvent,
        modifier = modifier,
    )
}

@Composable
fun EventOverviewBody(
    eventScheme: List<Event>,
    isReadOnly: Boolean,
    deleteEvent: (Long) -> Unit,
    navigateToAddEvent: () -> Unit,
    navigateToEditEvent: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val columnTimeWeight = .15f // 30% for both time columns together
    val columnWorkerWeight = .3f // 30% for the worker column
    val columnDescriptionWeight = .4f // 40%
    val editIconButtonColors = IconButtonColors(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.surfaceTint,
        disabledContainerColor = Color.Transparent,
        disabledContentColor = MaterialTheme.colorScheme.surfaceTint.copy(alpha = DISABLED_ICON_OPACITY)
    )
    val dateColor = MaterialTheme.colorScheme.surface
    val evenColor = MaterialTheme.colorScheme.surfaceContainer
    val oddColor = MaterialTheme.colorScheme.surfaceVariant
    val timeZone = TimeZone.currentSystemDefault()
    var currentDateTime = LocalDateTime(1970, 1, 1, 0, 0)
    LazyColumn(modifier = modifier) {
        itemsIndexed(items = eventScheme, key = { _, item -> item.id }) { index, item ->
            val startDateTime = item.startInstant.toLocalDateTime(timeZone)
            if (startDateTime.year != currentDateTime.year
                || startDateTime.month != currentDateTime.month
                || startDateTime.dayOfMonth != currentDateTime.dayOfMonth
                ) {
                currentDateTime = startDateTime
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                        .background(color = MaterialTheme.colorScheme.surfaceTint)
                        .padding(Dimensions.padding_small)
                ) {
                    Text(
                        text = startDateTime.formatDate(),
                        color = dateColor,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f).padding(Dimensions.padding_small)
                    )
                    if (!isReadOnly) {
                        AddItemButton(
                            navigateToAddScreen = navigateToAddEvent,
                            foregroundColor = dateColor,
                            contentDescription = Res.string.add_extra_event
                        )
                    }
                }
            }
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
                    .background(color = if (index % 2 == 0) evenColor else oddColor)
                    .padding(Dimensions.padding_small)
            ) {
                val endInstant = item.endInstant
                Text(
                    text = startDateTime.formatTime(),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(columnTimeWeight)
                )
                Text(
                    text = endInstant.toLocalDateTime(timeZone).formatTime(),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(columnTimeWeight)
                )
                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(columnWorkerWeight)
                ) {
                    Text(text = "•", color = item.color, fontSize = MaterialTheme.typography.headlineLarge.fontSize)
                    Text(text = stringResource(item.eventType.text))
                }
                Text(text = item.description, modifier = Modifier.weight(columnDescriptionWeight))
                if (!isReadOnly) {
                    EditItemButtons(
                        item = item,
                        editIconButtonColors = editIconButtonColors,
                        onDelete = deleteEvent,
                        navigateToEditScreen = navigateToEditEvent,
                        editContentDescription = Res.string.change_event,
                        deleteContentDescription = Res.string.cancel_event
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun EventOverviewScreenPreview() {
    val events = DummyEventRepository().getDummyEvents(6)
    EventDemoAppTheme {
        EventOverviewBody(
            eventScheme = events,
            isReadOnly = false,
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
    val events = DummyEventRepository().getDummyEvents(6)
    EventDemoAppTheme {
        EventOverviewBody(
            eventScheme = events,
            isReadOnly = true,
            deleteEvent = {},
            navigateToAddEvent = {},
            navigateToEditEvent = {},
            modifier = Modifier.fillMaxSize().background(Color.LightGray) // showBackground = true
        )
    }
}
