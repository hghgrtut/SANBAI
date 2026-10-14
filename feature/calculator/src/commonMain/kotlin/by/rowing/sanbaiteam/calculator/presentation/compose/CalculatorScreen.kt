package by.rowing.sanbaiteam.calculator.presentation.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import by.rowing.sanbaiteam.calculator.presentation.CalculatorMode
import by.rowing.sanbaiteam.calculator.presentation.CalculatorState
import by.rowing.sanbaiteam.calculator.presentation.CalculatorViewModel
import by.rowing.sanbaiteam.calculator.resources.Res
import by.rowing.sanbaiteam.calculator.resources.calculator_calculate
import by.rowing.sanbaiteam.calculator.resources.calculator_error_title
import by.rowing.sanbaiteam.calculator.resources.calculator_mode_pace
import by.rowing.sanbaiteam.calculator.resources.calculator_mode_percent
import by.rowing.sanbaiteam.calculator.resources.calculator_pace_label
import by.rowing.sanbaiteam.calculator.resources.calculator_pace_placeholder
import by.rowing.sanbaiteam.calculator.resources.calculator_percent_label
import by.rowing.sanbaiteam.calculator.resources.calculator_percent_placeholder
import by.rowing.sanbaiteam.calculator.resources.calculator_record_pace_label
import by.rowing.sanbaiteam.calculator.resources.calculator_result_label
import by.rowing.sanbaiteam.calculator.resources.calculator_subtitle
import by.rowing.sanbaiteam.calculator.resources.calculator_title
import by.rowing.sanbaiteam.calculator.resources.cancel
import by.rowing.sanbaiteam.uikit.component.SimpleTopAppBar
import by.rowing.sanbaiteam.uikit.component.TextField
import by.rowing.sanbaiteam.uikit.component.button.Button
import by.rowing.sanbaiteam.uikit.component.button.ButtonColors
import by.rowing.sanbaiteam.uikit.component.decimal
import by.rowing.sanbaiteam.uikit.component.time
import by.rowing.sanbaiteam.uikit.theme.Spacing
import by.rowing.sanbaiteam.uikit.theme.Spacing.SpacerM
import by.rowing.sanbaiteam.uikit.theme.TypographyPalette
import by.rowing.sanbaiteam.uikit.theme.TypographyPaletteSp
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun CalculatorScreen(viewModel: CalculatorViewModel) {
    CalculatorScreenContent(
        state = viewModel.state,
        onBackClick = viewModel::onBackClick,
        onModeChange = viewModel::changeMode,
        onRecordPaceChange = viewModel::changeRecordPace,
        onPaceChange = viewModel::changePace,
        onPercentChange = viewModel::changePercent,
        onCalculate = viewModel::calculate,
        onClearError = viewModel::clearResult,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalculatorScreenContent(
    state: CalculatorState,
    onBackClick: () -> Unit,
    onModeChange: (CalculatorMode) -> Unit,
    onRecordPaceChange: (String) -> Unit,
    onPaceChange: (String) -> Unit,
    onPercentChange: (String) -> Unit,
    onCalculate: () -> Unit,
    onClearError: () -> Unit,
) {
    Scaffold(topBar = calculatorScreenTopBar(onBackClick = onBackClick)) { paddingValues ->
        CalculatorScreenContent(
            paddingValues = paddingValues,
            state = state,
            onModeChange = onModeChange,
            onRecordPaceChange = onRecordPaceChange,
            onPaceChange = onPaceChange,
            onPercentChange = onPercentChange,
            onCalculate = onCalculate
        )
    }

    state.error?.let { error ->
        InputErrorAlert(
            onClearError = onClearError,
            error = error.resolve()
        )
    }
}

private fun calculatorScreenTopBar(onBackClick: () -> Unit) = @Composable {
    SimpleTopAppBar(
        title = stringResource(Res.string.calculator_title),
        onNavIconClick = onBackClick
    )
}

@Composable
private fun CalculatorScreenContent(
    paddingValues: PaddingValues,
    state: CalculatorState,
    onModeChange: (CalculatorMode) -> Unit,
    onRecordPaceChange: (String) -> Unit,
    onPaceChange: (String) -> Unit,
    onPercentChange: (String) -> Unit,
    onCalculate: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(Spacing.M),
        verticalArrangement = Arrangement.spacedBy(Spacing.M)
    ) {
        Text(
            text = stringResource(Res.string.calculator_subtitle),
            style = TypographyPalette.Body2Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        CalculatorModeSelector(
            state = state,
            onModeChange = onModeChange
        )

        TextField(
            value = state.recordPaceText,
            onValueChange = onRecordPaceChange,
            label = stringResource(Res.string.calculator_record_pace_label),
            placeholder = stringResource(Res.string.calculator_pace_placeholder),
            textStyle = TypographyPaletteSp.Body1Regular,
            keyboardOptions = KeyboardOptions.time(),
            modifier = Modifier.fillMaxWidth()
        )

        CalculatorInputField(
            state = state,
            onPaceChange = onPaceChange,
            onPercentChange = onPercentChange
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(Res.string.calculator_calculate),
            debounceClick = onCalculate
        )

        if (state.resultText.isNotBlank()) {
            CalculatorResult(state = state)
        }

        SpacerM()
    }
}

@Composable
private fun CalculatorModeSelector(
    state: CalculatorState,
    onModeChange: (CalculatorMode) -> Unit
) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        val calculatorModes = listOf(
            CalculatorMode.PaceToPercent to Res.string.calculator_mode_percent,
            CalculatorMode.PercentToPace to Res.string.calculator_mode_pace
        )
        val calculatorModesCount = calculatorModes.size
        calculatorModes.forEachIndexed { index, (mode, titleRes) ->
            SegmentedButton(
                selected = state.mode == mode,
                onClick = { onModeChange(mode) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = calculatorModesCount
                ),
                label = { Text(stringResource(titleRes)) }
            )
        }
    }
}

@Composable
private fun CalculatorInputField(
    state: CalculatorState,
    onPaceChange: (String) -> Unit,
    onPercentChange: (String) -> Unit
) {
    when (state.mode) {
        CalculatorMode.PaceToPercent -> {
            TextField(
                value = state.paceText,
                onValueChange = onPaceChange,
                label = stringResource(Res.string.calculator_pace_label),
                placeholder = stringResource(Res.string.calculator_pace_placeholder),
                keyboardOptions = KeyboardOptions.time(),
                modifier = Modifier.fillMaxWidth()
            )
        }

        CalculatorMode.PercentToPace -> {
            TextField(
                value = state.percentText,
                onValueChange = onPercentChange,
                label = stringResource(Res.string.calculator_percent_label),
                placeholder = stringResource(Res.string.calculator_percent_placeholder),
                keyboardOptions = KeyboardOptions.decimal(imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun CalculatorResult(state: CalculatorState) {
    Column {
        Text(
            modifier = Modifier.padding(top = Spacing.S),
            text = stringResource(Res.string.calculator_result_label),
            style = TypographyPalette.Body2Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = state.resultText,
            style = TypographyPaletteSp.H2
        )
    }
}

@Composable
private fun InputErrorAlert(
    onClearError: () -> Unit,
    error: String
) {
    AlertDialog(
        onDismissRequest = onClearError,
        title = {
            Text(
                text = stringResource(Res.string.calculator_error_title),
                style = TypographyPalette.H4
            )
        },
        text = {
            Text(
                text = error,
                style = TypographyPalette.Body1Regular
            )
        },
        confirmButton = {
            Button(
                text = stringResource(Res.string.cancel),
                buttonColors = ButtonColors.text(),
                debounceClick = onClearError
            )
        }
    )
}
