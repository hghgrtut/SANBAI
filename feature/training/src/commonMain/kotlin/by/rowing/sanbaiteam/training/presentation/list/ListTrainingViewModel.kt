package by.rowing.sanbaiteam.training.presentation.list

import androidx.lifecycle.viewModelScope
import by.rowing.sanbaiteam.athlete.data.repository.AthletesRepository
import by.rowing.sanbaiteam.core.presentation.compose.ComposeNavigator
import by.rowing.sanbaiteam.core.presentation.compose.getReturnToScreenNavOptions
import by.rowing.sanbaiteam.core.presentation.screen.base.ComposeBaseViewModel
import by.rowing.sanbaiteam.training.data.models.TrainingTransferResult
import by.rowing.sanbaiteam.training.data.repository.SavedTrainingFiltersRepository
import by.rowing.sanbaiteam.training.data.repository.TrainingRepository
import by.rowing.sanbaiteam.training.data.trainingtransfer.BackupFileAccess
import by.rowing.sanbaiteam.training.data.trainingtransfer.TrainingBackupSerializer
import by.rowing.sanbaiteam.training.data.trainingtransfer.TrainingTransferRepository
import by.rowing.sanbaiteam.training.presentation.filter.TrainingFilter
import by.rowing.sanbaiteam.training.presentation.filter.TrainingFilterParameter
import by.rowing.sanbaiteam.training.presentation.filter.TrainingFiltersController
import by.rowing.sanbaiteam.training.presentation.list.compose.ListTrainingScreenActions
import by.rowing.sanbaiteam.training.presentation.list.models.ListTrainingState
import by.rowing.sanbaiteam.training.presentation.navigation.TrainingNavigation
import by.rowing.sanbaiteam.training.resources.Res
import by.rowing.sanbaiteam.training.resources.training_transfer_export_error
import by.rowing.sanbaiteam.training.resources.training_transfer_export_success
import by.rowing.sanbaiteam.training.resources.training_transfer_import_error
import by.rowing.sanbaiteam.training.resources.training_transfer_import_result
import by.rowing.sanbaiteam.uikit.component.UiText
import kotlinx.coroutines.launch

internal class ListTrainingViewModel(
    private val repository: TrainingRepository,
    private val trainingTransferRepository: TrainingTransferRepository,
    private val backupFileAccess: BackupFileAccess,
    private val athletesRepository: AthletesRepository,
    private val composeNavigator: ComposeNavigator,
    savedFiltersRepository: SavedTrainingFiltersRepository
) : ComposeBaseViewModel<ListTrainingState>(
    initialState = ListTrainingState(
        items = emptyList(),
        transferNotice = null,
        isTransferring = false,
        exportPromptRequested = false,
    )
), ListTrainingScreenActions {

    private var pendingExportJson: String? = null

    private val filtersController = TrainingFiltersController(
        repository = savedFiltersRepository,
        scope = viewModelScope,
        readFilters = { state.filters },
        writeFilters = { filters -> changeState { copy(filters = filters) } },
    )

    init {
        reloadTrainings()
        loadAthleteCatalog()
        filtersController.subscribe()
    }

    override fun navigateBack() {
        if (state.isSelectionMode) {
            onCancelSelection()
        } else {
            composeNavigator.navigateBack()
        }
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

    override fun onShowFiltersChange(showFilters: Boolean) {
        filtersController.setShowFilters(showFilters = showFilters)
    }

    override fun onAddTrainingClick() {
        TrainingNavigation.TrainingAdd().navigateTo(
            composeNavigator = composeNavigator,
            navOptions = getReturnToScreenNavOptions<TrainingNavigation.TrainingList>()
        )
    }

    override fun onTrainingClick(trainingId: Long) {
        if (state.isSelectionMode) {
            toggleSelection(trainingId)
        } else {
            TrainingNavigation.TrainingDetail(trainingId).navigateTo(
                composeNavigator = composeNavigator,
                navOptions = getReturnToScreenNavOptions<TrainingNavigation.TrainingList>()
            )
        }
    }

    override fun onTrainingLongClick(trainingId: Long) {
        changeState { copy(selectedTrainingIds = setOf(trainingId)) }
    }

    override fun onCancelSelection() {
        changeState {
            copy(
                selectedTrainingIds = emptySet(),
                deleteConfirmationRequested = false
            )
        }
    }

    override fun onToggleSelectAllClick(trainingIds: List<Long>) {
        if (trainingIds.isEmpty()) return
        changeState {
            val allSelected = trainingIds.all { trainingId -> trainingId in selectedTrainingIds }
            copy(
                selectedTrainingIds = if (allSelected) {
                    emptySet()
                } else {
                    selectedTrainingIds + trainingIds
                }
            )
        }
    }

    override fun onDeleteClick() {
        changeState { copy(deleteConfirmationRequested = true) }
    }

    override fun onDeleteDismiss() {
        changeState { copy(deleteConfirmationRequested = false) }
    }

    override fun onDeleteConfirm() {
        changeState { copy(deleteConfirmationRequested = false) }
        viewModelScope.launch {
            repository.deleteTrainings(state.selectedTrainingIds.toList())
            reloadTrainings()
            changeState { copy(selectedTrainingIds = emptySet()) }
        }
    }

    override fun onExportSelectedClick() {
        if (state.isTransferring) return
        val selectedIds = state.selectedTrainingIds
        if (selectedIds.isEmpty()) return
        changeState { copy(isTransferring = true) }
        viewModelScope.launch {
            runCatching { trainingTransferRepository.exportSelectedBackup(selectedIds) }
                .onSuccess { backup ->
                    pendingExportJson = TrainingBackupSerializer.encode(backup)
                    changeState {
                        copy(
                            isTransferring = false,
                            exportPromptRequested = true
                        )
                    }
                }
                .onFailure {
                    pendingExportJson = null
                    changeState {
                        copy(
                            isTransferring = false,
                            transferNotice = UiText.of(Res.string.training_transfer_export_error)
                        )
                    }
                }
        }
    }

    override fun onExportPromptLaunched() {
        if (state.exportPromptRequested) {
            changeState { copy(exportPromptRequested = false) }
        }
    }

    override fun onExportToHandle(handle: String) {
        val json = pendingExportJson
        pendingExportJson = null
        json ?: return
        viewModelScope.launch {
            val success = backupFileAccess.write(
                handle = handle,
                text = json
            )
            val notice = UiText.of(
                resource = if (success) {
                    Res.string.training_transfer_export_success
                } else {
                    Res.string.training_transfer_export_error
                }
            )
            changeState { copy(transferNotice = notice) }
        }
    }

    override fun onImportFromHandle(handle: String) {
        if (state.isTransferring) return
        changeState { copy(isTransferring = true) }
        viewModelScope.launch {
            val result = backupFileAccess.read(handle)?.let { jsonText ->
                runCatching {
                    trainingTransferRepository.importBackup(TrainingBackupSerializer.decode(text = jsonText))
                }.getOrNull()
            }
            changeState {
                copy(
                    isTransferring = false,
                    transferNotice = buildImportNotice(result)
                )
            }
        }
    }

    override fun onDismissTransferNotice() {
        changeState { copy(transferNotice = null) }
    }

    fun reloadTrainings() {
        viewModelScope.launch {
            val trainings = repository.getTrainingsList()
            changeState {
                copy(
                    items = trainings,
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

    private fun toggleSelection(trainingId: Long) {
        changeState {
            copy(
                selectedTrainingIds = if (trainingId in selectedTrainingIds) {
                    selectedTrainingIds - trainingId
                } else {
                    selectedTrainingIds + trainingId
                }
            )
        }
    }

    private fun buildImportNotice(result: TrainingTransferResult?): UiText = result?.let {
        UiText.of(
            Res.string.training_transfer_import_result,
            result.trainingsImported,
            result.athletesCreated
        )
    } ?: UiText.of(Res.string.training_transfer_import_error)
}