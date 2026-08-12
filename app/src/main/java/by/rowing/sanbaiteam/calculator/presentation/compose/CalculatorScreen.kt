package by.rowing.sanbaiteam.calculator.presentation.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import by.rowing.sanbaiteam.R
import by.rowing.sanbaiteam.calculator.presentation.CalculatorMode
import by.rowing.sanbaiteam.calculator.presentation.CalculatorState
import by.rowing.sanbaiteam.calculator.presentation.CalculatorViewModel
import by.rowing.sanbaiteam.uikit.component.SimpleTopAppBar
import by.rowing.sanbaiteam.uikit.component.TextField
import by.rowing.sanbaiteam.uikit.component.button.Button
import by.rowing.sanbaiteam.uikit.component.button.ButtonColors
import by.rowing.sanbaiteam.uikit.theme.Spacing
import by.rowing.sanbaiteam.uikit.theme.Spacing.SpacerM
import by.rowing.sanbaiteam.uikit.theme.Spacing.SpacerS
import by.rowing.sanbaiteam.uikit.theme.TypographyPalette
import by.rowing.sanbaiteam.uikit.theme.TypographyPaletteSp

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
        onClearError = viewModel::clearError,
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
    Scaffold(
        topBar = {
            SimpleTopAppBar(
                title = stringResource(R.string.calculator_title),
                onNavIconClick = onBackClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(Spacing.M),
            verticalArrangement = Arrangement.spacedBy(Spacing.M)
        ) {
            Text(
                text = stringResource(R.string.calculator_subtitle),
                style = TypographyPalette.Body2Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = state.mode == CalculatorMode.PaceToPercent,
                    onClick = { onModeChange(CalculatorMode.PaceToPercent) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    label = {
                        Text(stringResource(R.string.calculator_mode_percent))
                    }
                )
                SegmentedButton(
                    selected = state.mode == CalculatorMode.PercentToPace,
                    onClick = { onModeChange(CalculatorMode.PercentToPace) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    label = {
                        Text(stringResource(R.string.calculator_mode_pace))
                    }
                )
            }

            TextField(
                value = state.recordPaceText,
                onValueChange = onRecordPaceChange,
                label = stringResource(R.string.calculator_record_pace_label),
                placeholder = stringResource(R.string.calculator_pace_placeholder),
                textStyle = TypographyPaletteSp.Body1Regular,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            when (state.mode) {
                CalculatorMode.PaceToPercent -> {
                    TextField(
                        value = state.paceText,
                        onValueChange = onPaceChange,
                        label = stringResource(R.string.calculator_pace_label),
                        placeholder = stringResource(R.string.calculator_pace_placeholder),
                        textStyle = TypographyPaletteSp.Body1Regular,
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                CalculatorMode.PercentToPace -> {
                    TextField(
                        value = state.percentText,
                        onValueChange = onPercentChange,
                        label = stringResource(R.string.calculator_percent_label),
                        placeholder = stringResource(R.string.calculator_percent_placeholder),
                        textStyle = TypographyPaletteSp.Body1Regular,
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.calculator_calculate),
                debounceClick = onCalculate
            )

            if (state.resultText.isNotBlank()) {
                SpacerS()
                Text(
                    text = stringResource(R.string.calculator_result_label),
                    style = TypographyPalette.Body2Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = state.resultText,
                    style = TypographyPaletteSp.H2
                )
            }
            SpacerM()
        }
    }

    state.error?.let { error ->
        AlertDialog(
            onDismissRequest = onClearError,
            title = {
                Text(
                    text = stringResource(R.string.calculator_error_title),
                    style = TypographyPalette.H4
                )
            },
            text = { Text(text = error, style = TypographyPalette.Body1Regular) },
            confirmButton = {
                Button(
                    text = stringResource(R.string.cancel),
                    buttonColors = ButtonColors.text(),
                    debounceClick = onClearError
                )
            }
        )
    }
}
