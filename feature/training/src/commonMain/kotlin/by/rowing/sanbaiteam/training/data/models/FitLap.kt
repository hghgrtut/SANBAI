package by.rowing.sanbaiteam.training.data.models

internal data class FitLap(
    val isActive: Boolean,
    val startMillis: Long,
    val endMillis: Long,
    val elapsedMillis: Long,
    val distanceMeters: Double,
)