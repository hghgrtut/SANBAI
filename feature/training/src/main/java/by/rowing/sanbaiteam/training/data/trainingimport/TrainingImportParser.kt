package by.rowing.sanbaiteam.training.data.trainingimport

internal interface TrainingImportParser {

    val source: TrainingImportSource

    fun matches(bytes: ByteArray): Boolean

    fun parse(bytes: ByteArray): ParsedTrainingImport

    fun merge(csvTexts: List<String>): String
}

internal data class ParsedTrainingImport(
    val source: TrainingImportSource,
    val csvText: String,
    val deviceSerial: String?,
    val sessionName: String?,
    val startTimeMillis: Long?,
    val intervals: List<TrainingImportInterval>,
)

internal data class TrainingImportInterval(
    val distanceMeters: Int,
    val timeMillis: Long,
    val strokeRate: Double,
    val avgHeartRate: Int? = null,
    val maxHeartRate: Int? = null,
    val recoveryHeartRate: Int? = null,
)