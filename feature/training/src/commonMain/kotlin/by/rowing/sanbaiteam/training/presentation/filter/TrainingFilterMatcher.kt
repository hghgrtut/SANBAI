package by.rowing.sanbaiteam.training.presentation.filter

import by.rowing.sanbaiteam.training.presentation.list.models.ListTrainingPieceItem

internal object TrainingFilterMatcher {

    fun matches(
        filter: TrainingFilter,
        training: FilterableTraining
    ): Boolean = filter.isEmpty ||
            (matchesAthletes(
                filter = filter,
                training = training
            ) && matchesTrainingType(
                filter = filter,
                training = training
            ) && matchesPieceTime(
                filter = filter,
                pieces = training.pieces
            ) && matchesPieceDistance(
                filter = filter,
                pieces = training.pieces
            ) && matchesLactate(
                filter = filter,
                pieces = training.pieces
            ) && matchesDate(
                range = filter.dateRange,
                dateMillis = training.dateMillis
            ))

    private fun matchesAthletes(
        filter: TrainingFilter,
        training: FilterableTraining
    ): Boolean = filter.athleteIds.isEmpty() || training.athleteIds.any { it in filter.athleteIds }

    private fun matchesTrainingType(
        filter: TrainingFilter,
        training: FilterableTraining
    ): Boolean = filter.trainingTypes.isEmpty() || training.trainingType in filter.trainingTypes

    private fun matchesPieceTime(
        filter: TrainingFilter,
        pieces: List<ListTrainingPieceItem>,
    ): Boolean {
        val range = filter.pieceTimeRange
        return range == null || pieces.any { it.timeMillis in range }
    }

    private fun matchesPieceDistance(
        filter: TrainingFilter,
        pieces: List<ListTrainingPieceItem>,
    ): Boolean {
        val range = filter.pieceDistanceRange
        return range == null || pieces.any { it.distanceMeters in range }
    }

    private fun matchesLactate(
        filter: TrainingFilter,
        pieces: List<ListTrainingPieceItem>,
    ): Boolean = !filter.lactateOnly || pieces.any { it.lactate != null }

    private fun matchesDate(
        range: LongRange?,
        dateMillis: Long
    ): Boolean = range == null || dateMillis in range
}

internal fun <T : FilterableTraining> List<T>.applyTrainingFilter(
    trainingFilter: TrainingFilter
): List<T> = if (trainingFilter.isEmpty) {
    this
} else {
    filter { training ->
        TrainingFilterMatcher.matches(
            filter = trainingFilter,
            training = training
        )
    }
}