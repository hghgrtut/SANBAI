package by.rowing.sanbaiteam.athlete.presentation.add.compose

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import by.rowing.sanbaiteam.athlete.data.model.AthleteItemModel
import by.rowing.sanbaiteam.athlete.presentation.add.AddAthleteState
import by.rowing.sanbaiteam.athlete.presentation.add.AddAthleteViewModel
import by.rowing.sanbaiteam.athlete.presentation.common.AthleteListItem
import by.rowing.sanbaiteam.uikit.component.TextField
import by.rowing.sanbaiteam.uikit.component.TopAppBar
import by.rowing.sanbaiteam.uikit.theme.Spacing
import by.rowing.sanbaiteam.uikit.theme.Spacing.SpacerS
import by.rowing.sanbaiteam.uikit.theme.TypographyPaletteSp

@Composable
internal fun AddAthleteScreen(viewModel: AddAthleteViewModel) {
    AddAthleteScreenContent(
        actions = viewModel,
        state = viewModel.state
    )
}

@Composable
private fun AddAthleteScreenContent(
    actions: AddAthleteScreenActions,
    state: AddAthleteState
) {
    Scaffold(
        topBar = { AddAthleteTopBar(actions = actions) }
    ) { paddingValues ->
        AddAthleteForm(
            actions = actions,
            state = state,
            paddingValues = paddingValues
        )
    }
}

@Composable
private fun AddAthleteTopBar(actions: AddAthleteScreenActions) {
    TopAppBar(
        title = stringResource(R.string.athletes_add_athlete),
        actions = {
            IconButton(onClick = actions::onSaveClick) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = "Сохранить",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        },
        onNavIconClick = actions::onBackClick
    )
}

@Composable
private fun AddAthleteForm(
    actions: AddAthleteScreenActions,
    state: AddAthleteState,
    paddingValues: PaddingValues
) {
    Column(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()
            .padding(Spacing.M),
        verticalArrangement = Arrangement.spacedBy(Spacing.M)
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
            supportingText = state.dateError ?: stringResource(R.string.athletes_add_athlete_birth_date_supporting_text),
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
            Column(
                modifier = Modifier.padding(Spacing.M)
            ) {
                Text(
                    text = "Пол спортсмена",
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

        AthleteListItem(
            athlete = AthleteItemModel(
                id = 0,
                name = state.name,
                dateOfBirth = state.dateOfBirth,
                isMale = state.isMale,
                speedCoachSerial = state.speedCoachSerial.ifBlank { null },
            )
        ) {}
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
                        else -> dateFormatLength + 2 // 10
                    }
                }

                override fun transformedToOriginal(offset: Int): Int {
                    return when {
                        offset <= firstDelimiterOffset - 1 -> offset
                        offset <= secondDelimiterOffset - 1 -> offset - 1
                        offset <= dateFormatLength + 1 -> offset - 2
                        else -> dateFormatLength // 8
                    }.coerceIn(0, formattedText.length)
                }
            }
        )
    }
}