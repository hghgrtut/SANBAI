package by.rowing.sanbaiteam.training.presentation.add

import by.rowing.sanbaiteam.athlete.presentation.common.AthleteItemModel

internal data class AddTrainingState(
    val dateMillis: Long,
    val dateFormatted: String,
    val athletesCatalog: List<AthleteItemModel> = emptyList(),
    val athleteSessions: List<AddAthleteSession> = listOf(AddAthleteSession()),
    val validationError: String? = null,
)

internal data class AddAthleteSession(
    val localId: Long = nextLocalId(),
    val athleteId: Long = 0,
    val pieces: List<AddPieceDraft> = listOf(AddPieceDraft()),
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
