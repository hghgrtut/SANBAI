package by.rowing.sanbaiteam.training.presentation.list

import androidx.lifecycle.viewModelScope
import by.rowing.sanbaiteam.core.presentation.compose.ComposeNavigator
import by.rowing.sanbaiteam.core.presentation.compose.getReturnToScreenNavOptions
import by.rowing.sanbaiteam.core.presentation.screen.base.ComposeBaseViewModel
import by.rowing.sanbaiteam.training.data.models.TrainingTransferResult
import by.rowing.sanbaiteam.training.data.repository.TrainingRepository
import by.rowing.sanbaiteam.training.data.trainingtransfer.BackupFileAccess
import by.rowing.sanbaiteam.training.data.trainingtransfer.TrainingBackupSerializer
import by.rowing.sanbaiteam.training.data.trainingtransfer.TrainingTransferRepository
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
    private val composeNavigator: ComposeNavigator
) : ComposeBaseViewModel<ListTrainingState>(
    initialState = ListTrainingState(
        items = emptyList(),
        searchQuery = "",
        showFilters = false,
        transferNotice = null,
        isTransferring = false,
        exportPromptRequested = false,
    )
), ListTrainingScreenActions {

    private var pendingExportJson: String? = null

    init {
        reloadTrainings()
    }

    fun reloadTrainings() {
        viewModelScope.launch {
            val trainings = repository.getTrainingsList()
            changeState { copy(items = trainings) }
        }
    }

    override fun onBackClick() {
        if (state.isSelectionMode) {
            onCancelSelection()
        } else {
            composeNavigator.navigateBack()
        }
    }

    override fun onSearchQueryChange(newQuery: String) {
        changeState { copy(searchQuery = newQuery) }
    }

    override fun onShowFiltersChange(showFilters: Boolean) {
        changeState { copy(showFilters = showFilters) }
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

    override fun onExportClick() {
        if (state.isTransferring) return
        changeState { copy(isTransferring = true) }
        viewModelScope.launch {
            runCatching { trainingTransferRepository.exportAllBackup() }
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
