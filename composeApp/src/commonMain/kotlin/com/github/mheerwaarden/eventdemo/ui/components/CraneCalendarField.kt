package com.github.mheerwaarden.eventdemo.ui.components


import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.github.mheerwaarden.eventdemo.resources.Res
import com.github.mheerwaarden.eventdemo.resources.select_date
import com.github.mheerwaarden.eventdemo.resources.show_date_picker
import com.github.mheerwaarden.eventdemo.util.format
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun CraneCalendarField(
    startDate: LocalDate? = null,
    endDate: LocalDate? = null,
    onDateChange: (LocalDate?, LocalDate?) -> Unit,
    modifier: Modifier = Modifier,
    labelId: StringResource = Res.string.select_date,
) {
    // String value showing start date - end date
    var period by rememberSaveable { mutableStateOf("") }
    period = "${startDate?.format() ?: ""} - ${endDate?.format() ?: ""}"

    DialogField(
        label = stringResource(labelId),
        value = period,
        modifier = modifier.fillMaxWidth(),
        isRequired = true,
        trailingIcon = {
            Icon(
                Icons.Filled.EditCalendar,
                contentDescription = stringResource(Res.string.show_date_picker),
            )
        },
        onShowDialog = { onClose ->
            ShowCraneCalendarDialog(
                startDate = startDate,
                endDate = endDate,
                onDateChange = { startDate, endDate ->
                    period = "${startDate?.format() ?: ""} - ${endDate?.format() ?: ""}"
                    onDateChange(startDate, endDate)
                    onClose()
                },
                onDismiss = { onClose() },
            )
        }
    )
}
