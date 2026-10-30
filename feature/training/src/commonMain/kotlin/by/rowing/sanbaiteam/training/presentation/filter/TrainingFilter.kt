package by.rowing.sanbaiteam.training.presentation.filter

import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType

/**
 * Набор ограничений фильтрации тренировок.
 *
 * Пустые значения означают отсутствие ограничения по параметру. Разные
 * параметры объединяются через AND, значения внутри одного параметра — через OR.
 */
internal data class TrainingFilter(
    val athleteIds: Set<Long> = emptySet(),
    val trainingTypes: Set<TrainingPieceType> = emptySet(),
    val pieceTimeRange: LongRange? = null,
    val pieceDistanceRange: IntRange? = null,
    val lactateOnly: Boolean = false,
    val dateRange: LongRange? = null,
) {

    val isEmpty: Boolean
        get() = athleteIds.isEmpty() &&
                trainingTypes.isEmpty() &&
                pieceTimeRange == null &&
                pieceDistanceRange == null &&
                !lactateOnly &&
                dateRange == null
}