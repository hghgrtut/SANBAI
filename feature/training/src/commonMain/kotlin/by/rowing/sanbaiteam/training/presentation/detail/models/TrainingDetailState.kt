package by.rowing.sanbaiteam.training.presentation.detail.models

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