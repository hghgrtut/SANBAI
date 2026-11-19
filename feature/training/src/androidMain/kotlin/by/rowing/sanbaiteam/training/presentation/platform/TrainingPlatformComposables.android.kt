package by.rowing.sanbaiteam.training.presentation.platform

import androidx.compose.runtime.Composable
import by.rowing.sanbaiteam.core.file.rememberOpenFilesLauncher

@Composable
internal actual fun rememberTrainingImportPicker(onFilesPicked: (List<String>) -> Unit): () -> Unit =
    rememberOpenFilesLauncher(onFilesPicked = onFilesPicked)