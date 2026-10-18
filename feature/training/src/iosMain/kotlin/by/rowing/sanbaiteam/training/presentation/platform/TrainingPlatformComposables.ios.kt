package by.rowing.sanbaiteam.training.presentation.platform

import androidx.compose.runtime.Composable

@Composable
internal actual fun rememberTrainingImportPicker(onFilesPicked: (List<String>) -> Unit): () -> Unit = {}

@Composable
internal actual fun rememberCreateBackupLauncher(onCreated: (String) -> Unit): (String) -> Unit = {}

@Composable
internal actual fun rememberOpenBackupLauncher(onOpened: (String) -> Unit): () -> Unit = {}

@Composable
internal actual fun rememberTrainingDatePicker(
    initialDateMillis: Long,
    onDatePicked: (year: Int, month: Int, dayOfMonth: Int) -> Unit,
): () -> Unit = {}
// TODO: Implement iOS file pickers and date picker.
