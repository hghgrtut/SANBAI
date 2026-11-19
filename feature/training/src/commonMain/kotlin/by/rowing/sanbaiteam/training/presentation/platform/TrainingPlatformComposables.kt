package by.rowing.sanbaiteam.training.presentation.platform

import androidx.compose.runtime.Composable

/**
 * Возвращает действие, открывающее системный выбор файлов тренировки.
 * Выбранные файлы передаются как строковые дескрипторы (на Android — URI).
 */
@Composable
internal expect fun rememberTrainingImportPicker(onFilesPicked: (List<String>) -> Unit): () -> Unit