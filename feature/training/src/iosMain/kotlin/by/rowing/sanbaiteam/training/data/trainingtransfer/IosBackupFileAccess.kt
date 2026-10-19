package by.rowing.sanbaiteam.training.data.trainingtransfer

import by.rowing.sanbaiteam.training.data.trainingimport.IosTrainingFileManager

/**
 * iOS-реализация доступа к файлам бэкапа.
 * Работает с файлами по абсолютным путям (временные копии из document picker и для Share Sheet).
 */
internal class IosBackupFileAccess : BackupFileAccess {

    override suspend fun write(
        handle: String,
        text: String
    ): Boolean {
        return runCatching {
            IosTrainingFileManager.writeAbsoluteText(handle, text)
        }.getOrDefault(false)
    }

    override suspend fun read(handle: String): String? {
        return runCatching {
            IosTrainingFileManager.readAbsoluteText(handle)
        }.getOrNull()
    }
}
