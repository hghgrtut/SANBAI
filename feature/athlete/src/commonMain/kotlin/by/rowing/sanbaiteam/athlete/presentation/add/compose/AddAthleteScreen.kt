package by.rowing.sanbaiteam.athlete.presentation.add.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import by.rowing.sanbaiteam.athlete.data.model.AthleteItemModel
import by.rowing.sanbaiteam.athlete.presentation.add.AddAthleteState
import by.rowing.sanbaiteam.athlete.presentation.add.AddAthleteViewModel
import by.rowing.sanbaiteam.athlete.presentation.common.AthleteListItem
import by.rowing.sanbaiteam.athlete.resources.Res
import by.rowing.sanbaiteam.athlete.resources.athletes_add_athlete
import by.rowing.sanbaiteam.athlete.resources.athletes_add_athlete_birth_date_label
import by.rowing.sanbaiteam.athlete.resources.athletes_add_athlete_birth_date_placeholder
import by.rowing.sanbaiteam.athlete.resources.athletes_add_athlete_birth_date_supporting_text
import by.rowing.sanbaiteam.athlete.resources.athletes_add_athlete_gender_female
import by.rowing.sanbaiteam.athlete.resources.athletes_add_athlete_gender_male
import by.rowing.sanbaiteam.athlete.resources.athletes_add_athlete_name_label
import by.rowing.sanbaiteam.athlete.resources.athletes_add_athlete_name_placeholder
import by.rowing.sanbaiteam.athlete.resources.athletes_add_athlete_speedcoach_serial_label
import by.rowing.sanbaiteam.athlete.resources.athletes_add_athlete_speedcoach_serial_placeholder
import by.rowing.sanbaiteam.athlete.resources.athletes_add_preview_header
import by.rowing.sanbaiteam.athlete.resources.athletes_add_section_main
import by.rowing.sanbaiteam.athlete.resources.athletes_detail_gender_label
import by.rowing.sanbaiteam.athlete.resources.save
import by.rowing.sanbaiteam.uikit.component.DataCard
import by.rowing.sanbaiteam.uikit.component.SectionHeader
import by.rowing.sanbaiteam.uikit.component.TextField
import by.rowing.sanbaiteam.uikit.component.TopAppBar
import by.rowing.sanbaiteam.uikit.theme.Spacing
import by.rowing.sanbaiteam.uikit.theme.TypographyPaletteSp
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

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
        modifier = Modifier.imePadding(),
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
        title = stringResource(Res.string.athletes_add_athlete),
        actions = {
            IconButton(onClick = actions::onSaveClick) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = stringResource(Res.string.save),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        },
        onNavIconClick = actions::navigateBack
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
            .verticalScroll(rememberScrollState())
            .padding(Spacing.M),
        verticalArrangement = Arrangement.spacedBy(Spacing.M)
    ) {
        SectionHeader(title = stringResource(Res.string.athletes_add_section_main))
        NameInput(
            state = state,
            onNameChange = actions::changeName
        )
        DateOfBirthInput(
            state = state,
            onBirthDateChange = actions::changeBirthDate
        )
        SpeedCoachSerialInput(
            state = state,
            onSpeedCoachSerialChange = actions::changeSpeedCoachSerial
        )
        SectionHeader(title = stringResource(Res.string.athletes_detail_gender_label))
        GenderSelector(
            state = state,
            onChangeGender = actions::changeGender
        )
        SectionHeader(title = stringResource(Res.string.athletes_add_preview_header))
        AthleteCardPreview(state = state)
    }
}

@Composable
private fun NameInput(
    state: AddAthleteState,
    onNameChange: (String) -> Unit
) {
    TextField(
        value = state.name,
        onValueChange = onNameChange,
        label = stringResource(Res.string.athletes_add_athlete_name_label),
        placeholder = stringResource(Res.string.athletes_add_athlete_name_placeholder),
        error = state.nameError != null,
        supportingText = state.nameError?.resolve(),
        singleLine = true,
        textStyle = TypographyPaletteSp.Body1Regular,
        leadingIcon = { FieldIcon(icon = Icons.Default.Person) },
        keyboardOptions = KeyboardOptions(
            autoCorrectEnabled = false,
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun DateOfBirthInput(
    state: AddAthleteState,
    onBirthDateChange: (String) -> Unit
) {
    TextField(
        value = state.dateOfBirth,
        onValueChange = onBirthDateChange,
        label = stringResource(Res.string.athletes_add_athlete_birth_date_label),
        placeholder = stringResource(Res.string.athletes_add_athlete_birth_date_placeholder),
        supportingText = state.dateError?.resolve()
            ?: stringResource(Res.string.athletes_add_athlete_birth_date_supporting_text),
        error = state.dateError != null,
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        textStyle = TypographyPaletteSp.Body1Regular,
        leadingIcon = { FieldIcon(icon = Icons.Default.Cake) },
        keyboardOptions = KeyboardOptions(
            autoCorrectEnabled = false,
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next
        ),
        visualTransformation = DateTransformation()
    )
}

@Composable
private fun SpeedCoachSerialInput(
    state: AddAthleteState,
    onSpeedCoachSerialChange: (String) -> Unit
) {
    TextField(
        value = state.speedCoachSerial,
        onValueChange = onSpeedCoachSerialChange,
        label = stringResource(Res.string.athletes_add_athlete_speedcoach_serial_label),
        placeholder = stringResource(Res.string.athletes_add_athlete_speedcoach_serial_placeholder),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        textStyle = TypographyPaletteSp.Body1Regular,
        leadingIcon = { FieldIcon(icon = Icons.Default.Numbers) },
        keyboardOptions = KeyboardOptions(
            autoCorrectEnabled = false,
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Done,
        )
    )
}

@Composable
private fun GenderSelector(
    state: AddAthleteState,
    onChangeGender: (isMale: Boolean) -> Unit
) {
    DataCard(
        modifier = Modifier.fillMaxWidth(),
        verticalSpacing = Spacing.S
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.M)) {
            GenderOption(
                textRes = Res.string.athletes_add_athlete_gender_male,
                isSelected = state.isMale,
                onClick = { onChangeGender(true) },
                icon = Icons.Default.Male
            )
            GenderOption(
                textRes = Res.string.athletes_add_athlete_gender_female,
                isSelected = !state.isMale,
                onClick = { onChangeGender(false) },
                icon = Icons.Default.Female
            )
        }
    }
}

@Composable
private fun AthleteCardPreview(state: AddAthleteState) {
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

@Composable
private fun FieldIcon(icon: ImageVector) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.size(FIELD_ICON_SIZE),
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun GenderOption(
    textRes: StringResource,
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
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
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
                text = stringResource(textRes),
                style = TypographyPaletteSp.Body2Medium
            )
        }
    }
}

private val FIELD_ICON_SIZE = 20.dp

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
