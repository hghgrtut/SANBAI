package by.rowing.sanbaiteam.training.presentation.filter

import by.rowing.sanbaiteam.athlete.data.model.AthleteItemModel
import by.rowing.sanbaiteam.training.data.repository.SavedTrainingFiltersRepository
import by.rowing.sanbaiteam.training.presentation.filter.models.QuickFilterDialog
import by.rowing.sanbaiteam.training.presentation.filter.models.QuickFilterOverwrite
import by.rowing.sanbaiteam.training.presentation.filter.models.TrainingFiltersState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Логика панели фильтров, общая для списка тренировок и выбора для сравнения.
 *
 * Контроллер не хранит состояние сам: он читает его через [readFilters] и
 * записывает через [writeFilters], поэтому подходит вьюмоделям с разными
 * состояниями экранов.
 */
internal class TrainingFiltersController(
    private val repository: SavedTrainingFiltersRepository,
    private val scope: CoroutineScope,
    private val readFilters: () -> TrainingFiltersState,
    private val writeFilters: (TrainingFiltersState) -> Unit,
) {

    fun subscribe() {
        scope.launch {
            repository.savedFilters.collect { savedFilters ->
                val current = readFilters()
                writeFilters(
                    current.copy(
                        savedFilters = savedFilters,
                        activeQuickFilterId = current.activeQuickFilterId?.takeIf { activeId ->
                            savedFilters.any { saved -> saved.id == activeId }
                        },
                    )
                )
            }
        }
    }

    fun updateAthleteCatalog(athletes: List<AthleteItemModel>) {
        writeFilters(readFilters().copy(athleteCatalog = athletes))
    }

    fun updateFilter(filter: TrainingFilter) {
        writeFilters(
            readFilters().copy(
                filter = filter,
                activeFilterSheet = null,
                activeQuickFilterId = null,
            )
        )
    }

    fun resetParameter(parameter: TrainingFilterParameter) {
        updateFilter(filter = readFilters().filter.without(parameter))
    }

    fun resetAll() {
        updateFilter(filter = TrainingFilter())
    }

    fun toggleLactate() {
        val filter = readFilters().filter
        updateFilter(filter = filter.copy(lactateOnly = !filter.lactateOnly))
    }

    fun openSheet(parameter: TrainingFilterParameter) {
        writeFilters(readFilters().copy(activeFilterSheet = parameter))
    }

    fun dismissSheet() {
        writeFilters(readFilters().copy(activeFilterSheet = null))
    }

    fun setShowFilters(showFilters: Boolean) {
        writeFilters(readFilters().copy(showFilters = showFilters))
    }

    fun applyQuickFilter(filterId: Long) {
        val current = readFilters()
        val savedFilter = current.savedFilters.firstOrNull { saved -> saved.id == filterId } ?: return
        writeFilters(
            current.copy(
                filter = current.filter.applyPreset(savedFilter.filter),
                activeFilterSheet = null,
                activeQuickFilterId = filterId,
            )
        )
    }

    fun requestSaveQuickFilter() {
        val current = readFilters()
        if (current.filter.isEmpty) return
        writeFilters(current.copy(quickFilterDialog = QuickFilterDialog.Save))
    }

    fun saveQuickFilter(name: String) {
        val normalizedName = name.trim()
        if (normalizedName.isEmpty()) return
        val current = readFilters()
        val existing = current.savedFilters.firstOrNull { saved ->
            saved.name.equals(normalizedName, ignoreCase = true)
        }
        if (existing != null) {
            writeFilters(
                current.copy(
                    quickFilterOverwrite = QuickFilterOverwrite(
                        name = normalizedName,
                        filter = current.filter,
                    )
                )
            )
            return
        }
        val filter = current.filter
        scope.launch {
            val filterId = repository.create(
                name = normalizedName,
                filter = filter
            )
            writeFilters(
                readFilters().copy(
                    quickFilterDialog = null,
                    activeQuickFilterId = filterId,
                )
            )
        }
    }

    fun confirmOverwrite() {
        val overwrite = readFilters().quickFilterOverwrite ?: return
        val existing = readFilters().savedFilters.firstOrNull { saved ->
            saved.name.equals(overwrite.name, ignoreCase = true)
        } ?: return
        scope.launch {
            repository.overwrite(
                id = existing.id,
                filter = overwrite.filter
            )
            writeFilters(
                readFilters().copy(
                    quickFilterDialog = null,
                    quickFilterOverwrite = null,
                    activeQuickFilterId = existing.id,
                )
            )
        }
    }

    fun dismissOverwrite() {
        writeFilters(readFilters().copy(quickFilterOverwrite = null))
    }

    fun openQuickFilterManage(filterId: Long) {
        if (readFilters().savedFilters.none { saved -> saved.id == filterId }) return
        writeFilters(readFilters().copy(quickFilterDialog = QuickFilterDialog.Manage(filterId)))
    }

    fun renameQuickFilter(
        filterId: Long,
        name: String
    ) {
        val normalizedName = name.trim()
        if (normalizedName.isEmpty()) return
        val filters = readFilters()
        val nameTaken = filters.savedFilters.any { saved ->
            saved.id != filterId && saved.name.equals(normalizedName, ignoreCase = true)
        }
        if (nameTaken) return
        scope.launch {
            repository.rename(
                id = filterId,
                name = normalizedName
            )
            writeFilters(readFilters().copy(quickFilterDialog = null))
        }
    }

    fun requestDeleteQuickFilter(filterId: Long) {
        writeFilters(readFilters().copy(quickFilterDeleteId = filterId))
    }

    fun confirmDeleteQuickFilter() {
        val filterId = readFilters().quickFilterDeleteId ?: return
        scope.launch {
            repository.delete(id = filterId)
            val current = readFilters()
            writeFilters(
                current.copy(
                    quickFilterDeleteId = null,
                    quickFilterDialog = null,
                    activeQuickFilterId = current.activeQuickFilterId.takeIf { activeId ->
                        activeId != filterId
                    },
                )
            )
        }
    }

    fun dismissDeleteQuickFilter() {
        writeFilters(readFilters().copy(quickFilterDeleteId = null))
    }

    fun dismissQuickFilterDialog() {
        writeFilters(readFilters().copy(quickFilterDialog = null))
    }
}