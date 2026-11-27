package by.rowing.sanbaiteam.settings.presentation

import androidx.lifecycle.viewModelScope
import by.rowing.sanbaiteam.core.presentation.compose.ComposeNavigator
import by.rowing.sanbaiteam.core.presentation.screen.base.ComposeBaseViewModel
import by.rowing.sanbaiteam.settings.data.model.AppThemeMode
import by.rowing.sanbaiteam.settings.data.repository.AppThemeSettingsRepository
import by.rowing.sanbaiteam.settings.data.repository.WorkDisplaySettingsRepository
import by.rowing.sanbaiteam.settings.presentation.models.SettingsUiState
import by.rowing.sanbaiteam.settings.presentation.models.WorkDisplaySettingItem
import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType
import by.rowing.sanbaiteam.training.data.entity.TrainingWorkDisplayMode
import kotlinx.coroutines.launch

internal class SettingsViewModel(
    private val workDisplayRepository: WorkDisplaySettingsRepository,
    private val themeRepository: AppThemeSettingsRepository,
    private val composeNavigator: ComposeNavigator,
) : ComposeBaseViewModel<SettingsUiState>(initialState = SettingsUiState()), SettingsActions {

    init {
        viewModelScope.launch {
            themeRepository.mode.collect { mode ->
                changeState { copy(themeMode = mode) }
            }
        }
        viewModelScope.launch {
            workDisplayRepository.modes.collect { modes ->
                changeState {
                    copy(
                        isLoading = false,
                        items = TrainingPieceType.entries.map { type ->
                            WorkDisplaySettingItem(
                                pieceType = type,
                                mode = modes.getValue(type),
                            )
                        },
                    )
                }
            }
        }
    }

    override fun onThemeModeChanged(mode: AppThemeMode) {
        themeRepository.setMode(mode = mode)
    }

    override fun onWorkDisplayModeChanged(
        pieceType: TrainingPieceType,
        mode: TrainingWorkDisplayMode,
    ) {
        viewModelScope.launch {
            workDisplayRepository.setMode(
                pieceType = pieceType,
                mode = mode,
            )
        }
    }

    override fun navigateBack() {
        composeNavigator.navigateBack()
    }
}