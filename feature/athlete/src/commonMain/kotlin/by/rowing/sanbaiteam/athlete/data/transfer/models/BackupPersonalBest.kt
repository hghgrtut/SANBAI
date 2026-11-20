package by.rowing.sanbaiteam.athlete.data.transfer.models

import kotlinx.serialization.Serializable

@Serializable
internal data class BackupPersonalBest(
    val boatType: String,
    val distanceMeters: Int,
    val timeMillis: Long,
)