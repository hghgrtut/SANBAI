package by.rowing.sanbaiteam.settings.presentation.models

import by.rowing.sanbaiteam.settings.data.model.AppThemeMode

internal data class SettingsUiState(
    val isLoading: Boolean = true,
    val themeMode: AppThemeMode = AppThemeMode.AUTO,
    val items: List<WorkDisplaySettingItem> = emptyList(),
)