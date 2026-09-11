package by.rowing.sanbaiteam.athlete.presentation.detail.compose

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import by.rowing.sanbaiteam.athlete.R
import by.rowing.sanbaiteam.athlete.presentation.detail.AthleteDetailScreenActions
import by.rowing.sanbaiteam.athlete.presentation.detail.AthleteDetailState
import by.rowing.sanbaiteam.athlete.presentation.detail.AthleteDetailViewModel
import by.rowing.sanbaiteam.athlete.presentation.detail.PersonalBestDraft
import by.rowing.sanbaiteam.uikit.component.TextField
import by.rowing.sanbaiteam.uikit.component.TopAppBar
import by.rowing.sanbaiteam.uikit.component.button.Button
import by.rowing.sanbaiteam.uikit.component.button.ButtonColors
import by.rowing.sanbaiteam.uikit.theme.Spacing
import by.rowing.sanbaiteam.uikit.theme.Spacing.SpacerS
import by.rowing.sanbaiteam.uikit.theme.Spacing.SpacerXS
import by.rowing.sanbaiteam.uikit.theme.TypographyPalette
import by.rowing.sanbaiteam.uikit.theme.TypographyPaletteSp

@Composable
internal fun AthleteDetailScreen(viewModel: AthleteDetailViewModel) {
    AthleteDetailScreenContent(
        actions = viewModel,
        state = viewModel.state,
    )
}

@Composable
private fun AthleteDetailScreenContent(
    actions: AthleteDetailScreenActions,
    state: AthleteDetailState,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = stringResource(R.string.athletes_detail_title),
                actions = {
                    if (!state.isLoading && !state.notFound) {
                        IconButton(onClick = actions::onSaveClick) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = stringResource(R.string.save),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                },
                onNavIconClick = actions::onBackClick
            )
        }
    ) { paddingValues ->
        when {
            state.isLoading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            }
            state.notFound -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(Spacing.M),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.athletes_detail_not_found),
                        style = TypographyPaletteSp.Body1Regular
                    )
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(Spacing.M),
                    verticalArrangement = Arrangement.spacedBy(Spacing.M)
                ) {
                    ProfileFields(state = state, actions = actions)
                    RecordsSection(state = state, actions = actions)
                }
            }
        }
    }

    state.recordsError?.let { error ->
        AlertDialog(
            onDismissRequest = actions::clearRecordsError,
            title = {
                Text(
                    text = stringResource(R.string.athletes_detail_save_error),
                    style = TypographyPalette.H4
                )
            },
            text = { Text(text = error, style = TypographyPalette.Body1Regular) },
            confirmButton = {
                Button(
                    text = stringResource(R.string.cancel),
                    buttonColors = ButtonColors.text(),
                    debounceClick = actions::clearRecordsError
                )
            }
        )
    }
}

@Composable
private fun ProfileFields(
    state: AthleteDetailState,
    actions: AthleteDetailScreenActions,
) {
    TextField(
        value = state.name,
        onValueChange = actions::changeName,
        label = stringResource(R.string.athletes_add_athlete_name_label),
        placeholder = stringResource(R.string.athletes_add_athlete_name_placeholder),
        error = state.nameError != null,
        supportingText = state.nameError,
        singleLine = true,
        textStyle = TypographyPaletteSp.Body1Regular,
        modifier = Modifier.fillMaxWidth(),
    )
    TextField(
        value = state.dateOfBirth,
        onValueChange = actions::changeBirthDate,
        label = stringResource(R.string.athletes_add_athlete_birth_date_label),
        placeholder = stringResource(R.string.athletes_add_athlete_birth_date_placeholder),
        supportingText = state.dateError
            ?: stringResource(R.string.athletes_add_athlete_birth_date_supporting_text),
        error = state.dateError != null,
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        textStyle = TypographyPaletteSp.Body1Regular,
        keyboardOptions = KeyboardOptions(
            autoCorrectEnabled = false,
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        visualTransformation = DateTransformation()
    )
    TextField(
        value = state.speedCoachSerial,
        onValueChange = actions::changeSpeedCoachSerial,
        label = stringResource(R.string.athletes_add_athlete_speedcoach_serial_label),
        placeholder = stringResource(R.string.athletes_add_athlete_speedcoach_serial_placeholder),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        textStyle = TypographyPaletteSp.Body1Regular,
        keyboardOptions = KeyboardOptions(
            autoCorrectEnabled = false,
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Done,
        )
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = Spacing._2XS)
    ) {
        Column(modifier = Modifier.padding(Spacing.M)) {
            Text(
                text = stringResource(R.string.athletes_detail_gender_label),
                style = TypographyPaletteSp.Body2Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            SpacerS()
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.M)) {
                GenderOption(
                    textResId = R.string.athletes_add_athlete_gender_male,
                    isSelected = state.isMale,
                    onClick = { actions.changeGender(isMale = true) },
                    icon = Icons.Default.Male
                )
                GenderOption(
                    textResId = R.string.athletes_add_athlete_gender_female,
                    isSelected = !state.isMale,
                    onClick = { actions.changeGender(isMale = false) },
                    icon = Icons.Default.Female
                )
            }
        }
    }
}

@Composable
private fun RecordsSection(
    state: AthleteDetailState,
    actions: AthleteDetailScreenActions,
) {
    Text(
        text = stringResource(R.string.athletes_detail_records_header),
        style = TypographyPalette.H4
    )
    state.records.forEach { record ->
        RecordRow(
            record = record,
            onDistanceChange = { actions.changeRecordDistance(record.localId, it) },
            onTimeChange = { actions.changeRecordTime(record.localId, it) },
            onRemove = { actions.removeRecord(record.localId) },
        )
    }
    Button(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(R.string.athletes_detail_add_record),
        debounceClick = actions::addRecord
    )
}

@Composable
private fun RecordRow(
    record: PersonalBestDraft,
    onDistanceChange: (String) -> Unit,
    onTimeChange: (String) -> Unit,
    onRemove: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(Spacing.S)) {
            TextField(
                value = record.distanceText,
                onValueChange = onDistanceChange,
                label = stringResource(R.string.athletes_detail_record_distance_label),
                textStyle = TypographyPaletteSp.Body1Regular,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            SpacerXS()
            TextField(
                value = record.timeText,
                onValueChange = onTimeChange,
                label = stringResource(R.string.athletes_detail_record_time_label),
                textStyle = TypographyPaletteSp.Body1Regular,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            SpacerXS()
            Button(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.athletes_detail_delete_record),
                buttonColors = ButtonColors.text(),
                debounceClick = onRemove
            )
        }
    }
}

@Composable
private fun GenderOption(
    @StringRes textResId: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector
) {
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    OutlinedButton(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outline
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.XS)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = stringResource(textResId),
                style = TypographyPaletteSp.Body2Medium
            )
        }
    }
}

private class DateTransformation : VisualTransformation {

    val firstDelimiterOffset = 2
    val secondDelimiterOffset = 3
    val dateFormatLength = 8

    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        val formattedText = buildAnnotatedString {
            if (originalText.isNotEmpty()) {
                for (i in originalText.indices) {
                    append(originalText[i])
                    if ((i == firstDelimiterOffset - 1 || i == secondDelimiterOffset) && i < originalText.lastIndex) {
                        append(".")
                    }
                }
            }
        }
        return TransformedText(
            text = formattedText,
            offsetMapping = object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int {
                    return when {
                        offset <= firstDelimiterOffset -> offset
                        offset <= secondDelimiterOffset + 1 -> offset + 1
                        offset <= dateFormatLength -> offset + 2
                        else -> dateFormatLength + 2
                    }
                }

                override fun transformedToOriginal(offset: Int): Int {
                    return when {
                        offset <= firstDelimiterOffset - 1 -> offset
                        offset <= secondDelimiterOffset - 1 -> offset - 1
                        offset <= dateFormatLength + 1 -> offset - 2
                        else -> dateFormatLength
                    }.coerceIn(0, formattedText.length)
                }
            }
        )
    }
}
