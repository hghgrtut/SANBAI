package by.rowing.sanbaiteam.athlete.data.model

data class AthleteItemModel(
    val id: Long,
    val name: String,
    val dateOfBirth: String,
    val isMale: Boolean,
    val speedCoachSerial: String?,
) {

    val gender: String = if (isMale) "M" else "W"
    val initials = name.split(" ").joinToString(separator = "") { it.firstOrNull()?.toString().orEmpty() }
}
