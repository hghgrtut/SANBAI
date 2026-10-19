package by.rowing.sanbaiteam.training.data.trainingimport

internal class FakeTrainingImportStorage : TrainingImportStorage {

    val savedCsv = mutableMapOf<String, String>()
    val deletedPaths = mutableListOf<String>()

    override fun saveCsv(
        trainingId: Long,
        source: TrainingImportSource,
        serial: String?,
        csvText: String
    ): String {
        val relativePath = "${source.storageFolder}/${trainingId}_${serial ?: "unknown"}.csv"
        savedCsv[relativePath] = csvText
        return relativePath
    }

    override fun readCsv(relativePath: String): String? = savedCsv[relativePath]

    override fun deleteCsv(relativePath: String) {
        deletedPaths += relativePath
        savedCsv.remove(relativePath)
    }
}
