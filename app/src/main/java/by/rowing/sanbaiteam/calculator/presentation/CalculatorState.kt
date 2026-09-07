package by.rowing.sanbaiteam.calculator.presentation

internal enum class CalculatorMode {
    PaceToPercent,
    PercentToPace,
}

internal data class CalculatorState(
    val mode: CalculatorMode = CalculatorMode.PaceToPercent,
    val recordPaceText: String = "",
    val paceText: String = "",
    val percentText: String = "",
    val resultText: String = "",
    val error: String? = null,
)
