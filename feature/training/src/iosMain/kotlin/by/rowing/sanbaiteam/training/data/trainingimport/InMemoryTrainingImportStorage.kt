package by.rowing.sanbaiteam.training.data.trainingimport

internal class InMemoryTrainingImportStorage : TrainingImportStorage {

    private val files = mutableMapOf<String, String>()
    private var sequence = 0L

    override fun saveCsv(
        trainingId: Long,
        source: TrainingImportSource,
        serial: String?,
        csvText: String
    ): String {
        val fileName =
            "${source.storageFolder}/${trainingId}_${serial?.ifBlank { null } ?: "unknown"}_${sequence++}.csv"
        files[fileName] = csvText
        return fileName
    }

    override fun readCsv(relativePath: String): String? = files[relativePath]
}
