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
        get() = TrainingFilterParameter.entries.none { parameter -> isActive(parameter) }

    val activeParametersCount: Int
        get() = TrainingFilterParameter.entries.count { parameter -> isActive(parameter) }

    fun isActive(parameter: TrainingFilterParameter): Boolean = when (parameter) {
        TrainingFilterParameter.ATHLETES -> athleteIds.isNotEmpty()
        TrainingFilterParameter.TRAINING_TYPE -> trainingTypes.isNotEmpty()
        TrainingFilterParameter.PIECE_TIME -> pieceTimeRange != null
        TrainingFilterParameter.PIECE_DISTANCE -> pieceDistanceRange != null
        TrainingFilterParameter.LACTATE -> lactateOnly
        TrainingFilterParameter.DATE -> dateRange != null
    }

    fun without(parameter: TrainingFilterParameter): TrainingFilter = when (parameter) {
        TrainingFilterParameter.ATHLETES -> copy(athleteIds = emptySet())
        TrainingFilterParameter.TRAINING_TYPE -> copy(trainingTypes = emptySet())
        TrainingFilterParameter.PIECE_TIME -> copy(pieceTimeRange = null)
        TrainingFilterParameter.PIECE_DISTANCE -> copy(pieceDistanceRange = null)
        TrainingFilterParameter.LACTATE -> copy(lactateOnly = false)
        TrainingFilterParameter.DATE -> copy(dateRange = null)
    }

    /**
     * Накладывает сохранённый фильтр поверх текущего.
     * Перезаписываются только параметры, заданные в [preset]; остальные текущие ограничения не трогаются.
     */
    fun applyPreset(preset: TrainingFilter): TrainingFilter = TrainingFilter(
        athleteIds = preset.athleteIds.ifEmpty { athleteIds },
        trainingTypes = preset.trainingTypes.ifEmpty { trainingTypes },
        pieceTimeRange = preset.pieceTimeRange ?: pieceTimeRange,
        pieceDistanceRange = preset.pieceDistanceRange ?: pieceDistanceRange,
        lactateOnly = lactateOnly || preset.lactateOnly,
        dateRange = preset.dateRange ?: dateRange,
    )
}