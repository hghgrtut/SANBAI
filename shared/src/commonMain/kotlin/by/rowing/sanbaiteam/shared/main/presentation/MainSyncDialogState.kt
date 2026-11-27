package by.rowing.sanbaiteam.shared.main.presentation

import by.rowing.sanbaiteam.uikit.component.UiText

internal data class MainSyncDialogState(
    val fromMillis: Long,
    val toMillis: Long,
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val exportPromptRequested: Boolean = false,
    val error: UiText? = null,
) {

    val isBusy: Boolean get() = isExporting || isImporting || exportPromptRequested
}