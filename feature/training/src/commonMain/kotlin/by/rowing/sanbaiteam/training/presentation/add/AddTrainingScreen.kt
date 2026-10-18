package by.rowing.sanbaiteam.training.presentation.add

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import by.rowing.sanbaiteam.core.util.RowingTimeFormat
import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType
import by.rowing.sanbaiteam.training.presentation.platform.rememberTrainingDatePicker
import by.rowing.sanbaiteam.training.presentation.platform.rememberTrainingImportPicker
import by.rowing.sanbaiteam.training.resources.Res
import by.rowing.sanbaiteam.training.resources.add_training_athlete_header
import by.rowing.sanbaiteam.training.resources.add_training_crew_header
import by.rowing.sanbaiteam.training.resources.add_training_date_label
import by.rowing.sanbaiteam.training.resources.add_training_delete_piece
import by.rowing.sanbaiteam.training.resources.add_training_import_csv
import by.rowing.sanbaiteam.training.resources.add_training_import_notice_title
import by.rowing.sanbaiteam.training.resources.add_training_pace_placeholder
import by.rowing.sanbaiteam.training.resources.add_training_piece
import by.rowing.sanbaiteam.training.resources.add_training_piece_avg_heart_rate_label
import by.rowing.sanbaiteam.training.resources.add_training_piece_lactate_label
import by.rowing.sanbaiteam.training.resources.add_training_piece_length_label
import by.rowing.sanbaiteam.training.resources.add_training_piece_max_heart_rate_label
import by.rowing.sanbaiteam.training.resources.add_training_piece_pace_label
import by.rowing.sanbaiteam.training.resources.add_training_piece_recovery_heart_rate_label
import by.rowing.sanbaiteam.training.resources.add_training_piece_stroke_rate_label
import by.rowing.sanbaiteam.training.resources.add_training_piece_time_label
import by.rowing.sanbaiteam.training.resources.add_training_piece_time_placeholder
import by.rowing.sanbaiteam.training.resources.add_training_pieces_header
import by.rowing.sanbaiteam.training.resources.add_training_save
import by.rowing.sanbaiteam.training.resources.add_training_save_are_you_sure
import by.rowing.sanbaiteam.training.resources.add_training_select_athlete
import by.rowing.sanbaiteam.training.resources.add_training_title
import by.rowing.sanbaiteam.training.resources.add_training_validation_title
import by.rowing.sanbaiteam.training.resources.cancel
import by.rowing.sanbaiteam.training.resources.save
import by.rowing.sanbaiteam.training.resources.training_detail_piece_number
import by.rowing.sanbaiteam.uikit.component.SimpleTopAppBar
import by.rowing.sanbaiteam.uikit.component.TextField
import by.rowing.sanbaiteam.uikit.component.button.Button
import by.rowing.sanbaiteam.uikit.component.button.ButtonColors
import by.rowing.sanbaiteam.uikit.component.decimal
import by.rowing.sanbaiteam.uikit.component.number
import by.rowing.sanbaiteam.uikit.component.time
import by.rowing.sanbaiteam.uikit.theme.Spacing
import by.rowing.sanbaiteam.uikit.theme.Spacing.SpacerS
import by.rowing.sanbaiteam.uikit.theme.TypographyPalette
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun AddTrainingScreen(viewModel: AddTrainingViewModel) {
    val state = viewModel.state

    ParseInitialTrainingFile(viewModel = viewModel)

    Scaffold(
        topBar = addTrainingToolbar(onBackClick = viewModel::onBackClick),
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::showSaveTrainingDialog,
                containerColor = MaterialTheme.colorScheme.primary,
            ) { SaveIcon() }
        }
    ) { paddingValues ->
        AddTrainingDataInputs(
            paddingValues = paddingValues,
            state = state,
            viewModel = viewModel
        )

        AddTrainingAlerts(
            viewModel = viewModel,
            state = state,
        )
    }
}

@Composable
private fun AddTrainingAlerts(
    viewModel: AddTrainingViewModel,
    state: AddTrainingState,
) {
    if (state.showSaveDialog) {
        Alert(
            title = Res.string.add_training_save,
            text = stringResource(Res.string.add_training_save_are_you_sure),
            onDismiss = viewModel::hideSaveTrainingDialog,
            onConfirm = viewModel::saveTraining
        )
    }
    state.validationError?.let { error ->
        Alert(
            title = Res.string.add_training_validation_title,
            text = error.resolve(),
            onDismiss = viewModel::clearValidationError,
        )
    }
    state.importNotice?.let { notice ->
        Alert(
            title = Res.string.add_training_import_notice_title,
            text = notice.resolve(),
            onDismiss = viewModel::clearValidationError,
        )
    }
}

@Composable
private fun AddTrainingDataInputs(
    paddingValues: PaddingValues,
    state: AddTrainingState,
    viewModel: AddTrainingViewModel,
) {
    val launchImport = rememberTrainingImportPicker(onFilesPicked = viewModel::importTrainingBatch)
    Column(
        modifier = Modifier
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.M)
    ) {
        TrainingTypeDropdown(
            state = state,
            onTypeSelected = viewModel::changeTrainingType
        )
        Date(
            state = state,
            viewModel = viewModel
        )
        Text(
            text = stringResource(Res.string.add_training_crew_header),
            style = TypographyPalette.H4
        )
        SpacerS()
        AthleteDropdown(
            state = state,
            onAthleteSelected = viewModel::changeAthleteId
        )
        Text(
            text = stringResource(Res.string.add_training_pieces_header),
            style = TypographyPalette.H4
        )
        SpacerS()
        TrainingPieces(
            state = state,
            viewModel = viewModel
        )
        Button(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(Res.string.add_training_piece),
            debounceClick = viewModel::addPiece
        )
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Spacing.M),
            text = stringResource(Res.string.add_training_import_csv),
            debounceClick = launchImport
        )
    }
}

@Composable
private fun addTrainingToolbar(onBackClick: () -> Unit) = @Composable {
    SimpleTopAppBar(
        title = stringResource(Res.string.add_training_title),
        onNavIconClick = onBackClick
    )
}

@Composable
private fun ParseInitialTrainingFile(viewModel: AddTrainingViewModel) {
    LaunchedEffect(Unit) {
        val initialUri = viewModel.consumeInitialImportUri()
        if (!initialUri.isNullOrBlank()) {
            viewModel.importTrainingBatch(listOf(initialUri))
        }
    }
}

@Composable
private fun TrainingPieces(
    state: AddTrainingState,
    viewModel: AddTrainingViewModel
) {
    state.pieces.forEachIndexed { pieceIndex, piece ->
        PieceForm(
            index = pieceIndex,
            piece = piece,
            canRemove = state.pieces.size > 1,
            onDistanceChange = { newDistance ->
                viewModel.changePieceDistance(
                    pieceLocalId = piece.localId,
                    text = newDistance
                )
            },
            onTimeChange = { newTime ->
                viewModel.changePieceTime(
                    pieceLocalId = piece.localId,
                    text = newTime
                )
            },
            onStrokeRateChange = { newStrokeRate ->
                viewModel.changePieceStrokeRate(
                    pieceLocalId = piece.localId,
                    text = newStrokeRate
                )
            },
            onAvgHeartRateChange = { newValue ->
                viewModel.changePieceAvgHeartRate(
                    pieceLocalId = piece.localId,
                    text = newValue
                )
            },
            onMaxHeartRateChange = { newValue ->
                viewModel.changePieceMaxHeartRate(
                    pieceLocalId = piece.localId,
                    text = newValue
                )
            },
            onRecoveryHeartRateChange = { newValue ->
                viewModel.changePieceRecoveryHeartRate(
                    pieceLocalId = piece.localId,
                    text = newValue
                )
            },
            onLactateChange = { newValue ->
                viewModel.changePieceLactate(
                    pieceLocalId = piece.localId,
                    text = newValue
                )
            },
            onRemove = { viewModel.removePiece(pieceLocalId = piece.localId) }
        )
    }
}

@Composable
private fun Date(
    state: AddTrainingState,
    viewModel: AddTrainingViewModel
) {
    val launchDatePicker = rememberTrainingDatePicker(
        initialDateMillis = state.dateMillis,
        onDatePicked = viewModel::onDatePicked
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = Spacing.M)
            .clickable(onClick = launchDatePicker)
    ) {
        TextField(
            value = state.dateFormatted,
            onValueChange = {},
            label = stringResource(Res.string.add_training_date_label),
            textStyle = TypographyPalette.Body1Regular,
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SaveIcon() {
    Icon(
        imageVector = Icons.Default.Save,
        contentDescription = stringResource(Res.string.save)
    )
}

private val CompactFieldContentPadding = PaddingValues(
    horizontal = Spacing.S,
    vertical = Spacing.M
)

@Composable
private fun PieceForm(
    index: Int,
    piece: AddPieceDraft,
    canRemove: Boolean,
    onDistanceChange: (String) -> Unit,
    onTimeChange: (String) -> Unit,
    onStrokeRateChange: (String) -> Unit,
    onAvgHeartRateChange: (String) -> Unit,
    onMaxHeartRateChange: (String) -> Unit,
    onRecoveryHeartRateChange: (String) -> Unit,
    onLactateChange: (String) -> Unit,
    onRemove: () -> Unit,
) {
    val pace: String = RowingTimeFormat.formatPace(
        timeMillis = piece.timeMillis,
        distanceMeters = piece.distanceMeters
    ) ?: stringResource(Res.string.add_training_pace_placeholder)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = Spacing.S),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(Spacing.S),
            verticalArrangement = Arrangement.spacedBy(space = Spacing.XS)
        ) {
            Text(
                text = stringResource(Res.string.training_detail_piece_number, index + 1),
                style = TypographyPalette.Body2Medium
            )
            DistanceTimeSPMBlock(
                piece = piece,
                onDistanceChange = onDistanceChange,
                onTimeChange = onTimeChange,
                onStrokeRateChange = onStrokeRateChange
            )
            HeartRateLactateBlock(
                piece = piece,
                onAvgHeartRateChange = onAvgHeartRateChange,
                onMaxHeartRateChange = onMaxHeartRateChange,
                onRecoveryHeartRateChange = onRecoveryHeartRateChange,
                onLactateChange = onLactateChange
            )
            Text(
                text = stringResource(Res.string.add_training_piece_pace_label, pace),
                style = TypographyPalette.Body2Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (canRemove) {
                RemovePieceButton(onRemove = onRemove)
            }
        }
    }
}

@Composable
private fun DistanceTimeSPMBlock(
    piece: AddPieceDraft,
    onDistanceChange: (String) -> Unit,
    onTimeChange: (String) -> Unit,
    onStrokeRateChange: (String) -> Unit
) {
    DataInputRow {
        TextField(
            value = piece.distanceText,
            onValueChange = onDistanceChange,
            label = stringResource(Res.string.add_training_piece_length_label),
            contentPadding = CompactFieldContentPadding,
            textStyle = TypographyPalette.Body1Regular,
            keyboardOptions = KeyboardOptions.number(),
            modifier = Modifier.weight(2f)
        )
        TextField(
            value = piece.timeText,
            onValueChange = onTimeChange,
            label = stringResource(Res.string.add_training_piece_time_label),
            contentPadding = CompactFieldContentPadding,
            placeholder = stringResource(Res.string.add_training_piece_time_placeholder),
            textStyle = TypographyPalette.Body1Regular,
            keyboardOptions = KeyboardOptions.time(),
            modifier = Modifier.weight(3f)
        )
        TextField(
            value = piece.strokeRateText,
            onValueChange = onStrokeRateChange,
            label = stringResource(Res.string.add_training_piece_stroke_rate_label),
            contentPadding = CompactFieldContentPadding,
            textStyle = TypographyPalette.Body1Regular,
            keyboardOptions = KeyboardOptions.decimal(),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun HeartRateLactateBlock(
    piece: AddPieceDraft,
    onAvgHeartRateChange: (String) -> Unit,
    onMaxHeartRateChange: (String) -> Unit,
    onRecoveryHeartRateChange: (String) -> Unit,
    onLactateChange: (String) -> Unit
) {
    DataInputRow {
        TextField(
            value = piece.avgHeartRate?.toString().orEmpty(),
            onValueChange = onAvgHeartRateChange,
            label = stringResource(Res.string.add_training_piece_avg_heart_rate_label),
            contentPadding = CompactFieldContentPadding,
            textStyle = TypographyPalette.Body1Regular,
            keyboardOptions = KeyboardOptions.number(),
            modifier = Modifier.weight(1f)
        )
        TextField(
            value = piece.maxHeartRate?.toString().orEmpty(),
            onValueChange = onMaxHeartRateChange,
            label = stringResource(Res.string.add_training_piece_max_heart_rate_label),
            contentPadding = CompactFieldContentPadding,
            textStyle = TypographyPalette.Body1Regular,
            keyboardOptions = KeyboardOptions.number(),
            modifier = Modifier.weight(1f)
        )
        TextField(
            value = piece.recoveryHeartRate?.toString().orEmpty(),
            onValueChange = onRecoveryHeartRateChange,
            label = stringResource(Res.string.add_training_piece_recovery_heart_rate_label),
            contentPadding = CompactFieldContentPadding,
            textStyle = TypographyPalette.Body1Regular,
            keyboardOptions = KeyboardOptions.number(imeAction = ImeAction.Next),
            modifier = Modifier.weight(1f)
        )
        TextField(
            value = piece.lactateText,
            onValueChange = onLactateChange,
            label = stringResource(Res.string.add_training_piece_lactate_label),
            contentPadding = CompactFieldContentPadding,
            textStyle = TypographyPalette.Body1Regular,
            keyboardOptions = KeyboardOptions.decimal(),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun DataInputRow(content: @Composable (RowScope.() -> Unit)) = Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(Spacing.XS),
    verticalAlignment = Alignment.CenterVertically,
    content = content
)

@Composable
private fun RemovePieceButton(onRemove: () -> Unit) {
    Button(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(Res.string.add_training_delete_piece),
        buttonColors = ButtonColors.text(),
        debounceClick = onRemove
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AthleteDropdown(
    state: AddTrainingState,
    onAthleteSelected: (crewId: Long, athleteId: Long) -> Unit
) {
    val rowers = state.rowersCatalog
    val selectedRower = state.crewAthletes.first()
    ExposedSelectionDropdown(
        selectedLabel = rowers.find { it.id == selectedRower.rowerId }?.name
            ?: stringResource(Res.string.add_training_select_athlete),
        items = rowers,
        label = stringResource(Res.string.add_training_athlete_header),
        itemText = { rower -> rower.name },
        onItemSelected = { rower -> onAthleteSelected(selectedRower.crewId, rower.id) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrainingTypeDropdown(
    state: AddTrainingState,
    onTypeSelected: (trainingType: TrainingPieceType) -> Unit
) {
    ExposedSelectionDropdown(
        selectedLabel = stringResource(state.trainingType.uiResId),
        items = TrainingPieceType.entries,
        itemText = { trainingType -> stringResource(trainingType.uiResId) },
        onItemSelected = onTypeSelected
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> ExposedSelectionDropdown(
    selectedLabel: String,
    items: List<T>,
    itemText: @Composable (item: T) -> String,
    onItemSelected: (item: T) -> Unit,
    label: String? = null
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        TextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = label,
            textStyle = TypographyPalette.Body1Regular,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = itemText(item),
                            style = TypographyPalette.Body1Regular
                        )
                    },
                    onClick = {
                        expanded = false
                        onItemSelected(item)
                    }
                )
            }
        }
    }
}

@Composable
private fun Alert(
    title: StringResource,
    text: String,
    onDismiss: () -> Unit,
    onConfirm: (() -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(title),
                style = TypographyPalette.H4
            )
        },
        text = {
            Text(
                text = text,
                style = TypographyPalette.Body1Regular
            )
        },
        confirmButton = {
            Button(
                text = stringResource(if (onConfirm != null) Res.string.save else Res.string.cancel),
                buttonColors = ButtonColors.text(),
                debounceClick = {
                    onDismiss()
                    onConfirm?.invoke()
                }
            )
        },
        dismissButton = onConfirm?.let {
            {
                Button(
                    text = stringResource(Res.string.cancel),
                    buttonColors = ButtonColors.text(),
                    debounceClick = onDismiss
                )
            }
        }
    )
}
