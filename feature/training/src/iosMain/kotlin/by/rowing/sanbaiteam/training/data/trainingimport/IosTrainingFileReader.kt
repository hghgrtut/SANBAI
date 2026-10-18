package by.rowing.sanbaiteam.training.data.trainingimport

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * iOS-реализация чтения файлов тренировок.
 * Принимает абсолютные пути к файлам (временные копии из document picker).
 */
internal class IosTrainingFileReader : TrainingFileReader {

    override suspend fun readFiles(handles: List<String>): List<PickedImportFile> =
        withContext(Dispatchers.Default) {
            handles.mapNotNull { handle ->
                val bytes = IosTrainingFileManager.readAbsoluteBytes(handle) ?: return@mapNotNull null
                PickedImportFile(
                    bytes = bytes,
                    fileName = handle.substringAfterLast('/'),
                )
            }
        }
}
