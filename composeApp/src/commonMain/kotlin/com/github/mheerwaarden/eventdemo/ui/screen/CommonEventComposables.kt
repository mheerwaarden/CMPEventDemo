/*
 * Copyright (c) 2025. Marcel Van Heerwaarden
 * @Author Marcel van Heerwaarden
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.github.mheerwaarden.eventdemo.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.github.mheerwaarden.eventdemo.Dimensions
import com.github.mheerwaarden.eventdemo.data.model.Event
import com.github.mheerwaarden.eventdemo.resources.Res
import com.github.mheerwaarden.eventdemo.resources.add
import com.github.mheerwaarden.eventdemo.resources.delete
import com.github.mheerwaarden.eventdemo.resources.edit
import com.github.mheerwaarden.eventdemo.resources.remove
import com.github.mheerwaarden.eventdemo.ui.components.DeleteConfirmationDialog
import com.github.mheerwaarden.eventdemo.ui.util.DISABLED_ICON_OPACITY
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddItemButton(
    navigateToAddScreen: () -> Unit,
    modifier: Modifier = Modifier,
    foregroundColor: Color = MaterialTheme.colorScheme.surface,
    contentDescription: StringResource = Res.string.add,
) {
    HeaderButton(
        onClick = navigateToAddScreen,
        foregroundColor = foregroundColor,
        imageVector = Icons.Filled.Add,
        contentDescription = contentDescription,
        modifier = modifier
    )
}

@Composable
fun HeaderButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    foregroundColor: Color = MaterialTheme.colorScheme.surface,
    imageVector: ImageVector = Icons.Filled.Add,
    contentDescription: StringResource = Res.string.add,
) {

    IconButton(
        onClick = onClick,
        colors = IconButtonColors(
            containerColor = Color.Transparent,
            contentColor = foregroundColor,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = foregroundColor.copy(alpha = DISABLED_ICON_OPACITY)
        ),
        modifier = modifier
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = stringResource(contentDescription)
        )
    }
}

@Composable
fun DeleteEventHeaderButton(
    event: Event,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    foregroundColor: Color = MaterialTheme.colorScheme.surface,
    contentDescription: StringResource = Res.string.delete,
) {
    var deleteConfirmationDialogRequired by rememberSaveable { mutableStateOf(false) }
    IconButton(
        onClick = { deleteConfirmationDialogRequired = true },
        colors = IconButtonColors(
            containerColor = Color.Transparent,
            contentColor = foregroundColor,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = foregroundColor.copy(alpha = DISABLED_ICON_OPACITY)
        ),
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Filled.Delete,
            contentDescription = stringResource(contentDescription)
        )
    }
    if (deleteConfirmationDialogRequired) {
        DeleteConfirmationDialog(
            deleteActionName = getDisplayName(event),
            onRemoveConfirm = {
                deleteConfirmationDialogRequired = false
                onDelete()
            },
            onCancelCancel = { deleteConfirmationDialogRequired = false },
            modifier = Modifier.padding(Dimensions.padding_medium)
        )
    }
}

@Composable
fun EditItemButtons(
    event: Event,
    editIconButtonColors: IconButtonColors,
    onDelete: (Long) -> Unit,
    navigateToEditScreen: (Long) -> Unit,
    modifier: Modifier = Modifier,
    editContentDescription: StringResource = Res.string.edit,
    deleteContentDescription: StringResource = Res.string.delete,
) {
    var deleteConfirmationDialogRequired by rememberSaveable { mutableStateOf(false) }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        IconButton(
            onClick = { navigateToEditScreen(event.id) }, colors = editIconButtonColors
        ) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = stringResource(editContentDescription)
            )
        }
        IconButton(
            onClick = { deleteConfirmationDialogRequired = true }, colors = editIconButtonColors
        ) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = stringResource(deleteContentDescription)
            )
        }
        if (deleteConfirmationDialogRequired) {
            DeleteConfirmationDialog(
                deleteActionName = getDisplayName(event),
                onRemoveConfirm = {
                    deleteConfirmationDialogRequired = false
                    onDelete(event.id)
                },
                onCancelCancel = { deleteConfirmationDialogRequired = false },
                modifier = Modifier.padding(Dimensions.padding_medium)
            )
        }
    }
}

@Composable
fun RemoveItemButton(
    event: Event,
    editIconButtonColors: IconButtonColors,
    onRemove: (Long) -> Unit,
    modifier: Modifier = Modifier,
    removeContentDescription: StringResource = Res.string.remove,
) {
    var deleteConfirmationDialogRequired by rememberSaveable { mutableStateOf(false) }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        IconButton(
            onClick = { deleteConfirmationDialogRequired = true }, colors = editIconButtonColors
        ) {
            Icon(
                imageVector = Icons.Filled.Remove,
                contentDescription = stringResource(removeContentDescription)
            )
        }
        if (deleteConfirmationDialogRequired) {
            DeleteConfirmationDialog(
                deleteActionName = getDisplayName(event),
                onRemoveConfirm = {
                    deleteConfirmationDialogRequired = false
                    onRemove(event.id)
                },
                onCancelCancel = { deleteConfirmationDialogRequired = false },
                modifier = Modifier.padding(Dimensions.padding_medium)
            )
        }
    }
}

@Composable
fun getDisplayName(event: Event): String  = "${stringResource(event.eventType.text)} ${event.description}"
