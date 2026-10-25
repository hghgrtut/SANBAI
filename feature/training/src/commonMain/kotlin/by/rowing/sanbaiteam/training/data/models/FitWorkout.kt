package by.rowing.sanbaiteam.training.data.models

internal data class FitWorkout(
    val isRowing: Boolean,
    val sport: UShort?,
    val subSport: UShort?,
    val manufacturer: UShort?,
    val deviceSerial: String?,
    val startTimeMillis: Long?,
    val laps: List<FitLap>,
    val records: List<FitRecord>,
)