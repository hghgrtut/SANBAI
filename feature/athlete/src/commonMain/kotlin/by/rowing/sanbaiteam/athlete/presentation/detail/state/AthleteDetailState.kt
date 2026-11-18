package by.rowing.sanbaiteam.athlete.presentation.detail.state

import by.rowing.sanbaiteam.athlete.domain.weight.AthleteWeightRecord
import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType
import by.rowing.sanbaiteam.uikit.component.UiText

internal data class AthleteDetailState(
    val athleteId: Long = 0,
    val name: String = "",
    val dateOfBirth: String = "",
    val speedCoachSerial: String = "",
    val isMale: Boolean = true,
    val recordsByType: Map<TrainingPieceType, List<PersonalBestDraft>> = emptyRecordsByType(),
    val weights: List<AthleteWeightRecord> = emptyList(),
    val currentWeightTenths: Int? = null,
    val isWeightTableVisible: Boolean = false,
    val weightDialog: WeightDialogState? = null,
    val deleteWeightMillis: Long? = null,
    val weightRangeDialog: WeightRangeDialogState? = null,
    val isLoading: Boolean = true,
    val notFound: Boolean = false,
    val nameError: UiText? = null,
    val dateError: UiText? = null,
    val recordsError: UiText? = null,
)

private fun emptyRecordsByType(): Map<TrainingPieceType, List<PersonalBestDraft>> =
    TrainingPieceType.entries.associateWith { emptyList() }