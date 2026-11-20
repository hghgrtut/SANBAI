package by.rowing.sanbaiteam.core.file

import androidx.compose.runtime.Composable
import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic

/**
 * Возвращает действие, открывающее системный диалог сохранения JSON-файла.
 * Принимает предлагаемое имя файла, возвращает выбранный дескриптор.
 */
@AllowDetektPublic
@Composable
expect fun rememberCreateTextFileLauncher(onCreated: (String) -> Unit): (String) -> Unit

/**
 * Возвращает действие, открывающее системный выбор JSON-файла.
 * Выбранный файл передаётся как строковый дескриптор.
 */
@AllowDetektPublic
@Composable
expect fun rememberOpenTextFileLauncher(onOpened: (String) -> Unit): () -> Unit

/**
 * Возвращает действие, открывающее системный выбор нескольких файлов любого типа.
 * Выбранные файлы передаются как строковые дескрипторы.
 */
@AllowDetektPublic
@Composable
expect fun rememberOpenFilesLauncher(onFilesPicked: (List<String>) -> Unit): () -> Unit