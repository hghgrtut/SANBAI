package by.rowing.sanbaiteam.uikit.component

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.window.DialogProperties
import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic
import by.rowing.sanbaiteam.uikit.resources.Res
import by.rowing.sanbaiteam.uikit.resources.uikit_cancel
import by.rowing.sanbaiteam.uikit.resources.uikit_ok
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@AllowDetektPublic
@Composable
actual fun rememberDatePicker(
    initialDateMillis: Long,
    onDatePicked: (year: Int, month: Int, dayOfMonth: Int) -> Unit,
): () -> Unit {
    val openDialog = remember { mutableStateOf(false) }

    if (openDialog.value) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDateMillis)
        DatePickerDialog(
            onDismissRequest = { openDialog.value = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        openDialog.value = false
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = Instant.fromEpochMilliseconds(epochMilliseconds = millis)
                                .toLocalDateTime(timeZone = TimeZone.currentSystemDefault())
                            onDatePicked(date.year, date.month.number, date.day)
                        }
                    }
                ) { Text(text = stringResource(resource = Res.string.uikit_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { openDialog.value = false }) {
                    Text(text = stringResource(resource = Res.string.uikit_cancel))
                }
            },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(),
            )
        }
    }

    return remember {
        { openDialog.value = true }
    }
}
