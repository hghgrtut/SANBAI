package by.rowing.sanbaiteam.athlete.presentation.detail.state

import by.rowing.sanbaiteam.uikit.component.UiText

internal data class WeightRangeDialogState(
    val fromMillis: Long,
    val toMillis: Long,
    val error: UiText? = null,
)