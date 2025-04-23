/*
 * Copyright (c) 2025. Marcel Van Heerwaarden
 * @Author Marcel van Heerwaarden
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.github.mheerwaarden.eventdemo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.github.mheerwaarden.eventdemo.Dimensions
import com.github.mheerwaarden.eventdemo.data.database.DummyEventRepository
import com.github.mheerwaarden.eventdemo.data.model.ModelItem
import com.github.mheerwaarden.eventdemo.resources.Res
import com.github.mheerwaarden.eventdemo.resources.show_selection_dialog
import com.github.mheerwaarden.eventdemo.ui.theme.EventDemoAppTheme
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun SelectionField(
    label: String,
    currentItem: ModelItem?,
    onGetItems: () -> List<ModelItem>,
    onChange: (ModelItem) -> Unit,
    modifier: Modifier = Modifier,
    isRequired: Boolean = true,
) {
    // String value of the item
    var value by rememberSaveable { mutableStateOf("") }
    value = currentItem?.getDisplayName() ?: ""

    DialogField(
        label = label,
        value = value,
        modifier = modifier.fillMaxWidth(),
        isRequired = isRequired,
        trailingIcon = {
            Icon(
                Icons.Filled.Search,
                contentDescription = stringResource(Res.string.show_selection_dialog),
            )
        },
        { close ->
            SelectionDialog(
                label = label,
                currentDisplayValue = value,
                items = onGetItems(),
                onChange = onChange,
                onDismissRequest = { close() },
            )
        })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionDialog(
    label: String,
    currentDisplayValue: String,
    items: List<ModelItem>,
    onChange: (ModelItem) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Defaults equal to DatePickerDialog
    BasicAlertDialog(
        onDismissRequest = onDismissRequest, modifier = modifier.wrapContentHeight()
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Box(
                modifier = Modifier.padding(Dimensions.padding_large).border(
                        width = Dimensions.border_width,
                        color = MaterialTheme.colorScheme.surfaceTint
                    )
            ) {
                val evenColor = MaterialTheme.colorScheme.surfaceContainer
                val oddColor = MaterialTheme.colorScheme.surfaceVariant
                LazyColumn {
                    item {
                        Text(
                            text = label,
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth()
                                .background(color = MaterialTheme.colorScheme.surfaceTint)
                                .padding(Dimensions.padding_small)
                        )
                    }
                    itemsIndexed(items = items, key = { _, item -> item.id }) { index, item ->
                        val displayName = item.getDisplayName()
                        val isSelected = displayName == currentDisplayValue
                        val backgroundColor = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            if (index % 2 == 0) evenColor else oddColor
                        }
                        val textColor = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            Color.Unspecified
                        }
                        Text(
                            text = displayName,
                            color = textColor,
                            modifier = Modifier.fillMaxWidth().background(color = backgroundColor)
                                .padding(Dimensions.padding_small).clickable {
                                    onChange(item)
                                    onDismissRequest()
                                })
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun SelectionDialogPreview() {
    EventDemoAppTheme {
        SelectionDialog(
            label = "Event",
            currentDisplayValue = "Birthday",
            items = DummyEventRepository().getDefaultEvents(6),
            onChange = { },
            onDismissRequest = { },
            modifier = Modifier.fillMaxSize().background(Color.LightGray) // showBackground = true
        )
    }
}