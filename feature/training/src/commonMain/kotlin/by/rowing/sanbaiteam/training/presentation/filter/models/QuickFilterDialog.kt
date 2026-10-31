package by.rowing.sanbaiteam.training.presentation.filter.models

import by.rowing.sanbaiteam.training.presentation.filter.TrainingFilter

/** Открытое окно быстрых фильтров. */
internal sealed interface QuickFilterDialog {

    /** Сохранение текущего фильтра под новым именем. */
    data object Save : QuickFilterDialog

    /** Управление сохранённым фильтром: переименование и удаление. */
    data class Manage(val filterId: Long) : QuickFilterDialog
}

/** Запрос на перезапись существующего быстрого фильтра. */
internal data class QuickFilterOverwrite(
    val name: String,
    val filter: TrainingFilter,
)