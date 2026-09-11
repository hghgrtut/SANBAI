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
        changeState {
            copy(
                mode = mode,
                resultText = "",
                error = null,
            )
        }
    }

    fun changeRecordPace(text: String) {
        changeState { copy(recordPaceText = text, resultText = "", error = null) }
    }

    fun changePace(text: String) {
        changeState { copy(paceText = text, resultText = "", error = null) }
    }

    fun changePercent(text: String) {
        changeState { copy(percentText = text, resultText = "", error = null) }
    }

    fun calculate() {
        when (state.mode) {
            CalculatorMode.PaceToPercent -> calculatePercent()
            CalculatorMode.PercentToPace -> calculatePace()
        }
    }

    fun clearError() {
        changeState { copy(error = null) }
    }

    private fun calculatePercent() {
        val recordPace = RowingTimeFormat.parseDuration(state.recordPaceText)
        val pace = RowingTimeFormat.parseDuration(state.paceText)
        if (recordPace == null || recordPace <= 0) {
            changeState {
                copy(error = resourceUtils.getString(R.string.calculator_error_record_pace))
            }
            return
        }
        if (pace == null || pace <= 0) {
            changeState {
                copy(error = resourceUtils.getString(R.string.calculator_error_pace))
            }
            return
        }
        val percent = PersonalBestPercent.percentFromPaces(recordPace, pace)
        changeState {
            copy(
                resultText = PersonalBestPercent.format(percent),
                error = null,
            )
        }
    }

    private fun calculatePace() {
        val recordPace = RowingTimeFormat.parseDuration(state.recordPaceText)
        val percent = state.percentText.trim().replace(',', '.').toDoubleOrNull()
        if (recordPace == null || recordPace <= 0) {
            changeState {
                copy(error = resourceUtils.getString(R.string.calculator_error_record_pace))
            }
            return
        }
        if (percent == null || percent <= 0) {
            changeState {
                copy(error = resourceUtils.getString(R.string.calculator_error_percent))
            }
            return
        }
        val pace = PersonalBestPercent.paceFromPercent(recordPace, percent)
        changeState {
            copy(
                resultText = pace?.let { "${RowingTimeFormat.formatDuration(it)} /500м" }.orEmpty(),
                error = null,
            )
        }
    }
}
