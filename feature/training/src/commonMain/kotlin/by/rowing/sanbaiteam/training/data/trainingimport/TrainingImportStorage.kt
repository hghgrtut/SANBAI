package by.rowing.sanbaiteam.training.data.trainingimport

internal interface TrainingImportStorage {

    fun saveImportFile(
        trainingId: Long,
        source: TrainingImportSource,
        serial: String?,
        fileExtension: String,
        bytes: ByteArray
    ): String

    fun readImportFile(relativePath: String): ByteArray?

    fun deleteImportFile(relativePath: String)
}