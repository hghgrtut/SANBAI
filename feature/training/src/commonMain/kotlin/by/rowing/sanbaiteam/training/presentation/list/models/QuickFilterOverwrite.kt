package by.rowing.sanbaiteam.training.presentation.list.models

import by.rowing.sanbaiteam.training.presentation.filter.TrainingFilter

/** Запрос на перезапись существующего быстрого фильтра. */
internal data class QuickFilterOverwrite(
    val name: String,
    val filter: TrainingFilter,
)