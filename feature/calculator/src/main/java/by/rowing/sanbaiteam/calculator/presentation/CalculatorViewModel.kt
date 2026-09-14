package by.rowing.sanbaiteam.calculator.presentation

import by.rowing.sanbaiteam.calculator.R
import by.rowing.sanbaiteam.core.presentation.compose.ComposeNavigator
import by.rowing.sanbaiteam.core.presentation.screen.base.ComposeBaseViewModel
import by.rowing.sanbaiteam.core.util.AndroidResourceUtils
import by.rowing.sanbaiteam.core.util.PersonalBestPercent
import by.rowing.sanbaiteam.core.util.RowingTimeFormat

internal class CalculatorViewModel(
    private val resourceUtils: AndroidResourceUtils,
    private val composeNavigator: ComposeNavigator,
) : ComposeBaseViewModel<CalculatorState>(
    initialState = CalculatorState()
) {

    fun onBackClick() {
        composeNavigator.navigateBack()
    }

    fun changeMode(mode: CalculatorMode) {
        clearResult()
        changeState { copy(mode = mode) }
    }

    fun changeRecordPace(text: String) {
        clearResult()
        changeState { copy(recordPaceText = text) }
    }

    fun changePace(text: String) {
        clearResult()
        changeState { copy(paceText = text) }
    }

    fun changePercent(text: String) {
        clearResult()
        changeState { copy(percentText = text) }
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

    private fun calculatePercent() {
        val recordPace = RowingTimeFormat.parseDuration(state.recordPaceText)
        val pace = RowingTimeFormat.parseDuration(state.paceText)
        if (recordPace == null || recordPace <= 0) {
            changeState { copy(error = resourceUtils.getString(R.string.calculator_error_record_pace)) }
            return
        }
        if (pace == null || pace <= 0) {
            changeState { copy(error = resourceUtils.getString(R.string.calculator_error_pace)) }
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
        val recordPace = RowingTimeFormat.parseDuration(input = state.recordPaceText)
        val percent = state.percentText.trim().replace(
            oldChar = ',',
            newChar = '.'
        ).toDoubleOrNull()
        if (recordPace == null || recordPace <= 0) {
            changeState { copy(error = resourceUtils.getString(R.string.calculator_error_record_pace)) }
            return
        }
        if (percent == null || percent <= 0) {
            changeState { copy(error = resourceUtils.getString(R.string.calculator_error_percent)) }
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
