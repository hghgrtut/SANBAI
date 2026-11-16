package by.rowing.sanbaiteam.athlete.presentation.detail.state

import by.rowing.sanbaiteam.uikit.component.UiText

internal data class WeightDialogState(
    val dateMillis: Long,
    val weightText: String,
    val error: UiText? = null,
)