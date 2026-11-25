package by.rowing.sanbaiteam.training.data.trainingimport

import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic

@AllowDetektPublic
interface TrainingImportStorage {

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