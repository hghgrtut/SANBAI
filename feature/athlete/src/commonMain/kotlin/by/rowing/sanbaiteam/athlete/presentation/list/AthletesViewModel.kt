package by.rowing.sanbaiteam.athlete.presentation.list

import androidx.lifecycle.viewModelScope
import by.rowing.sanbaiteam.athlete.data.repository.AthletesRepository
import by.rowing.sanbaiteam.athlete.domain.trainings.AthleteTrainingsCleaner
import by.rowing.sanbaiteam.athlete.presentation.list.compose.AthletesListActions
import by.rowing.sanbaiteam.athlete.presentation.navigation.AthletesNavigation
import by.rowing.sanbaiteam.athlete.resources.Res
import by.rowing.sanbaiteam.athlete.resources.athletes_export_error
import by.rowing.sanbaiteam.athlete.resources.athletes_export_success
import by.rowing.sanbaiteam.backup.data.repository.BackupRepository
import by.rowing.sanbaiteam.backup.data.serializer.BackupSerializer
import by.rowing.sanbaiteam.core.file.TextFileAccess
import by.rowing.sanbaiteam.core.presentation.compose.ComposeNavigator
import by.rowing.sanbaiteam.core.presentation.compose.getReturnToScreenNavOptions
import by.rowing.sanbaiteam.core.presentation.screen.base.ComposeBaseViewModel
import by.rowing.sanbaiteam.uikit.component.UiText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class AthletesViewModel(
    private val repository: AthletesRepository,
    private val backupRepository: BackupRepository,
    private val backupSerializer: BackupSerializer,
    private val trainingsCleaner: AthleteTrainingsCleaner,
    private val textFileAccess: TextFileAccess,
    private val composeNavigator: ComposeNavigator,
) : ComposeBaseViewModel<AthletesUiState>(
    initialState = AthletesUiState(
        items = emptyList(),
        searchQuery = "",
        showFilters = false
    )
), AthletesListActions {

    private var pendingExportJson: String? = null

    init {
        viewModelScope.launch {
            repository.allAthletes.collectLatest { athletes ->
                changeState {
                    copy(
                        items = athletes,
                        selectedAthleteIds = selectedAthleteIds.filterTo(mutableSetOf()) { athleteId ->
                            athletes.any { athlete -> athlete.id == athleteId }
                        },
                        isLoading = false,
                    )
                }
            }
        }
    }

    override fun navigateBack() {
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

    override fun onAddAthleteClick() {
        AthletesNavigation.AthletesAdd.navigateTo(
            composeNavigator = composeNavigator,
            navOptions = getReturnToScreenNavOptions<AthletesNavigation.AthletesList>()
        )
    }

    override fun onAthleteClick(athleteId: Long) {
        if (state.isSelectionMode) {
            toggleSelection(athleteId = athleteId)
        } else {
            AthletesNavigation.AthleteDetail(athleteId).navigateTo(
                composeNavigator = composeNavigator,
                navOptions = getReturnToScreenNavOptions<AthletesNavigation.AthletesList>()
            )
        }
    }

    override fun onAthleteLongClick(athleteId: Long) {
        changeState { copy(selectedAthleteIds = setOf(athleteId)) }
    }

    override fun onCancelSelection() {
        changeState {
            copy(
                selectedAthleteIds = emptySet(),
                deleteConfirmationRequested = false,
                relatedTrainingsCount = 0,
            )
        }
    }

    override fun onToggleSelectAllClick(athleteIds: List<Long>) {
        if (athleteIds.isNotEmpty()) {
            changeState {
                val allSelected = athleteIds.all { athleteId -> athleteId in selectedAthleteIds }
                copy(selectedAthleteIds = if (allSelected) emptySet() else selectedAthleteIds + athleteIds)
            }
        }
    }

    override fun onDeleteClick() {
        viewModelScope.launch {
            val count = trainingsCleaner.countRelatedTrainings(athleteIds = state.selectedAthleteIds.toList())
            changeState {
                copy(
                    relatedTrainingsCount = count,
                    deleteConfirmationRequested = true,
                )
            }
        }
    }

    override fun onDeleteDismiss() {
        changeState {
            copy(
                deleteConfirmationRequested = false,
                relatedTrainingsCount = 0,
            )
        }
    }

    override fun onDeleteConfirm() {
        val selectedIds = state.selectedAthleteIds.toList()
        changeState {
            copy(
                deleteConfirmationRequested = false,
                relatedTrainingsCount = 0,
            )
        }
        viewModelScope.launch {
            trainingsCleaner.deleteRelatedTrainings(athleteIds = selectedIds)
            repository.deleteAthletes(athleteIds = selectedIds)
            changeState { copy(selectedAthleteIds = emptySet()) }
        }
    }

    override fun onExportSelectedClick() {
        val selectedIds = state.selectedAthleteIds.takeIf { !state.isExporting && it.isNotEmpty() } ?: return
        changeState { copy(isExporting = true) }
        viewModelScope.launch {
            runCatching {
                val backup = backupRepository.exportSelectedAthletes(athleteIds = selectedIds)
                withContext(Dispatchers.Default) { backupSerializer.encode(backup = backup) }
            }.onSuccess { json ->
                pendingExportJson = json
                changeState {
                    copy(
                        isExporting = false,
                        exportPromptRequested = true,
                    )
                }
            }.onFailure {
                pendingExportJson = null
                changeState {
                    copy(
                        isExporting = false,
                        exportNotice = UiText.of(Res.string.athletes_export_error)
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
            val success = textFileAccess.write(
                handle = handle,
                text = json,
            )
            val notice = UiText.of(
                resource = if (success) Res.string.athletes_export_success else Res.string.athletes_export_error
            )
            changeState { copy(exportNotice = notice) }
        }
    }

    override fun onDismissExportNotice() {
        changeState { copy(exportNotice = null) }
    }

    private fun toggleSelection(athleteId: Long) {
        changeState {
            copy(
                selectedAthleteIds = if (athleteId in selectedAthleteIds) {
                    selectedAthleteIds - athleteId
                } else {
                    selectedAthleteIds + athleteId
                }
            )
        }
    }
}