package com.github.mheerwaarden.eventdemo.ui.screen.license

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.mheerwaarden.eventdemo.resources.Res
import com.github.mheerwaarden.eventdemo.resources.accept
import com.github.mheerwaarden.eventdemo.resources.accepted_at
import com.github.mheerwaarden.eventdemo.resources.i_agree
import com.github.mheerwaarden.eventdemo.resources.license_content
import com.github.mheerwaarden.eventdemo.resources.license_title
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource

@Composable
fun LicenseScreen(
    viewModel: LicenseViewModel,
    userId: String,
    version: String = "1.0",
    onAccepted: () -> Unit
) {
    var checked by remember { mutableStateOf(false) }
    var accepted by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(Res.string.license_title),
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(12.dp)
        ) {
            Text(text = stringResource(Res.string.license_content))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = checked,
                onCheckedChange = { checked = it }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(Res.string.i_agree))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                accepted = true
                viewModel.acceptLicense(userId, version)
                onAccepted()
            },
            enabled = checked && !accepted,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(Res.string.accept))
        }

        if (accepted) {
            Text(
                text = "${stringResource(Res.string.accepted_at)}: ${Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}