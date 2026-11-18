package by.rowing.sanbaiteam.uikit.component

import androidx.compose.runtime.Composable
import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic

/**
 * Возвращает действие, открывающее системный выбор даты.
 * Месяц в [onDatePicked] — от 1 до 12.
 */
@AllowDetektPublic
@Composable
expect fun rememberDatePicker(
    initialDateMillis: Long,
    onDatePicked: (year: Int, month: Int, dayOfMonth: Int) -> Unit,
): () -> Unit