package by.rowing.sanbaiteam.training.data.trainingimport

/**
 * iOS-реализация хранилища CSV-импортов.
 * Файлы сохраняются в Documents/<storageFolder>/ и переживают перезапуск приложения.
 */
internal class IosTrainingImportStorage : TrainingImportStorage {

    override fun saveCsv(
        trainingId: Long,
        source: TrainingImportSource,
        serial: String?,
        csvText: String
    ): String {
        val fileName = "${trainingId}_${serial?.ifBlank { null } ?: "unknown"}_${IosTrainingFileManager.currentTimeMillis()}.csv"
        val relativePath = "${source.storageFolder}/$fileName"
        IosTrainingFileManager.writeText(relativePath, csvText)
        return relativePath
    }

    override fun readCsv(relativePath: String): String? =
        IosTrainingFileManager.readText(relativePath)

    override fun deleteCsv(relativePath: String) {
        IosTrainingFileManager.deleteFile(relativePath)
    }
}
