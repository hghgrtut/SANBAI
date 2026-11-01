package by.rowing.sanbaiteam.training.presentation.filter.models

import by.rowing.sanbaiteam.athlete.data.model.AthleteItemModel
import by.rowing.sanbaiteam.training.data.filter.SavedTrainingFilter
import by.rowing.sanbaiteam.training.presentation.filter.TrainingFilter
import by.rowing.sanbaiteam.training.presentation.filter.TrainingFilterParameter

/**
 * Состояние панели фильтров, общее для списка тренировок и выбора для сравнения.
 *
 * Текущее состояние фильтров не сохраняется между запусками приложения:
 * персистятся только быстрые фильтры.
 */
internal data class TrainingFiltersState(
    val filter: TrainingFilter = TrainingFilter(),
    val athleteCatalog: List<AthleteItemModel> = emptyList(),
    val activeFilterSheet: TrainingFilterParameter? = null,
    val showFilters: Boolean = false,
    val savedFilters: List<SavedTrainingFilter> = emptyList(),
    val activeQuickFilterId: Long? = null,
    val quickFilterDialog: QuickFilterDialog? = null,
    val quickFilterOverwrite: QuickFilterOverwrite? = null,
    val quickFilterDeleteId: Long? = null,
)