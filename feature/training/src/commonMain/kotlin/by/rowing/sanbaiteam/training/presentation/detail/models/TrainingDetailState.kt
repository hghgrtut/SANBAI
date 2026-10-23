package by.rowing.sanbaiteam.training.presentation.detail.models

import by.rowing.sanbaiteam.training.data.comparison.PieceComparison
import org.jetbrains.compose.resources.StringResource

internal data class TrainingDetailState(
    val trainingId: Long = 0,
    val dateFormatted: String = "",
    val typeResId: StringResource? = null,
    val athleteNames: List<String> = emptyList(),
    val pieces: List<TrainingDetailPiece> = emptyList(),
    val comparedTrainingIds: List<Long> = emptyList(),
    val expandedComparisonOrders: Set<Int> = emptySet(),
    val isLoading: Boolean = true,
    val notFound: Boolean = false,
)

internal data class TrainingDetailPiece(
    val order: Int,
    val distanceMeters: Int,
    val timeFormatted: String,
    val paceFormatted: String,
    val strokeRateFormatted: String,
    val avgHeartRate: Int?,
    val maxHeartRate: Int?,
    val recoveryHeartRate: Int?,
    val lactate: Double?,
    val percentOfPbFormatted: String,
    val comparison: PieceComparison? = null,
)
