package by.rowing.sanbaiteam.athlete.data.model

import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic

@AllowDetektPublic
data class AthleteItemModel(
    val id: Long,
    val name: String,
    val dateOfBirth: String,
    val isMale: Boolean,
    val speedCoachSerial: String?,
    val age: Int? = null,
) {

    val initials = name.split(" ").joinToString(separator = "") { it.firstOrNull()?.toString().orEmpty() }
}
