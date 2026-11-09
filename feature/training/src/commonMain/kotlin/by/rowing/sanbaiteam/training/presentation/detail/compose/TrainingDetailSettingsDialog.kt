package by.rowing.sanbaiteam.training.presentation.detail.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import by.rowing.sanbaiteam.core.util.RowingTimeFormat
import by.rowing.sanbaiteam.training.data.chart.models.TrainingSplitLength
import by.rowing.sanbaiteam.training.data.models.detail.TrainingDetailField
import by.rowing.sanbaiteam.training.data.models.detail.canToggle
import by.rowing.sanbaiteam.training.resources.Res
import by.rowing.sanbaiteam.training.resources.training_detail_avg_heart_rate_label
import by.rowing.sanbaiteam.training.resources.training_detail_distance_label
import by.rowing.sanbaiteam.training.resources.training_detail_lactate_label
import by.rowing.sanbaiteam.training.resources.training_detail_max_heart_rate_label
import by.rowing.sanbaiteam.training.resources.training_detail_pace_label
import by.rowing.sanbaiteam.training.resources.training_detail_percent_of_pb_label
import by.rowing.sanbaiteam.training.resources.training_detail_recovery_heart_rate_label
import by.rowing.sanbaiteam.training.resources.training_detail_settings_done
import by.rowing.sanbaiteam.training.resources.training_detail_settings_fields
import by.rowing.sanbaiteam.training.resources.training_detail_settings_last_field
import by.rowing.sanbaiteam.training.resources.training_detail_settings_show_all
import by.rowing.sanbaiteam.training.resources.training_detail_settings_title
import by.rowing.sanbaiteam.training.resources.training_detail_stroke_rate_label
import by.rowing.sanbaiteam.training.resources.training_detail_time_label
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
import by.rowing.sanbaiteam.uikit.component.Checkbox
import by.rowing.sanbaiteam.uikit.component.CheckedState
import by.rowing.sanbaiteam.uikit.component.Chip
import by.rowing.sanbaiteam.uikit.component.TextField
import by.rowing.sanbaiteam.uikit.component.button.Button
import by.rowing.sanbaiteam.uikit.theme.Spacing
import by.rowing.sanbaiteam.uikit.theme.TypographyPalette
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private val DISTANCE_PRESETS = listOf(250, 500, 1000)
private val TIME_PRESETS = listOf(60_000L, 120_000L, 180_000L)
private const val CHECKBOX_COLUMNS = 2
private const val LAST_VISIBLE_FIELD_COUNT = 1

@Composable
internal fun TrainingDetailSettingsDialog(
    currentSplit: TrainingSplitLength?,
    hiddenFields: Set<TrainingDetailField>,
    actions: TrainingDetailActions,
) {
    var isTime by remember { mutableStateOf(currentSplit is TrainingSplitLength.Time) }
    var input by remember { mutableStateOf(initialSplitInput(currentSplit)) }
    var hasError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = actions::onSettingsDismiss,
        title = {
            Text(
                text = stringResource(Res.string.training_detail_settings_title),
                style = TypographyPalette.H4
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Spacing.M)
            ) {
                SplitSection(
                    isTime = isTime,
                    input = input,
                    hasError = hasError,
                    hasSplit = currentSplit != null,
                    onModeChange = { time ->
                        isTime = time
                        input = ""
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
                    onApply = {
                        parseSplitLength(
                            input = input,
                            isTime = isTime
                        )?.let(actions::onSplitApply) ?: run { hasError = true }
                    },
                    onClear = {
                        input = ""
                        hasError = false
                        actions.onSplitClear()
                    },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                FieldsSection(
                    hiddenFields = hiddenFields,
                    onFieldToggle = actions::onFieldToggle,
                    onShowAllFields = actions::onShowAllFields,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = actions::onSettingsDismiss) {
                Text(text = stringResource(Res.string.training_detail_settings_done))
            }
        },
    )
}

@Composable
private fun SplitSection(
    isTime: Boolean,
    input: String,
    hasError: Boolean,
    hasSplit: Boolean,
    onModeChange: (Boolean) -> Unit,
    onInputChange: (String) -> Unit,
    onPresetSelect: (String) -> Unit,
    onApply: () -> Unit,
    onClear: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.S)) {
        Text(
            text = stringResource(Res.string.training_split_title),
            style = TypographyPalette.H4
        )
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
        Button(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(Res.string.training_split_apply),
            debounceClick = onApply,
        )
        if (hasSplit) {
            TextButton(onClick = onClear) {
                Text(
                    text = stringResource(Res.string.training_split_clear),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun FieldsSection(
    hiddenFields: Set<TrainingDetailField>,
    onFieldToggle: (TrainingDetailField) -> Unit,
    onShowAllFields: () -> Unit,
) {
    val visibleCount = TrainingDetailField.entries.count { field -> field !in hiddenFields }
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.XS)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = stringResource(Res.string.training_detail_settings_fields),
                style = TypographyPalette.H4
            )
            if (hiddenFields.isNotEmpty()) {
                TextButton(
                    contentPadding = PaddingValues(horizontal = Spacing.XS),
                    onClick = onShowAllFields,
                ) {
                    Text(
                        text = stringResource(Res.string.training_detail_settings_show_all),
                        style = TypographyPalette.Body2Medium
                    )
                }
            }
        }
        TrainingDetailField.entries
            .chunked(size = CHECKBOX_COLUMNS)
            .forEach { rowFields ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.S)
                ) {
                    rowFields.forEach { field ->
                        Checkbox(
                            modifier = Modifier.weight(1f),
                            label = stringResource(field.labelRes()),
                            checked = if (field in hiddenFields) {
                                CheckedState.UNCHECKED
                            } else {
                                CheckedState.CHECKED
                            },
                            enabled = hiddenFields.canToggle(field = field),
                            contentPadding = PaddingValues(
                                horizontal = Spacing._2XS,
                                vertical = Spacing._2XS
                            ),
                            click = { onFieldToggle(field) },
                        )
                    }
                    if (rowFields.size < CHECKBOX_COLUMNS) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        if (visibleCount == LAST_VISIBLE_FIELD_COUNT) {
            Text(
                text = stringResource(Res.string.training_detail_settings_last_field),
                style = TypographyPalette.Caption1Regular,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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

private fun TrainingDetailField.labelRes(): StringResource = when (this) {
    TrainingDetailField.DISTANCE -> Res.string.training_detail_distance_label
    TrainingDetailField.TIME -> Res.string.training_detail_time_label
    TrainingDetailField.PACE -> Res.string.training_detail_pace_label
    TrainingDetailField.STROKE_RATE -> Res.string.training_detail_stroke_rate_label
    TrainingDetailField.AVG_HEART_RATE -> Res.string.training_detail_avg_heart_rate_label
    TrainingDetailField.MAX_HEART_RATE -> Res.string.training_detail_max_heart_rate_label
    TrainingDetailField.RECOVERY_HEART_RATE -> Res.string.training_detail_recovery_heart_rate_label
    TrainingDetailField.LACTATE -> Res.string.training_detail_lactate_label
    TrainingDetailField.PERCENT_OF_PB -> Res.string.training_detail_percent_of_pb_label
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
    null -> ""
}

private fun parseSplitLength(
    input: String,
    isTime: Boolean,
): TrainingSplitLength? = if (isTime) {
    RowingTimeFormat.parseTimeInput(input)?.takeIf { millis -> millis > 0L }?.let(TrainingSplitLength::Time)
} else {
    input.toIntOrNull()?.takeIf { meters -> meters > 0 }?.let(TrainingSplitLength::Distance)
}