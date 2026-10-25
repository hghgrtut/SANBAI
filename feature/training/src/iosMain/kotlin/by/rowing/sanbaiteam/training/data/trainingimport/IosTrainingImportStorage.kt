package by.rowing.sanbaiteam.training.data.trainingimport

/**
 * iOS-реализация хранилища импортов. Файлы сохраняются в Documents/<storageFolder>/ и переживают перезапуск приложения.
 */
internal class IosTrainingImportStorage : TrainingImportStorage {

    override fun saveImportFile(
        trainingId: Long,
        source: TrainingImportSource,
        serial: String?,
        fileExtension: String,
        bytes: ByteArray
    ): String {
        val safeExtension = fileExtension.trimStart('.').ifBlank { FIT_FILE_EXTENSION }
        val fileName = "${trainingId}_${serial?.ifBlank { null } ?: "unknown"}_" +
                "${IosTrainingFileManager.currentTimeMillis()}.$safeExtension"
        val relativePath = "${source.storageFolder}/$fileName"
        IosTrainingFileManager.writeBytes(relativePath, bytes)
        return relativePath
    }

    override fun readImportFile(relativePath: String): ByteArray? =
        IosTrainingFileManager.readBytes(relativePath)

    override fun deleteImportFile(relativePath: String) {
        IosTrainingFileManager.deleteFile(relativePath)
    }
}
