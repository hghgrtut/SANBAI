package by.rowing.sanbaiteam.training.data.models

internal data class FitRecord(
    val timestampMillis: Long,
    val heartRate: Int?,
    val cadence: Int?,
    val distanceMeters: Double? = null,
    val speedMetersPerSecond: Double? = null,
)