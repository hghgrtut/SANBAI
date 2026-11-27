package by.rowing.sanbaiteam.backup.data.model

import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic
import kotlinx.serialization.Serializable

@AllowDetektPublic
@Serializable
data class BackupPiece(
    val athleteIndex: Int,
    val order: Int,
    val distanceMeters: Int,
    val timeMillis: Long,
    val strokeRate: Double,
    val avgHeartRate: Int? = null,
    val maxHeartRate: Int? = null,
    val recoveryHeartRate: Int? = null,
    val lactate: Double? = null,
    val sourceFileIndex: Int? = null,
    val sourceIntervalIndex: Int? = null,
)