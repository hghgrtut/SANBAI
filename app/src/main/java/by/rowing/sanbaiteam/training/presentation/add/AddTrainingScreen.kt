package by.rowing.sanbaiteam.training.presentation.add

import android.app.DatePickerDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.core.net.toUri
import by.rowing.sanbaiteam.R
import by.rowing.sanbaiteam.athlete.presentation.common.AthleteItemModel
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
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        val csvTexts = uris.mapNotNull { selectedUri ->
            runCatching {
                context.contentResolver.openInputStream(selectedUri)?.bufferedReader()?.use { it.readText() }
            }.getOrNull()
        }
        if (csvTexts.isNotEmpty()) {
            viewModel.importCsvBatch(csvTexts)
        }
    }

    LaunchedEffect(Unit) {
        val initialUri = viewModel.consumeInitialImportUri() ?: return@LaunchedEffect
        runCatching {
            context.contentResolver.openInputStream(initialUri.toUri())?.bufferedReader()?.use { it.readText() }
        }.getOrNull()?.let(viewModel::importCsv)
    }

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

            Text(
                text = stringResource(R.string.add_training_crew_header),
                style = TypographyPalette.H4
            )
            SpacerS()
            state.crewAthletes.forEachIndexed { index, crewAthlete ->
                val selectedIds = state.crewAthletes.map { it.athleteId }.filter { it > 0 }.toSet()
                AthleteDropdown(
                    athletes = state.athletesCatalog.filter { athlete ->
                        athlete.id == crewAthlete.athleteId || athlete.id !in selectedIds
                    },
                    selectedAthleteId = crewAthlete.athleteId,
                    label = stringResource(R.string.add_training_athlete_header, index + 1),
                    onAthleteSelected = { viewModel.changeAthleteId(crewAthlete.localId, it) }
                )
                if (state.crewAthletes.size > 1) {
                    SpacerXS()
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.add_training_delete_piece_work),
                        buttonColors = ButtonColors.text(),
                        debounceClick = { viewModel.removeCrewAthlete(crewAthlete.localId) }
                    )
                }
                SpacerS()
            }
            if (state.crewAthletes.size < AddTrainingState.MAX_CREW_SIZE) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.add_training_piece_athlete),
                    debounceClick = viewModel::addCrewAthlete
                )
                SpacerM()
            }

            Text(
                text = stringResource(R.string.add_training_pieces_header),
                style = TypographyPalette.H4
            )
            SpacerS()
            state.pieces.forEachIndexed { pieceIndex, piece ->
                PieceForm(
                    index = pieceIndex,
                    piece = piece,
                    canRemove = state.pieces.size > 1,
                    onDistanceChange = { viewModel.changePieceDistance(piece.localId, it) },
                    onTimeChange = { viewModel.changePieceTime(piece.localId, it) },
                    onStrokeRateChange = { viewModel.changePieceStrokeRate(piece.localId, it) },
                    onRemove = { viewModel.removePiece(piece.localId) }
                )
                SpacerS()
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.add_training_piece),
                debounceClick = viewModel::addPiece
            )
            SpacerM()

            Button(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.add_training_import_csv),
                debounceClick = { importLauncher.launch("*/*") }
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
        state.importNotice?.let { notice ->
            AlertDialog(
                onDismissRequest = viewModel::clearValidationError,
                title = {
                    Text(
                        text = stringResource(R.string.add_training_import_notice_title),
                        style = TypographyPalette.H4
                    )
                },
                text = { Text(text = notice, style = TypographyPalette.Body1Regular) },
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
    athletes: List<AthleteItemModel>,
    selectedAthleteId: Long,
    label: String,
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
