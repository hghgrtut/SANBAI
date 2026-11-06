package by.rowing.sanbaiteam.training.presentation.detail.models

import by.rowing.sanbaiteam.training.data.chart.models.TrainingPieceSplit
import by.rowing.sanbaiteam.training.data.chart.models.TrainingSplitLength
import org.jetbrains.compose.resources.StringResource

internal data class TrainingDetailState(
    val trainingId: Long = 0,
    val dateFormatted: String = "",
    val typeResId: StringResource? = null,
    val athleteNames: List<String> = emptyList(),
    val pieces: List<TrainingDetailPiece> = emptyList(),
    val comparedTrainingIds: List<Long> = emptyList(),
    val expandedComparisonOrders: Set<Int> = emptySet(),
    val splitLength: TrainingSplitLength? = null,
    val splitsByOrder: Map<Int, List<TrainingPieceSplit>> = emptyMap(),
    val showSplitDialog: Boolean = false,
    val isLoading: Boolean = true,
    val notFound: Boolean = false,
)