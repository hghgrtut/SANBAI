package by.rowing.sanbaiteam.shared.main.presentation

import androidx.lifecycle.viewModelScope
import by.rowing.sanbaiteam.athlete.data.repository.AthletesRepository
import by.rowing.sanbaiteam.athlete.domain.weight.AthleteWeightInput
import by.rowing.sanbaiteam.athlete.presentation.navigation.AthletesNavGraphProvider
import by.rowing.sanbaiteam.backup.data.repository.BackupRepository
import by.rowing.sanbaiteam.backup.data.serializer.BackupSerializer
import by.rowing.sanbaiteam.backup.data.serializer.UnsupportedBackupFormatException
import by.rowing.sanbaiteam.calculator.presentation.navigation.CalculatorNavGraphProvider
import by.rowing.sanbaiteam.core.file.TextFileAccess
import by.rowing.sanbaiteam.core.presentation.compose.ComposeNavigator
import by.rowing.sanbaiteam.core.presentation.compose.getReturnToScreenNavOptions
import by.rowing.sanbaiteam.core.presentation.screen.base.ComposeBaseViewModel
import by.rowing.sanbaiteam.core.util.TimeUtils
import by.rowing.sanbaiteam.core.util.nowInMillis
import by.rowing.sanbaiteam.settings.presentation.navigation.SettingsNavGraphProvider
import by.rowing.sanbaiteam.shared.main.presentation.navigation.MainNavigation
import by.rowing.sanbaiteam.shared.resources.Res
import by.rowing.sanbaiteam.shared.resources.main_sync_date_order_invalid
import by.rowing.sanbaiteam.shared.resources.main_sync_export_error
import by.rowing.sanbaiteam.shared.resources.main_sync_export_success
import by.rowing.sanbaiteam.shared.resources.main_sync_import_error
import by.rowing.sanbaiteam.shared.resources.main_sync_import_result
import by.rowing.sanbaiteam.shared.resources.main_sync_import_unsupported
import by.rowing.sanbaiteam.training.data.dashboard.TrainingDashboard
import by.rowing.sanbaiteam.training.presentation.navigation.TrainingNavGraphProvider
import by.rowing.sanbaiteam.uikit.component.UiText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class MainViewModel(
    private val composeNavigator: ComposeNavigator,
    private val athletesRepository: AthletesRepository,
    private val trainingDashboard: TrainingDashboard,
    private val backupRepository: BackupRepository,
    private val backupSerializer: BackupSerializer,
    private val textFileAccess: TextFileAccess,
) : ComposeBaseViewModel<MainUiState>(initialState = MainUiState()), MainScreenActions {

    private var pendingSyncExportJson: String? = null

    init {
        viewModelScope.launch {
            combine(
                trainingDashboard.observeDashboard(),
                athletesRepository.allAthletes,
            ) { trainings, athletes -> trainings to athletes }
                .collect { (trainings, athletes) ->
                    changeState {
                        copy(
                            isLoading = false,
                            athletes = athletes,
                            trainingsCount = trainings.trainingCount,
                            recentTrainings = trainings.recent,
                        )
                    }
                }
        }
    }

    override fun navigateToAthletesScreen() {
        AthletesNavGraphProvider.navigateToRoot(
            composeNavigator = composeNavigator,
            navOptions = getReturnToScreenNavOptions<MainNavigation.Root>()
        )
    }

    override fun navigateToTrainingsScreen() {
        TrainingNavGraphProvider.navigateToRoot(
            composeNavigator = composeNavigator,
            navOptions = getReturnToScreenNavOptions<MainNavigation.Root>()
        )
    }

    override fun navigateToCalculatorScreen() {
        CalculatorNavGraphProvider.navigateToRoot(
            composeNavigator = composeNavigator,
            navOptions = getReturnToScreenNavOptions<MainNavigation.Root>()
        )
    }

    override fun navigateToAddTrainingScreen() {
        TrainingNavGraphProvider.navigateToAdd(
            composeNavigator = composeNavigator,
            navOptions = getReturnToScreenNavOptions<MainNavigation.Root>()
        )
    }

    override fun navigateToSettings() {
        SettingsNavGraphProvider.navigateToRoot(
            composeNavigator = composeNavigator,
            navOptions = getReturnToScreenNavOptions<MainNavigation.Root>()
        )
    }

    override fun onTrainingClick(trainingId: Long) {
        TrainingNavGraphProvider.navigateToDetail(
            composeNavigator = composeNavigator,
            trainingId = trainingId,
            navOptions = getReturnToScreenNavOptions<MainNavigation.Root>()
        )
    }

    override fun openWeightDialog() {
        val athleteId = state.athletes.firstOrNull()?.id ?: return
        changeState {
            copy(
                weightDialog = MainWeightDialogState(
                    athleteId = athleteId,
                    dateMillis = TimeUtils.noonOfDay(timeInMillis = nowInMillis()),
                    weightText = "",
                )
            )
        }
    }

    override fun dismissWeightDialog() {
        changeState { copy(weightDialog = null) }
    }

    override fun changeWeightAthlete(athleteId: Long) {
        changeState {
            copy(
                weightDialog = weightDialog?.copy(
                    athleteId = athleteId,
                    error = null,
                )
            )
        }
    }

    override fun changeWeightDate(
        year: Int,
        month: Int,
        day: Int,
    ) {
        TimeUtils.noonMillis(
            year = year,
            month = month,
            day = day,
        )?.let { dateMillis ->
            changeState {
                copy(
                    weightDialog = weightDialog?.copy(
                        dateMillis = dateMillis,
                        error = null,
                    )
                )
            }
        }
    }

    override fun changeWeightValue(text: String) {
        changeState {
            copy(
                weightDialog = weightDialog?.copy(
                    weightText = AthleteWeightInput.sanitize(text = text),
                    error = null,
                )
            )
        }
    }

    override fun saveWeight() {
        val dialog = state.weightDialog ?: return
        val error = AthleteWeightInput.validate(
            text = dialog.weightText,
            dateMillis = dialog.dateMillis,
            nowMillis = nowInMillis(),
        )
        if (error != null) {
            changeState { copy(weightDialog = dialog.copy(error = error)) }
        } else {
            val weightTenths = AthleteWeightInput.parse(text = dialog.weightText) ?: return
            viewModelScope.launch {
                athletesRepository.saveWeight(
                    athleteId = dialog.athleteId,
                    dateMillis = dialog.dateMillis,
                    weightTenths = weightTenths,
                )
                changeState { copy(weightDialog = null) }
            }
        }
    }

    override fun openSyncDialog() {
        val now = nowInMillis()
        changeState {
            copy(
                syncDialog = MainSyncDialogState(
                    fromMillis = TimeUtils.plusDaysMillis(
                        timeInMillis = now,
                        days = -DEFAULT_SYNC_DAYS_BACK,
                    ),
                    toMillis = TimeUtils.plusDaysMillis(
                        timeInMillis = now,
                        days = 1,
                    ) - 1,
                )
            )
        }
    }

    override fun dismissSyncDialog() {
        pendingSyncExportJson = null
        changeState { copy(syncDialog = null) }
    }

    override fun changeSyncDateFrom(
        year: Int,
        month: Int,
        day: Int,
    ) {
        TimeUtils.startOfDayMillis(
            year = year,
            month = month,
            day = day,
        )?.let { dateMillis ->
            changeState {
                copy(
                    syncDialog = syncDialog?.copy(
                        fromMillis = dateMillis,
                        error = null,
                    )
                )
            }
        }
    }

    override fun changeSyncDateTo(
        year: Int,
        month: Int,
        day: Int,
    ) {
        TimeUtils.endOfDayMillis(
            year = year,
            month = month,
            day = day,
        )?.let { dateMillis ->
            changeState {
                copy(
                    syncDialog = syncDialog?.copy(
                        toMillis = dateMillis,
                        error = null,
                    )
                )
            }
        }
    }

    override fun exportSyncBackup() {
        val dialog = state.syncDialog?.takeIf { !it.isBusy } ?: return
        if (dialog.fromMillis > dialog.toMillis) {
            changeState {
                copy(syncDialog = dialog.copy(error = UiText.of(Res.string.main_sync_date_order_invalid)))
            }
            return
        }
        changeState {
            copy(
                syncDialog = dialog.copy(
                    isExporting = true,
                    error = null,
                )
            )
        }
        viewModelScope.launch {
            runCatching {
                val backup = backupRepository.exportUnifiedBackup(
                    fromMillis = dialog.fromMillis,
                    toMillis = dialog.toMillis,
                )
                withContext(Dispatchers.Default) { backupSerializer.encode(backup) }
            }.onSuccess { json ->
                pendingSyncExportJson = json
                changeState {
                    copy(
                        syncDialog = syncDialog?.copy(
                            isExporting = false,
                            exportPromptRequested = true,
                        )
                    )
                }
            }.onFailure {
                changeState {
                    copy(
                        syncDialog = syncDialog?.copy(isExporting = false),
                        syncNotice = UiText.of(Res.string.main_sync_export_error),
                    )
                }
            }
        }
    }

    override fun onSyncExportPromptLaunched() {
        changeState { copy(syncDialog = syncDialog?.copy(exportPromptRequested = false)) }
    }

    override fun onSyncExportToHandle(handle: String) {
        val json = pendingSyncExportJson
        pendingSyncExportJson = null
        if (json == null) {
            return
        }
        changeState { copy(syncDialog = null) }
        viewModelScope.launch {
            val success = textFileAccess.write(
                handle = handle,
                text = json,
            )
            val syncNoticeRes = if (success) Res.string.main_sync_export_success else Res.string.main_sync_export_error
            changeState { copy(syncNotice = UiText.of(resource = syncNoticeRes)) }
        }
    }

    override fun onSyncImportFromHandle(handle: String) {
        val dialog = state.syncDialog
        if (dialog?.isBusy == true) {
            return
        }
        changeState {
            copy(
                syncDialog = syncDialog?.copy(
                    isImporting = true,
                    error = null
                )
            )
        }
        viewModelScope.launch {
            val notice = runCatching {
                val text = textFileAccess.read(handle = handle)
                    ?: throw IllegalStateException("Unable to read backup file")
                backupRepository.import(text = text)
            }.fold(
                onSuccess = { result ->
                    UiText.of(
                        Res.string.main_sync_import_result,
                        result.trainingsImported,
                        result.trainingsSkippedAsDuplicates,
                        result.athletesCreated,
                        result.athletesUpdated,
                        result.weightsImported,
                    )
                },
                onFailure = { error ->
                    UiText.of(
                        if (error is UnsupportedBackupFormatException) {
                            Res.string.main_sync_import_unsupported
                        } else {
                            Res.string.main_sync_import_error
                        }
                    )
                },
            )
            changeState {
                copy(
                    syncDialog = null,
                    syncNotice = notice,
                )
            }
        }
    }

    override fun dismissSyncNotice() {
        changeState { copy(syncNotice = null) }
    }

    companion object {

        private const val DEFAULT_SYNC_DAYS_BACK = 3
    }
}