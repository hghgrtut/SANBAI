package by.rowing.sanbaiteam.training.presentation.platform

import androidx.compose.runtime.Composable

/**
 * Возвращает действие, открывающее системный выбор файлов тренировки.
 * Выбранные файлы передаются как строковые дескрипторы (на Android — URI).
 */
@Composable
internal expect fun rememberTrainingImportPicker(onFilesPicked: (List<String>) -> Unit): () -> Unit

/**
 * Возвращает действие, открывающее системный диалог сохранения резервной копии.
 * Принимает предлагаемое имя файла, возвращает выбранный дескриптор (на Android — URI).
 */
@Composable
internal expect fun rememberCreateBackupLauncher(onCreated: (String) -> Unit): (String) -> Unit

/**
 * Возвращает действие, открывающее системный выбор файла резервной копии.
 * Выбранный файл передаётся как строковый дескриптор (на Android — URI).
 */
@Composable
internal expect fun rememberOpenBackupLauncher(onOpened: (String) -> Unit): () -> Unit

/**
 * Возвращает действие, открывающее системный выбор даты.
 * Месяц в [onDatePicked] — от 1 до 12.
 */
@Composable
internal expect fun rememberTrainingDatePicker(
    initialDateMillis: Long,
    onDatePicked: (year: Int, month: Int, dayOfMonth: Int) -> Unit,
): () -> Unit
