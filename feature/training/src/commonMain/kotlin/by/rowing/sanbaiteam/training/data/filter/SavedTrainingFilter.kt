package by.rowing.sanbaiteam.training.data.filter

import by.rowing.sanbaiteam.training.presentation.filter.TrainingFilter

/** Сохранённый пользователем быстрый фильтр тренировок. */
internal data class SavedTrainingFilter(
    val id: Long,
    val name: String,
    val filter: TrainingFilter,
    val createdAtMillis: Long,
)