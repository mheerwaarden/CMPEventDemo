/*
 * Copyright (c) 2025. Marcel Van Heerwaarden
 * @Author Marcel van Heerwaarden
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.github.mheerwaarden.eventdemo.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.github.mheerwaarden.eventdemo.resources.Res
import com.github.mheerwaarden.eventdemo.resources.attention
import com.github.mheerwaarden.eventdemo.resources.delete_question
import com.github.mheerwaarden.eventdemo.resources.no
import com.github.mheerwaarden.eventdemo.resources.yes
import org.jetbrains.compose.resources.stringResource

@Composable
fun DeleteConfirmationDialog(
    deleteActionName: String,
    onRemoveConfirm: () -> Unit,
    onCancelCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = { /* Do nothing */ },
        title = { Text(stringResource(Res.string.attention)) },
        text = {
            Text(
                stringResource(
                    Res.string.delete_question,
                    deleteActionName.lowercase()
                )
            )
        },
        modifier = modifier,
        dismissButton = {
            TextButton(onClick = onCancelCancel) {
                Text(stringResource(Res.string.no))
            }
        },
        confirmButton = {
            TextButton(onClick = onRemoveConfirm) {
                Text(stringResource(Res.string.yes))
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
    )
}