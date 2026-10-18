package by.rowing.sanbaiteam.training.data.trainingimport

internal data class FitWorkout(
    val isRowing: Boolean,
    val startTimeMillis: Long?,
    val laps: List<FitLap>,
    val records: List<FitRecord>,
)

internal data class FitLap(
    val isActive: Boolean,
    val startMillis: Long,
    val endMillis: Long,
    val elapsedMillis: Long,
    val distanceMeters: Double,
)

internal data class FitRecord(
    val timestampMillis: Long,
    val heartRate: Int?,
    val cadence: Int?,
)

/**
 * Decodes a Concept2 `.fit` workout into platform-neutral data.
 */
internal fun decodeFitWorkout(bytes: ByteArray): FitWorkout = by.rowing.sanbaiteam.training.data.trainingimport.decodeFitWorkoutImpl(bytes)
