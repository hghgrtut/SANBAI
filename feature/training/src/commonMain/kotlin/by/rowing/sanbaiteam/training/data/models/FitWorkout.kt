package by.rowing.sanbaiteam.training.data.models

internal data class FitWorkout(
    val isRowing: Boolean,
    val startTimeMillis: Long?,
    val laps: List<FitLap>,
    val records: List<FitRecord>,
)