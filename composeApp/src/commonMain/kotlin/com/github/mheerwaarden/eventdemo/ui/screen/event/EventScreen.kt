package com.github.mheerwaarden.eventdemo.ui.screen.event


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.mheerwaarden.eventdemo.Dimensions
import com.github.mheerwaarden.eventdemo.data.database.DummyEventRepository
import com.github.mheerwaarden.eventdemo.data.model.Event
import com.github.mheerwaarden.eventdemo.resources.Res
import com.github.mheerwaarden.eventdemo.resources.color
import com.github.mheerwaarden.eventdemo.resources.contact
import com.github.mheerwaarden.eventdemo.resources.delete
import com.github.mheerwaarden.eventdemo.resources.description
import com.github.mheerwaarden.eventdemo.resources.edit
import com.github.mheerwaarden.eventdemo.resources.event_category
import com.github.mheerwaarden.eventdemo.resources.event_overview
import com.github.mheerwaarden.eventdemo.resources.event_title
import com.github.mheerwaarden.eventdemo.resources.event_type
import com.github.mheerwaarden.eventdemo.resources.is_online
import com.github.mheerwaarden.eventdemo.resources.location
import com.github.mheerwaarden.eventdemo.resources.notes
import com.github.mheerwaarden.eventdemo.ui.AppViewModelProvider
import com.github.mheerwaarden.eventdemo.ui.navigation.NavigationDestination
import com.github.mheerwaarden.eventdemo.ui.screen.DeleteEventHeaderButton
import com.github.mheerwaarden.eventdemo.ui.screen.HeaderButton
import com.github.mheerwaarden.eventdemo.ui.screen.LoadingScreen
import com.github.mheerwaarden.eventdemo.ui.screen.settings.SettingsViewModel
import com.github.mheerwaarden.eventdemo.ui.theme.EventDemoAppTheme
import com.github.mheerwaarden.eventdemo.ui.util.DISABLED_ICON_OPACITY
import com.github.mheerwaarden.eventdemo.util.formatDate
import com.github.mheerwaarden.eventdemo.util.formatTime
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

object EventDestination : NavigationDestination {
    override val route = "event"
    override val titleRes = Res.string.event_title
    const val eventIdArg = "eventId"
    val routeWithArgs = "$route/{$eventIdArg}"
}

@Composable
fun EventScreen(
    onUpdateTopAppBar: (String, @Composable (RowScope.() -> Unit)) -> Unit,
    navigateToEventOverview: () -> Unit,
    navigateToEditEvent: (Long) -> Unit,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    eventViewModel: EventEditViewModel = viewModel(factory = AppViewModelProvider.Factory),
    settingsViewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    onUpdateTopAppBar(stringResource(EventDestination.titleRes)) {
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

    LoadingScreen(loadingViewModel = settingsViewModel) {
        val eventUiState = eventViewModel.eventUiState
        EventScreen(
            eventUiState = eventUiState,
            deleteEvent = {
                eventViewModel.deleteEvent(eventUiState.id)
                navigateBack()
            },
            navigateToEditEvent = { navigateToEditEvent(eventUiState.id) },
            modifier = modifier.fillMaxSize()
        )
    }
}

@Composable
private fun EventScreen(
    eventUiState: EventUiState,
    deleteEvent: () -> Unit,
    navigateToEditEvent: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(
            space = Dimensions.padding_medium,
            alignment = Alignment.Top
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        EventHeader(
            event = eventUiState.toEvent(),
            startDateTime = eventUiState.startDateTime,
            endDateTime = eventUiState.endDateTime,
            deleteEvent = deleteEvent,
            navigateToEditEvent = navigateToEditEvent,
            modifier = Modifier.fillMaxWidth()
        )
        EventBody(
            eventUiState = eventUiState,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun EventHeader(
    event: Event,
    startDateTime: LocalDateTime,
    endDateTime: LocalDateTime,
    deleteEvent: () -> Unit,
    navigateToEditEvent: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surfaceTint)
            .padding(Dimensions.padding_small)
    ) {
        Text(
            text = "${startDateTime.formatDate()} ${startDateTime.formatTime()} - ${endDateTime.formatTime()}",
            color = MaterialTheme.colorScheme.surface,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f)
        )
        HeaderButton(
            onClick = navigateToEditEvent,
            imageVector = Icons.Filled.Edit,
            contentDescription = Res.string.edit
        )
        DeleteEventHeaderButton(
            event = event,
            onDelete = deleteEvent,
            contentDescription = Res.string.delete
        )
    }
}

@Composable
fun EventBody(eventUiState: EventUiState, modifier: Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Dimensions.padding_medium),
        horizontalAlignment = Alignment.Start,
        modifier = modifier
    ) {
        EventDetailRow(
            labelResId = Res.string.description,
            detail = eventUiState.description,
        )
        EventDetailRow(
            labelResId = Res.string.location,
            detail = eventUiState.location,
        )
        EventBooleanDetailRow(
            labelResId = Res.string.is_online,
            detail = eventUiState.isOnline,
        )
        EventDetailRow(
            labelResId = Res.string.contact,
            detail = eventUiState.contact,
        )
        EventDetailRow(
            labelResId = Res.string.notes,
            detail = eventUiState.notes,
        )
        EventDetailRow(
            labelResId = Res.string.event_type,
            detail = stringResource(eventUiState.eventType.text),
        )
        EventDetailRow(
            labelResId = Res.string.event_category,
            detail = stringResource(eventUiState.eventCategory.text),
        )
        EventDetailRow(
            labelResId = Res.string.color,
            detail = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        color = eventUiState.htmlColor.color,
                        fontSize = MaterialTheme.typography.headlineLarge.fontSize
                    )
                ) {
                    append("• ")
                }
                append(eventUiState.htmlColor.text)
            },
        )
    }
}

@Composable
private fun EventDetailRow(
    labelResId: StringResource, detail: String?, modifier: Modifier = Modifier,
) {
    if (detail.isNullOrBlank()) return
    EventDetailRow(labelResId = labelResId, detail = AnnotatedString(detail), modifier = modifier)
}

@Composable
private fun EventDetailRow(
    labelResId: StringResource, detail: AnnotatedString?, modifier: Modifier = Modifier,
) {
    if (detail.isNullOrBlank()) return

    Row(modifier = modifier.fillMaxWidth().padding(horizontal = Dimensions.padding_medium)) {
        Text(stringResource(labelResId), fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.weight(1f))
        Text(text = detail)
    }
}

@Composable
private fun EventBooleanDetailRow(
    labelResId: StringResource, detail: Boolean, modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth().padding(horizontal = Dimensions.padding_medium)) {
        Text(stringResource(labelResId))
        Spacer(modifier = Modifier.weight(1f))
        Checkbox(checked = detail, onCheckedChange = {}, enabled = false)
    }
}

@Preview
@Composable
fun EventScreenPreview() {
    val event = DummyEventRepository().getDefaultEvents(1).first()
    EventDemoAppTheme {
        EventScreen(
            eventUiState = event.toEventUiState(),
            deleteEvent = {},
            navigateToEditEvent = {},
            modifier = Modifier
                .fillMaxSize()
                .background(Color.LightGray) // showBackground = true
        )
    }
}