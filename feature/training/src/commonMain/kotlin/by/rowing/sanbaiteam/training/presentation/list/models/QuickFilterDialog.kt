package by.rowing.sanbaiteam.training.presentation.list.models

/** Открытое окно быстрых фильтров. */
internal sealed interface QuickFilterDialog {

    /** Сохранение текущего фильтра под новым именем. */
    data object Save : QuickFilterDialog

    /** Управление сохранённым фильтром: переименование и удаление. */
    data class Manage(val filterId: Long) : QuickFilterDialog
}

