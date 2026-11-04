package by.rowing.sanbaiteam.training.data.trainingimport

import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType

internal interface TrainingImportParser {

    val source: TrainingImportSource

    fun matches(bytes: ByteArray): Boolean

    fun parse(
        bytes: ByteArray,
        fileName: String?
    ): ParsedTrainingImport
}

internal class ParsedTrainingImport(
    val source: TrainingImportSource,
    val trainingType: TrainingPieceType,
    val rawFile: ByteArray,
    val fileExtension: String,
    val deviceSerial: String?,
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