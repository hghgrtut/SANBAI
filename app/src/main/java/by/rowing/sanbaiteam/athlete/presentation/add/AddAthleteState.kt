package by.rowing.sanbaiteam.athlete.presentation.add

internal data class AddAthleteState(
    val name: String = "",
    val dateOfBirth: String = "",
    val speedCoachSerial: String = "",
    val isMale: Boolean = true,
    val nameError: String? = null,
    val dateError: String? = null
)