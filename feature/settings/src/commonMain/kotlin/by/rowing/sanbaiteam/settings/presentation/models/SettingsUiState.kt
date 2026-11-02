package by.rowing.sanbaiteam.settings.presentation.models

internal data class SettingsUiState(
    val isLoading: Boolean = true,
    val items: List<WorkDisplaySettingItem> = emptyList(),
)