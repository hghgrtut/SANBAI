package by.rowing.sanbaiteam.uikit.modifier

import androidx.compose.ui.Modifier

internal actual fun Modifier.clearFocusOnKeyboardDismiss(
    onKeyboardDismiss: () -> Unit,
): Modifier = this
