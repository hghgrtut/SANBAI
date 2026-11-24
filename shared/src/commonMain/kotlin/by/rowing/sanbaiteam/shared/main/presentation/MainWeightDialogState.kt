package by.rowing.sanbaiteam.shared.main.presentation

import by.rowing.sanbaiteam.uikit.component.UiText

internal data class MainWeightDialogState(
    val athleteId: Long,
    val dateMillis: Long,
    val weightText: String,
    val error: UiText? = null,
)