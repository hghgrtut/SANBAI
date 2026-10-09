package by.rowing.sanbaiteam.athlete.presentation.detail

import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType

internal data class AthleteDetailState(
    val athleteId: Long = 0,
    val name: String = "",
    val dateOfBirth: String = "",
    val speedCoachSerial: String = "",
    val isMale: Boolean = true,
    val recordsByType: Map<TrainingPieceType, List<PersonalBestDraft>> = emptyRecordsByType(),
    val isLoading: Boolean = true,
    val notFound: Boolean = false,
    val nameError: String? = null,
    val dateError: String? = null,
    val recordsError: String? = null,
)

internal data class PersonalBestDraft(
    val localId: Long = nextLocalId(),
    val distanceText: String = "",
    val timeText: String = "",
    val distanceMeters: Int = 0,
    val timeMillis: Long = 0,
) {
    companion object {
        private var localIdSeq = 1L
        fun nextLocalId(): Long = localIdSeq++
    }
}

private fun emptyRecordsByType(): Map<TrainingPieceType, List<PersonalBestDraft>> =
    TrainingPieceType.entries.associateWith { emptyList() }
