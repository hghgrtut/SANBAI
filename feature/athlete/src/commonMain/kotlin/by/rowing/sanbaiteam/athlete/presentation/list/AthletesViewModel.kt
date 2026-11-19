package by.rowing.sanbaiteam.athlete.presentation.list

import androidx.lifecycle.viewModelScope
import by.rowing.sanbaiteam.athlete.data.repository.AthletesRepository
import by.rowing.sanbaiteam.athlete.data.transfer.models.AthleteBackupSerializer
import by.rowing.sanbaiteam.athlete.data.transfer.AthleteTransferRepository
import by.rowing.sanbaiteam.athlete.data.transfer.AthleteTransferResult
import by.rowing.sanbaiteam.athlete.domain.trainings.AthleteTrainingsCleaner
import by.rowing.sanbaiteam.athlete.presentation.list.compose.AthletesListActions
import by.rowing.sanbaiteam.athlete.presentation.navigation.AthletesNavigation
import by.rowing.sanbaiteam.athlete.resources.Res
import by.rowing.sanbaiteam.athlete.resources.athletes_transfer_export_error
import by.rowing.sanbaiteam.athlete.resources.athletes_transfer_export_success
import by.rowing.sanbaiteam.athlete.resources.athletes_transfer_import_error
import by.rowing.sanbaiteam.athlete.resources.athletes_transfer_import_result
import by.rowing.sanbaiteam.core.file.TextFileAccess
import by.rowing.sanbaiteam.core.presentation.compose.ComposeNavigator
import by.rowing.sanbaiteam.core.presentation.compose.getReturnToScreenNavOptions
import by.rowing.sanbaiteam.core.presentation.screen.base.ComposeBaseViewModel
import by.rowing.sanbaiteam.core.util.nowInMillis
import by.rowing.sanbaiteam.uikit.component.UiText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

internal class AthletesViewModel(
    private val repository: AthletesRepository,
    private val transferRepository: AthleteTransferRepository,
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
        if (athleteIds.isEmpty()) return
        changeState {
            val allSelected = athleteIds.all { athleteId -> athleteId in selectedAthleteIds }
            copy(
                selectedAthleteIds = if (allSelected) {
                    emptySet()
                } else {
                    selectedAthleteIds + athleteIds
                }
            )
        }
    }

    override fun onDeleteClick() {
        val selectedIds = state.selectedAthleteIds.toList()
        if (selectedIds.isEmpty()) return
        viewModelScope.launch {
            val count = trainingsCleaner.countRelatedTrainings(athleteIds = selectedIds)
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
        if (selectedIds.isEmpty()) return
        viewModelScope.launch {
            trainingsCleaner.deleteRelatedTrainings(athleteIds = selectedIds)
            repository.deleteAthletes(athleteIds = selectedIds)
            changeState { copy(selectedAthleteIds = emptySet()) }
        }
    }

    override fun onExportSelectedClick() {
        if (state.isTransferring) return
        val selectedIds = state.selectedAthleteIds
        if (selectedIds.isEmpty()) return
        changeState { copy(isTransferring = true) }
        viewModelScope.launch {
            runCatching { transferRepository.exportBackup(athleteIds = selectedIds) }
                .onSuccess { backup ->
                    pendingExportJson = AthleteBackupSerializer.encode(backup = backup)
                    changeState {
                        copy(
                            isTransferring = false,
                            exportPromptRequested = true,
                        )
                    }
                }
                .onFailure {
                    pendingExportJson = null
                    changeState {
                        copy(
                            isTransferring = false,
                            transferNotice = UiText.of(Res.string.athletes_transfer_export_error)
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
                resource = if (success) {
                    Res.string.athletes_transfer_export_success
                } else {
                    Res.string.athletes_transfer_export_error
                }
            )
            changeState { copy(transferNotice = notice) }
        }
    }

    override fun onImportFromHandle(handle: String) {
        if (state.isTransferring) return
        changeState { copy(isTransferring = true) }
        viewModelScope.launch {
            val result = withContext(Dispatchers.Default) {
                textFileAccess.read(handle = handle)?.let { jsonText ->
                    runCatching {
                        transferRepository.importBackup(
                            backup = AthleteBackupSerializer.decode(text = jsonText)
                        )
                    }.getOrNull()
                }
            }
            changeState {
                copy(
                    isTransferring = false,
                    transferNotice = buildImportNotice(result = result),
                )
            }
        }
    }

    override fun onDismissTransferNotice() {
        changeState { copy(transferNotice = null) }
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

    private fun buildImportNotice(result: AthleteTransferResult?): UiText = result?.let {
        UiText.of(
            Res.string.athletes_transfer_import_result,
            it.athletesCreated,
            it.athletesUpdated,
            it.personalBestsImported,
            it.weightsImported,
        )
    } ?: UiText.of(Res.string.athletes_transfer_import_error)
}

internal fun suggestedAthletesBackupFileName(): String {
    val date = Instant.fromEpochMilliseconds(nowInMillis()).toLocalDateTime(TimeZone.currentSystemDefault()).date
    val month = date.month.number.toString().padStart(2, '0')
    val day = date.day.toString().padStart(2, '0')
    return "sanbai-athletes-backup-${date.year}$month$day.json"
}