package by.rowing.sanbaiteam.training.data.trainingimport

internal interface TrainingImportStorage {

    fun saveCsv(
        trainingId: Long,
        source: TrainingImportSource,
        serial: String?,
        csvText: String
    ): String

    fun readCsv(relativePath: String): String?

    fun deleteCsv(relativePath: String)
}
