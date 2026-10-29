package by.rowing.sanbaiteam.calculator.presentation

import by.rowing.sanbaiteam.calculator.resources.Res
import by.rowing.sanbaiteam.calculator.resources.calculator_error_pace
import by.rowing.sanbaiteam.calculator.resources.calculator_error_percent
import by.rowing.sanbaiteam.calculator.resources.calculator_error_record_pace
import by.rowing.sanbaiteam.core.presentation.compose.ComposeNavigator
import by.rowing.sanbaiteam.core.presentation.compose.NavigateBackAware
import by.rowing.sanbaiteam.core.presentation.screen.base.ComposeBaseViewModel
import by.rowing.sanbaiteam.core.util.PersonalBestPercent
import by.rowing.sanbaiteam.core.util.RowingTimeFormat
import by.rowing.sanbaiteam.uikit.component.UiText

internal class CalculatorViewModel(
    private val composeNavigator: ComposeNavigator,
) : ComposeBaseViewModel<CalculatorState>(initialState = CalculatorState()), NavigateBackAware {

    override fun navigateBack() {
        composeNavigator.navigateBack()
    }

    fun changeMode(mode: CalculatorMode) {
        clearResult()
        changeState { copy(mode = mode) }
    }

    fun changeRecordPace(text: String) {
        clearResult()
        changeState { copy(recordPaceText = RowingTimeFormat.sanitizeTimeInput(text)) }
    }

    fun changePace(text: String) {
        clearResult()
        changeState { copy(paceText = RowingTimeFormat.sanitizeTimeInput(text)) }
    }

    fun changePercent(text: String) {
        clearResult()
        changeState { copy(percentText = sanitizePercentInput(text)) }
    }

    fun calculate() {
        when (state.mode) {
            CalculatorMode.PaceToPercent -> calculatePercent()
            CalculatorMode.PercentToPace -> calculatePace()
        }
    }

    fun clearResult() {
        changeState {
            copy(
                resultText = "",
                error = null,
            )
        }
    }

    private fun sanitizePercentInput(text: String): String {
        val result = StringBuilder()
        var hasSeparator = false
        text.forEach { symbol ->
            when {
                symbol.isDigit() -> result.append(symbol)
                (symbol == '.' || symbol == ',') && !hasSeparator -> {
                    hasSeparator = true
                    result.append('.')
                }
            }
        }
        return result.toString()
    }

    private fun calculatePercent() {
        val recordPace = RowingTimeFormat.parseTimeInput(input = state.recordPaceText)
        val pace = RowingTimeFormat.parseTimeInput(input = state.paceText)
        if (recordPace == null || recordPace <= 0) {
            changeState { copy(error = UiText.of(Res.string.calculator_error_record_pace)) }
            return
        }
        if (pace == null || pace <= 0) {
            changeState { copy(error = UiText.of(Res.string.calculator_error_pace)) }
            return
        }
        val percent = PersonalBestPercent.percentFromPaces(
            recordPaceMillis = recordPace,
            paceMillis = pace
        )
        changeState {
            copy(
                resultText = PersonalBestPercent.format(percent),
                error = null,
            )
        }
    }

    private fun calculatePace() {
        val recordPace = RowingTimeFormat.parseTimeInput(input = state.recordPaceText)
        val percent = state.percentText.toDoubleOrNull()
        if (recordPace == null || recordPace <= 0) {
            changeState { copy(error = UiText.of(Res.string.calculator_error_record_pace)) }
            return
        }
        if (percent == null || percent <= 0) {
            changeState { copy(error = UiText.of(Res.string.calculator_error_percent)) }
            return
        }
        val pace = PersonalBestPercent.paceFromPercent(
            recordPaceMillis = recordPace,
            percent = percent
        )
        changeState {
            copy(
                resultText = pace?.let { "${RowingTimeFormat.formatDuration(it)} /500м" }.orEmpty(),
                error = null,
            )
        }
    }
}