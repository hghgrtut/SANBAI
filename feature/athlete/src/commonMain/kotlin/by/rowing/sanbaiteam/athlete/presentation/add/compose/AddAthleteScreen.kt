package by.rowing.sanbaiteam.athlete.presentation.add.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import by.rowing.sanbaiteam.athlete.data.model.AthleteItemModel
import by.rowing.sanbaiteam.athlete.presentation.add.AddAthleteState
import by.rowing.sanbaiteam.athlete.presentation.add.AddAthleteViewModel
import by.rowing.sanbaiteam.athlete.presentation.common.AthleteDateTransformation
import by.rowing.sanbaiteam.athlete.presentation.common.AthleteListItem
import by.rowing.sanbaiteam.athlete.presentation.common.GenderSegmentedSelector
import by.rowing.sanbaiteam.athlete.resources.Res
import by.rowing.sanbaiteam.athlete.resources.athletes_add_athlete
import by.rowing.sanbaiteam.athlete.resources.athletes_add_athlete_birth_date_label
import by.rowing.sanbaiteam.athlete.resources.athletes_add_athlete_birth_date_pick
import by.rowing.sanbaiteam.athlete.resources.athletes_add_athlete_birth_date_placeholder
import by.rowing.sanbaiteam.athlete.resources.athletes_add_athlete_birth_date_supporting_text
import by.rowing.sanbaiteam.athlete.resources.athletes_add_athlete_name_label
import by.rowing.sanbaiteam.athlete.resources.athletes_add_athlete_name_placeholder
import by.rowing.sanbaiteam.athlete.resources.athletes_add_athlete_speedcoach_serial_label
import by.rowing.sanbaiteam.athlete.resources.athletes_add_athlete_speedcoach_serial_placeholder
import by.rowing.sanbaiteam.athlete.resources.athletes_add_preview_header
import by.rowing.sanbaiteam.athlete.resources.athletes_add_section_main
import by.rowing.sanbaiteam.athlete.resources.athletes_weight_value_label
import by.rowing.sanbaiteam.athlete.resources.athletes_weight_value_placeholder
import by.rowing.sanbaiteam.athlete.resources.save
import by.rowing.sanbaiteam.core.util.TimeUtils
import by.rowing.sanbaiteam.core.util.nowInMillis
import by.rowing.sanbaiteam.uikit.component.SectionHeader
import by.rowing.sanbaiteam.uikit.component.TextField
import by.rowing.sanbaiteam.uikit.component.TopAppBar
import by.rowing.sanbaiteam.uikit.component.decimal
import by.rowing.sanbaiteam.uikit.component.rememberDatePicker
import by.rowing.sanbaiteam.uikit.modifier.scaffoldContentInsets
import by.rowing.sanbaiteam.uikit.theme.Spacing
import by.rowing.sanbaiteam.uikit.theme.TypographyPaletteSp
import org.jetbrains.compose.resources.stringResource

private val FIELD_ICON_SIZE = 20.dp
private val GENDER_SELECTOR_WIDTH = 104.dp

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
    Scaffold(topBar = { AddAthleteTopBar(actions = actions) }) { paddingValues ->
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
        modifier = Modifier.scaffoldContentInsets(paddingValues)
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.S),
            verticalAlignment = Alignment.Top,
        ) {
            DateOfBirthInput(
                state = state,
                onBirthDateChange = actions::changeBirthDate,
                onBirthDatePicked = actions::pickBirthDate,
                modifier = Modifier.weight(weight = 1f),
            )
            GenderSegmentedSelector(
                isMale = state.isMale,
                onGenderChange = actions::changeGender,
                modifier = Modifier
                    .width(width = GENDER_SELECTOR_WIDTH)
                    .padding(top = Spacing.SM),
            )
        }
        SpeedCoachSerialInput(
            state = state,
            onSpeedCoachSerialChange = actions::changeSpeedCoachSerial
        )
        TextField(
            value = state.weight,
            onValueChange = actions::changeWeight,
            label = stringResource(Res.string.athletes_weight_value_label),
            placeholder = stringResource(Res.string.athletes_weight_value_placeholder),
            supportingText = state.weightError?.resolve(),
            error = state.weightError != null,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            textStyle = TypographyPaletteSp.Body1Regular,
            leadingIcon = { FieldIcon(icon = Icons.Default.FitnessCenter) },
            keyboardOptions = KeyboardOptions.decimal(imeAction = ImeAction.Done),
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
    onBirthDateChange: (String) -> Unit,
    onBirthDatePicked: (year: Int, month: Int, day: Int) -> Unit,
    modifier: Modifier,
) {
    val initialDateMillis = remember(state.dateOfBirth) {
        TimeUtils.noonOfDay(
            timeInMillis = TimeUtils.parseDateDigits(state.dateOfBirth) ?: nowInMillis()
        )
    }
    val launchDatePicker = rememberDatePicker(
        initialDateMillis = initialDateMillis,
        onDatePicked = onBirthDatePicked,
    )
    TextField(
        value = state.dateOfBirth,
        onValueChange = onBirthDateChange,
        label = stringResource(Res.string.athletes_add_athlete_birth_date_label),
        placeholder = stringResource(Res.string.athletes_add_athlete_birth_date_placeholder),
        supportingText = state.dateError?.resolve()
            ?: stringResource(Res.string.athletes_add_athlete_birth_date_supporting_text),
        error = state.dateError != null,
        singleLine = true,
        modifier = modifier,
        textStyle = TypographyPaletteSp.Body1Regular,
        leadingIcon = {
            IconButton(onClick = launchDatePicker) {
                FieldIcon(
                    icon = Icons.Default.Cake,
                    contentDescription = stringResource(Res.string.athletes_add_athlete_birth_date_pick),
                )
            }
        },
        keyboardOptions = KeyboardOptions(
            autoCorrectEnabled = false,
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next
        ),
        visualTransformation = AthleteDateTransformation()
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
            imeAction = ImeAction.Next,
        )
    )
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
private fun FieldIcon(
    icon: ImageVector,
    contentDescription: String? = null,
) {
    Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        modifier = Modifier.size(FIELD_ICON_SIZE),
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}