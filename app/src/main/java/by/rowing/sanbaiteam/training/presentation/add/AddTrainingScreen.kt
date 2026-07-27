package by.rowing.sanbaiteam.training.presentation.add

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import by.rowing.sanbaiteam.R
import by.rowing.sanbaiteam.core.util.RowingTimeFormat
import by.rowing.sanbaiteam.uikit.component.SimpleTopAppBar
import by.rowing.sanbaiteam.uikit.component.TextField
import by.rowing.sanbaiteam.uikit.component.button.Button
import by.rowing.sanbaiteam.uikit.component.button.ButtonColors
import by.rowing.sanbaiteam.uikit.theme.Spacing
import by.rowing.sanbaiteam.uikit.theme.Spacing.SpacerM
import by.rowing.sanbaiteam.uikit.theme.Spacing.SpacerS
import by.rowing.sanbaiteam.uikit.theme.Spacing.SpacerXS
import by.rowing.sanbaiteam.uikit.theme.TypographyPalette
import java.util.Calendar

@Composable
internal fun AddTrainingScreen(viewModel: AddTrainingViewModel) {
    var showSaveDialog by remember { mutableStateOf(false) }
    val state = viewModel.state
    val context = LocalContext.current

    Scaffold(
        topBar = {
            SimpleTopAppBar(
                title = stringResource(R.string.add_training_title),
                onNavIconClick = viewModel::onBackClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showSaveDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = stringResource(R.string.save)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.M)
        ) {
            Text(
                text = stringResource(R.string.training_piece_type_single),
                style = TypographyPalette.Body2Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            SpacerXS()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val calendar = Calendar.getInstance().apply { timeInMillis = state.dateMillis }
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                viewModel.onDatePicked(year, month, dayOfMonth)
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }
            ) {
                TextField(
                    value = state.dateFormatted,
                    onValueChange = {},
                    label = stringResource(R.string.add_training_date_label),
                    textStyle = TypographyPalette.Body1Regular,
                    enabled = false,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            SpacerM()

            state.athleteSessions.forEachIndexed { index, session ->
                AthleteSessionCard(
                    index = index,
                    session = session,
                    state = state,
                    canRemove = state.athleteSessions.size > 1,
                    onAthleteSelected = { viewModel.changeAthleteId(session.localId, it) },
                    onRemoveSession = { viewModel.removeAthleteSession(session.localId) },
                    onAddPiece = { viewModel.addPiece(session.localId) },
                    onRemovePiece = { pieceLocalId ->
                        viewModel.removePiece(session.localId, pieceLocalId)
                    },
                    onDistanceChange = { pieceLocalId, text ->
                        viewModel.changePieceDistance(session.localId, pieceLocalId, text)
                    },
                    onTimeChange = { pieceLocalId, text ->
                        viewModel.changePieceTime(session.localId, pieceLocalId, text)
                    },
                    onStrokeRateChange = { pieceLocalId, text ->
                        viewModel.changePieceStrokeRate(session.localId, pieceLocalId, text)
                    }
                )
                SpacerM()
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.add_training_piece_athlete),
                debounceClick = viewModel::addAthleteSession
            )
            SpacerS()
            Button(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.add_training_save),
                debounceClick = { showSaveDialog = true }
            )
            SpacerM()
        }

        if (showSaveDialog) {
            SaveTrainingDialog(
                onConfirm = {
                    showSaveDialog = false
                    viewModel.saveTraining()
                },
                onDismiss = { showSaveDialog = false }
            )
        }

        state.validationError?.let { error ->
            AlertDialog(
                onDismissRequest = viewModel::clearValidationError,
                title = {
                    Text(
                        text = stringResource(R.string.add_training_validation_title),
                        style = TypographyPalette.H4
                    )
                },
                text = {
                    Text(text = error, style = TypographyPalette.Body1Regular)
                },
                confirmButton = {
                    Button(
                        text = stringResource(R.string.cancel),
                        buttonColors = ButtonColors.text(),
                        debounceClick = viewModel::clearValidationError
                    )
                }
            )
        }
    }
}

@Composable
private fun AthleteSessionCard(
    index: Int,
    session: AddAthleteSession,
    state: AddTrainingState,
    canRemove: Boolean,
    onAthleteSelected: (Long) -> Unit,
    onRemoveSession: () -> Unit,
    onAddPiece: () -> Unit,
    onRemovePiece: (Long) -> Unit,
    onDistanceChange: (Long, String) -> Unit,
    onTimeChange: (Long, String) -> Unit,
    onStrokeRateChange: (Long, String) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = Spacing._2XS)
    ) {
        Column(modifier = Modifier.padding(Spacing.M)) {
            Text(
                text = stringResource(R.string.add_training_athlete_header, index + 1),
                style = TypographyPalette.H4
            )
            SpacerS()
            AthleteDropdown(
                athletes = state.athletesCatalog,
                selectedAthleteId = session.athleteId,
                onAthleteSelected = onAthleteSelected
            )
            SpacerM()
            session.pieces.forEachIndexed { pieceIndex, piece ->
                PieceForm(
                    index = pieceIndex,
                    piece = piece,
                    canRemove = session.pieces.size > 1,
                    onDistanceChange = { onDistanceChange(piece.localId, it) },
                    onTimeChange = { onTimeChange(piece.localId, it) },
                    onStrokeRateChange = { onStrokeRateChange(piece.localId, it) },
                    onRemove = { onRemovePiece(piece.localId) }
                )
                SpacerS()
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.add_training_piece),
                debounceClick = onAddPiece
            )
            if (canRemove) {
                SpacerS()
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.add_training_delete_piece_work),
                    debounceClick = onRemoveSession
                )
            }
        }
    }
}

@Composable
private fun PieceForm(
    index: Int,
    piece: AddPieceDraft,
    canRemove: Boolean,
    onDistanceChange: (String) -> Unit,
    onTimeChange: (String) -> Unit,
    onStrokeRateChange: (String) -> Unit,
    onRemove: () -> Unit,
) {
    val pace = RowingTimeFormat.formatPace(piece.timeMillis, piece.distanceMeters)
        ?: stringResource(R.string.add_training_pace_placeholder)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(Spacing.S)) {
            Text(
                text = stringResource(R.string.add_training_piece_header, index + 1),
                style = TypographyPalette.Body2Medium
            )
            SpacerXS()
            TextField(
                value = piece.distanceText,
                onValueChange = onDistanceChange,
                label = stringResource(R.string.add_training_piece_length_label),
                textStyle = TypographyPalette.Body1Regular,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            SpacerXS()
            TextField(
                value = piece.timeText,
                onValueChange = onTimeChange,
                label = stringResource(R.string.add_training_piece_time_label),
                textStyle = TypographyPalette.Body1Regular,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            SpacerXS()
            TextField(
                value = piece.strokeRateText,
                onValueChange = onStrokeRateChange,
                label = stringResource(R.string.add_training_piece_stroke_rate_label),
                textStyle = TypographyPalette.Body1Regular,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            SpacerXS()
            Text(
                text = stringResource(R.string.add_training_piece_pace_label, pace),
                style = TypographyPalette.Body2Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (canRemove) {
                SpacerXS()
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.add_training_delete_piece),
                    buttonColors = ButtonColors.text(),
                    debounceClick = onRemove
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AthleteDropdown(
    athletes: List<by.rowing.sanbaiteam.athlete.presentation.common.AthleteItemModel>,
    selectedAthleteId: Long,
    onAthleteSelected: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = athletes.find { it.id == selectedAthleteId }?.name
        ?: stringResource(R.string.add_training_select_athlete)

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = stringResource(R.string.add_training_athlete_label),
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
            athletes.forEach { athlete ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = athlete.name,
                            style = TypographyPalette.Body1Regular
                        )
                    },
                    onClick = {
                        onAthleteSelected(athlete.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun SaveTrainingDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.add_training_save),
                style = TypographyPalette.H4
            )
        },
        text = {
            Text(
                stringResource(R.string.add_training_save_are_you_sure),
                style = TypographyPalette.Body1Regular
            )
        },
        confirmButton = {
            Button(
                text = stringResource(R.string.save),
                buttonColors = ButtonColors.text(),
                debounceClick = onConfirm
            )
        },
        dismissButton = {
            Button(
                text = stringResource(R.string.cancel),
                buttonColors = ButtonColors.text(),
                debounceClick = onDismiss
            )
        }
    )
}
