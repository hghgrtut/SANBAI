package by.rowing.sanbaiteam.backup.data.model

import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic
import kotlinx.serialization.Serializable

@AllowDetektPublic
@Serializable
data class BackupAthlete(
    val name: String,
    val dateOfBirthMillis: Long,
    val isMale: Boolean,
    val weights: List<BackupWeight> = emptyList(),
)