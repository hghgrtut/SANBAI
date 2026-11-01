package by.rowing.sanbaiteam.training.presentation.comparison

import androidx.lifecycle.viewModelScope
import by.rowing.sanbaiteam.athlete.data.repository.AthletesRepository
import by.rowing.sanbaiteam.core.presentation.compose.ComposeNavigator
import by.rowing.sanbaiteam.core.presentation.screen.base.ComposeBaseViewModel
import by.rowing.sanbaiteam.training.data.repository.SavedTrainingFiltersRepository
import by.rowing.sanbaiteam.training.data.repository.TrainingRepository
import by.rowing.sanbaiteam.training.presentation.comparison.models.TrainingCompareState
import by.rowing.sanbaiteam.training.presentation.filter.TrainingFilter
import by.rowing.sanbaiteam.training.presentation.filter.TrainingFilterParameter
import by.rowing.sanbaiteam.training.presentation.filter.TrainingFiltersController
import by.rowing.sanbaiteam.training.presentation.filter.applyTrainingFilter
import kotlinx.coroutines.launch

internal class TrainingCompareViewModel(
    private val repository: TrainingRepository,
    private val composeNavigator: ComposeNavigator,
    private val comparisonSelectionStore: TrainingComparisonSelectionStore,
    private val athletesRepository: AthletesRepository,
    private val trainingId: Long,
    savedFiltersRepository: SavedTrainingFiltersRepository,
) : ComposeBaseViewModel<TrainingCompareState>(initialState = TrainingCompareState()), TrainingCompareActions {

    private val filtersController = TrainingFiltersController(
        repository = savedFiltersRepository,
        scope = viewModelScope,
        readFilters = { state.filters },
        writeFilters = { filters ->
            changeState {
                copy(
                    filters = filters,
                    filteredItems = items.applyTrainingFilter(trainingFilter = filters.filter),
                )
            }
        },
    )

    init {
        loadCandidates()
        loadAthleteCatalog()
        filtersController.subscribe()
    }

    override fun onToggleSelection(selectedTrainingId: Long) {
        changeState {
            copy(
                selectedIds = if (selectedTrainingId in selectedIds) {
                    selectedIds - selectedTrainingId
                } else {
                    selectedIds + selectedTrainingId
                }
            )
        }
    }

    override fun onConfirm() {
        comparisonSelectionStore.select(
            trainingId = trainingId,
            comparedTrainingIds = state.visibleSelectedIds.ifEmpty { return },
        )
        composeNavigator.navigateBack()
    }

    override fun onReset() {
        comparisonSelectionStore.clear()
        composeNavigator.navigateBack()
    }

    override fun navigateBack() {
        composeNavigator.navigateBack()
    }

    override fun onFilterChange(filter: TrainingFilter) {
        filtersController.updateFilter(filter = filter)
    }

    override fun onFilterReset(parameter: TrainingFilterParameter) {
        filtersController.resetParameter(parameter = parameter)
    }

    override fun onResetFilters() {
        filtersController.resetAll()
    }

    override fun onFilterSheetOpen(parameter: TrainingFilterParameter) {
        filtersController.openSheet(parameter = parameter)
    }

    override fun onFilterSheetDismiss() {
        filtersController.dismissSheet()
    }

    override fun onLactateFilterToggle() {
        filtersController.toggleLactate()
    }

    override fun onShowFiltersChange(showFilters: Boolean) {
        filtersController.setShowFilters(showFilters = showFilters)
    }

    override fun onQuickFilterApply(filterId: Long) {
        filtersController.applyQuickFilter(filterId = filterId)
    }

    override fun onQuickFilterSaveClick() {
        filtersController.requestSaveQuickFilter()
    }

    override fun onQuickFilterSave(name: String) {
        filtersController.saveQuickFilter(name = name)
    }

    override fun onQuickFilterOverwriteConfirm() {
        filtersController.confirmOverwrite()
    }

    override fun onQuickFilterOverwriteDismiss() {
        filtersController.dismissOverwrite()
    }

    override fun onQuickFilterManageClick(filterId: Long) {
        filtersController.openQuickFilterManage(filterId = filterId)
    }

    override fun onQuickFilterRename(
        filterId: Long,
        name: String
    ) {
        filtersController.renameQuickFilter(
            filterId = filterId,
            name = name
        )
    }

    override fun onQuickFilterDeleteClick(filterId: Long) {
        filtersController.requestDeleteQuickFilter(filterId = filterId)
    }

    override fun onQuickFilterDeleteConfirm() {
        filtersController.confirmDeleteQuickFilter()
    }

    override fun onQuickFilterDeleteDismiss() {
        filtersController.dismissDeleteQuickFilter()
    }

    override fun onQuickFilterDialogDismiss() {
        filtersController.dismissQuickFilterDialog()
    }

    private fun loadCandidates() {
        viewModelScope.launch {
            val candidates = repository.getComparisonCandidates(trainingId)
            val availableIds = candidates.map { it.trainingId }.toSet()
            val storedSelection = comparisonSelectionStore.selection.value.orEmpty()
            val selectedIds = storedSelection.intersect(availableIds)

            changeState {
                copy(
                    typeResId = candidates.firstOrNull()?.trainingTypeResId,
                    items = candidates,
                    filteredItems = candidates.applyTrainingFilter(trainingFilter = filters.filter),
                    selectedIds = selectedIds,
                    hasAppliedComparison = storedSelection.isNotEmpty(),
                    isLoading = false,
                )
            }
        }
    }

    private fun loadAthleteCatalog() {
        viewModelScope.launch {
            athletesRepository.allAthletes.collect { athletes ->
                filtersController.updateAthleteCatalog(athletes)
            }
        }
    }
}
