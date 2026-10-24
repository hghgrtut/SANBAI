package by.rowing.sanbaiteam.training.data.models.comparison

internal data class ComparablePiece(
    val distanceMeters: Int,
    val timeMillis: Long,
    val strokeRate: Double,
    val avgHeartRate: Int?,
    val maxHeartRate: Int?,
    val recoveryHeartRate: Int?,
    val lactate: Double?,
    val percentOfPb: Double?,
)