package by.rowing.sanbaiteam.athlete.presentation.common

internal data class AthleteItemModel(
    val id: Long,
    val name: String,
    val dateOfBirth: String,
    val isMale: Boolean,
) {

    val gender: String = if (isMale) "M" else "W"
    val initials = name.split(" ").joinToString(separator = "") { it.firstOrNull()?.toString().orEmpty() }
}
