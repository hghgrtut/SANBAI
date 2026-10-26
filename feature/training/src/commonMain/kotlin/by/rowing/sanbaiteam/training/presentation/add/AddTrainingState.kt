package by.rowing.sanbaiteam.training.presentation.add

import by.rowing.sanbaiteam.athlete.data.model.AthleteItemModel
import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType
import by.rowing.sanbaiteam.training.data.trainingimport.TrainingImportSource
import by.rowing.sanbaiteam.uikit.component.UiText

internal data class AddTrainingState(
    val dateMillis: Long,
    val dateFormatted: String,
    val rowersCatalog: List<AthleteItemModel> = emptyList(),
    val crewAthletes: List<AddCrewAthlete> = listOf(AddCrewAthlete()),
    val pieces: List<AddPieceDraft> = listOf(AddPieceDraft()),
    val validationError: UiText? = null,
    val pendingRawFiles: List<ByteArray> = emptyList(),
    val pendingFileExtension: String? = null,
    val pendingImportSource: TrainingImportSource? = null,
    val trainingType: TrainingPieceType = TrainingPieceType.SINGLE,
    val pendingSourceSerial: String? = null,
    val importNotice: UiText? = null,
    val showSaveDialog: Boolean = false
) {
    companion object {
        const val MAX_CREW_SIZE = 9
    }
}

internal data class AddCrewAthlete(
    val crewId: Long = nextLocalId(),
    val rowerId: Long = 0,
) {
    companion object {
        private var localIdSeq = 1L
        fun nextLocalId(): Long = localIdSeq++
    }
}

internal data class AddPieceDraft(
    val localId: Long = nextLocalId(),
    val distanceMeters: Int = 0,
    val timeMillis: Long = 0,
    val strokeRate: Double = 0.0,
    val avgHeartRate: Int? = null,
    val maxHeartRate: Int? = null,
    val recoveryHeartRate: Int? = null,
    val lactate: Double? = null,
    val distanceText: String = "",
    val timeText: String = "",
    val strokeRateText: String = "",
    val lactateText: String = "",
    val sourceFileIndex: Int? = null,
    val sourceIntervalIndex: Int? = null,
) {
    companion object {
        private var localIdSeq = 1L
        fun nextLocalId(): Long = localIdSeq++
    }
}