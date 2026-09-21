package by.rowing.sanbaiteam.training.presentation.list

import android.content.Context
import android.net.Uri
import androidx.lifecycle.viewModelScope
import by.rowing.sanbaiteam.core.presentation.compose.ComposeNavigator
import by.rowing.sanbaiteam.core.presentation.compose.getReturnToScreenNavOptions
import by.rowing.sanbaiteam.core.presentation.screen.base.ComposeBaseViewModel
import by.rowing.sanbaiteam.core.util.AndroidResourceUtils
import by.rowing.sanbaiteam.training.R
import by.rowing.sanbaiteam.training.data.models.TrainingTransferResult
import by.rowing.sanbaiteam.training.data.repository.TrainingRepository
import by.rowing.sanbaiteam.training.data.trainingtransfer.TrainingBackupSerializer
import by.rowing.sanbaiteam.training.data.trainingtransfer.TrainingTransferRepository
import by.rowing.sanbaiteam.training.presentation.list.compose.ListTrainingScreenActions
import by.rowing.sanbaiteam.training.presentation.list.models.ListTrainingState
import by.rowing.sanbaiteam.training.presentation.navigation.TrainingNavigation
import kotlinx.coroutines.launch

internal class ListTrainingViewModel(
    private val repository: TrainingRepository,
    private val trainingTransferRepository: TrainingTransferRepository,
    private val resourceUtils: AndroidResourceUtils,
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
        composeNavigator.navigateBack()
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
        TrainingNavigation.TrainingDetail(trainingId).navigateTo(
            composeNavigator = composeNavigator,
            navOptions = getReturnToScreenNavOptions<TrainingNavigation.TrainingList>()
        )
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
                            transferNotice = resourceUtils.getString(R.string.training_transfer_export_error)
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

    override fun onExportToUri(
        uri: Uri,
        context: Context
    ) {
        val json = pendingExportJson
        pendingExportJson = null
        json ?: return
        viewModelScope.launch {
            val success = runCatching {
                context.contentResolver.openOutputStream(uri)?.use { stream -> stream.write(json.toByteArray()) }
                    ?: throw IllegalStateException("Unable to open output stream")
            }.isSuccess
            val noticeResId: Int =
                if (success) R.string.training_transfer_export_success else R.string.training_transfer_export_error
            changeState { copy(transferNotice = resourceUtils.getString(id = noticeResId)) }
        }
    }

    override fun onImportFromUri(
        uri: Uri,
        context: Context
    ) {
        if (state.isTransferring) return
        changeState { copy(isTransferring = true) }
        viewModelScope.launch {
            val result = runCatching {
                context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            }.getOrNull()?.let { jsonText ->
                runCatching { trainingTransferRepository.importBackup(TrainingBackupSerializer.decode(text = jsonText)) }
                    .getOrNull()
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

    private fun buildImportNotice(result: TrainingTransferResult?): String = result?.let {
        resourceUtils.getString(
            R.string.training_transfer_import_result,
            result.trainingsImported,
            result.athletesCreated
        )
    } ?: resourceUtils.getString(R.string.training_transfer_import_error)
}