package by.rowing.sanbaiteam.athlete.data.transfer.models

import kotlinx.serialization.Serializable

@Serializable
internal data class BackupAthlete(
    val name: String,
    val dateOfBirthMillis: Long,
    val isMale: Boolean,
    val speedCoachSerial: String? = null,
    val personalBests: List<BackupPersonalBest> = emptyList(),
    val weights: List<BackupWeight> = emptyList(),
)