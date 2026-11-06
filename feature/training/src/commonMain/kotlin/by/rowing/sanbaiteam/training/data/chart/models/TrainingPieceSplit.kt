package by.rowing.sanbaiteam.training.data.chart.models

/** Показатели одного сплита внутри отрезка. */
internal data class TrainingPieceSplit(
    val index: Int,
    val distanceMeters: Int,
    val timeMillis: Long,
    val paceMillis: Long?,
    val strokeRate: Double?,
    val avgHeartRate: Int?,
    val maxHeartRate: Int?,
)