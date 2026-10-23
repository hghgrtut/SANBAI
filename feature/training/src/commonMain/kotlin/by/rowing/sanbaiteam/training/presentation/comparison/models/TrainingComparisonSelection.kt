package by.rowing.sanbaiteam.training.presentation.comparison.models

internal data class TrainingComparisonSelection(
    val trainingId: Long,
    val comparedTrainingIds: Set<Long>,
) {

    fun comparedTrainingIdsFor(trainingId: Long): Set<Long> =
        if (this.trainingId == trainingId) comparedTrainingIds else emptySet()
}