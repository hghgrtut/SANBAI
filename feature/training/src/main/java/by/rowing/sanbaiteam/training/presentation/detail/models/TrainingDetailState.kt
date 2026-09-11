package by.rowing.sanbaiteam.training.presentation.detail.models

internal data class TrainingDetailState(
    val trainingId: Long = 0,
    val dateFormatted: String = "",
    val typeResId: Int = 0,
    val athleteNames: List<String> = emptyList(),
    val pieces: List<TrainingDetailPiece> = emptyList(),
    val isLoading: Boolean = true,
    val notFound: Boolean = false,
)

internal data class TrainingDetailPiece(
    val order: Int,
    val distanceMeters: Int,
    val timeFormatted: String,
    val paceFormatted: String,
    val strokeRateFormatted: String,
    val percentOfPbFormatted: String,
)
