package by.rowing.sanbaiteam.athlete.presentation.add

import by.rowing.sanbaiteam.uikit.component.UiText

internal data class AddAthleteState(
    val name: String = "",
    val dateOfBirth: String = "",
    val speedCoachSerial: String = "",
    val isMale: Boolean = true,
    val nameError: UiText? = null,
    val dateError: UiText? = null
)
