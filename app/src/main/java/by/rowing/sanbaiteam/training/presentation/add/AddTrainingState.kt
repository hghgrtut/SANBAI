package by.rowing.sanbaiteam.training.presentation.add

import by.rowing.sanbaiteam.athlete.presentation.common.AthleteItemModel

internal data class AddTrainingState(
    val dateMillis: Long,
    val dateFormatted: String,
    val athletesCatalog: List<AthleteItemModel> = emptyList(),
    val crewAthletes: List<AddCrewAthlete> = listOf(AddCrewAthlete()),
    val pieces: List<AddPieceDraft> = listOf(AddPieceDraft()),
    val validationError: String? = null,
    val pendingRawCsv: String? = null,
    val pendingSourceSerial: String? = null,
    val pendingSourceSessionName: String? = null,
    val importNotice: String? = null,
) {
    companion object {
        const val MAX_CREW_SIZE = 9
    }
}

internal data class AddCrewAthlete(
    val localId: Long = nextLocalId(),
    val athleteId: Long = 0,
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
    val distanceText: String = "",
    val timeText: String = "",
    val strokeRateText: String = "",
) {
    companion object {
        private var localIdSeq = 1L
        fun nextLocalId(): Long = localIdSeq++
    }
}
