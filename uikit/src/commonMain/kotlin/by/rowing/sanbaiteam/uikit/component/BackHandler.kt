package by.rowing.sanbaiteam.uikit.component

import androidx.compose.runtime.Composable
import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic

/**
 * Перехватывает системное событие "Назад".
 * На Android делегирует в [androidx.activity.compose.BackHandler], на iOS — no-op.
 */
@AllowDetektPublic
@Composable
expect fun BackHandler(enabled: Boolean = true, onBack: () -> Unit)
