package by.rowing.sanbaiteam.training.presentation.detail.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import by.rowing.sanbaiteam.core.util.RowingTimeFormat
import by.rowing.sanbaiteam.training.data.chart.models.TrainingSplitLength
import by.rowing.sanbaiteam.training.resources.Res
import by.rowing.sanbaiteam.training.resources.cancel
import by.rowing.sanbaiteam.training.resources.training_split_apply
import by.rowing.sanbaiteam.training.resources.training_split_clear
import by.rowing.sanbaiteam.training.resources.training_split_error
import by.rowing.sanbaiteam.training.resources.training_split_length_label
import by.rowing.sanbaiteam.training.resources.training_split_length_placeholder_distance
import by.rowing.sanbaiteam.training.resources.training_split_length_placeholder_time
import by.rowing.sanbaiteam.training.resources.training_split_mode_distance
import by.rowing.sanbaiteam.training.resources.training_split_mode_time
import by.rowing.sanbaiteam.training.resources.training_split_preset_distance
import by.rowing.sanbaiteam.training.resources.training_split_preset_time
import by.rowing.sanbaiteam.training.resources.training_split_title
import by.rowing.sanbaiteam.uikit.component.Chip
import by.rowing.sanbaiteam.uikit.component.TextField
import by.rowing.sanbaiteam.uikit.component.button.Button
import by.rowing.sanbaiteam.uikit.theme.Spacing
import by.rowing.sanbaiteam.uikit.theme.TypographyPalette
import org.jetbrains.compose.resources.stringResource

private const val DEFAULT_DISTANCE_METERS = "500"
private const val DEFAULT_TIME = "2:00"
private val DISTANCE_PRESETS = listOf(250, 500, 1000)
private val TIME_PRESETS = listOf(60_000L, 120_000L, 180_000L)

@Composable
internal fun TrainingSplitDialog(
    current: TrainingSplitLength?,
    onDismiss: () -> Unit,
    onApply: (TrainingSplitLength) -> Unit,
    onClear: () -> Unit,
) {
    var isTime by remember { mutableStateOf(current is TrainingSplitLength.Time) }
    var input by remember { mutableStateOf(initialSplitInput(current)) }
    var hasError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(Res.string.training_split_title),
                style = TypographyPalette.H4
            )
        },
        text = {
            SplitDialogForm(
                isTime = isTime,
                input = input,
                hasError = hasError,
                onModeChange = { time ->
                    isTime = time
                    input = if (isTime) DEFAULT_TIME else DEFAULT_DISTANCE_METERS
                    hasError = false
                },
                onInputChange = { value ->
                    input = sanitizeSplitInput(
                        value = value,
                        isTime = isTime
                    )
                    hasError = false
                },
                onPresetSelect = { preset ->
                    input = preset
                    hasError = false
                },
            )
        },
        confirmButton = {
            Button(
                text = stringResource(Res.string.training_split_apply),
                debounceClick = {
                    parseSplitLength(
                        input = input,
                        isTime = isTime
                    )?.let { length -> onApply(length) } ?: run { hasError = true }
                }
            )
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.XS)) {
                if (current != null) {
                    TextButton(onClick = onClear) {
                        Text(
                            text = stringResource(Res.string.training_split_clear),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                TextButton(onClick = onDismiss) { Text(text = stringResource(Res.string.cancel)) }
            }
        },
    )
}

@Composable
private fun SplitDialogForm(
    isTime: Boolean,
    input: String,
    hasError: Boolean,
    onModeChange: (Boolean) -> Unit,
    onInputChange: (String) -> Unit,
    onPresetSelect: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.S)) {
        SplitModeSelector(
            isTime = isTime,
            onModeChange = onModeChange
        )
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = input,
            onValueChange = onInputChange,
            label = stringResource(Res.string.training_split_length_label),
            placeholder = stringResource(
                if (isTime) {
                    Res.string.training_split_length_placeholder_time
                } else {
                    Res.string.training_split_length_placeholder_distance
                }
            ),
            error = hasError,
            supportingText = stringResource(Res.string.training_split_error).takeIf { hasError },
            keyboardOptions = KeyboardOptions(keyboardType = if (isTime) KeyboardType.Text else KeyboardType.Number),
        )
        SplitPresets(
            isTime = isTime,
            selectedInput = input,
            onSelect = onPresetSelect,
        )
    }
}

@Composable
private fun SplitModeSelector(
    isTime: Boolean,
    onModeChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.XS)
    ) {
        Chip(
            text = stringResource(Res.string.training_split_mode_distance),
            selected = !isTime,
            onClick = { onModeChange(false) }
        )
        Chip(
            text = stringResource(Res.string.training_split_mode_time),
            selected = isTime,
            onClick = { onModeChange(true) }
        )
    }
}

@Composable
private fun SplitPresets(
    isTime: Boolean,
    selectedInput: String,
    onSelect: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.XS)
    ) {
        if (isTime) {
            TIME_PRESETS.forEach { millis ->
                val text = RowingTimeFormat.formatDurationMmSs(millis)
                Chip(
                    text = stringResource(Res.string.training_split_preset_time, text),
                    selected = selectedInput == text,
                    onClick = { onSelect(text) }
                )
            }
        } else {
            DISTANCE_PRESETS.forEach { meters ->
                val text = meters.toString()
                Chip(
                    text = stringResource(Res.string.training_split_preset_distance, meters),
                    selected = selectedInput == text,
                    onClick = { onSelect(text) }
                )
            }
        }
    }
}

private fun sanitizeSplitInput(
    value: String,
    isTime: Boolean,
): String = if (isTime) {
    RowingTimeFormat.sanitizeTimeInput(value)
} else {
    value.filter { symbol -> symbol.isDigit() }
}

private fun initialSplitInput(current: TrainingSplitLength?): String = when (current) {
    is TrainingSplitLength.Distance -> current.meters.toString()
    is TrainingSplitLength.Time -> RowingTimeFormat.formatDurationMmSs(current.millis)
    null -> DEFAULT_DISTANCE_METERS
}

private fun parseSplitLength(
    input: String,
    isTime: Boolean,
): TrainingSplitLength? = if (isTime) {
    RowingTimeFormat.parseTimeInput(input)?.takeIf { millis -> millis > 0L }?.let(TrainingSplitLength::Time)
} else {
    input.toIntOrNull()?.takeIf { meters -> meters > 0 }?.let(TrainingSplitLength::Distance)
}