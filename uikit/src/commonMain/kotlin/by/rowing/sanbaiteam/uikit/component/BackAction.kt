package by.rowing.sanbaiteam.uikit.component

import androidx.compose.runtime.Composable

/**
 * Возвращает действие системного "Назад" по умолчанию.
 * На Android — делегирует в [androidx.activity.OnBackPressedDispatcher], на iOS — no-op.
 */
@Composable
internal expect fun defaultBackAction(): () -> Unit
