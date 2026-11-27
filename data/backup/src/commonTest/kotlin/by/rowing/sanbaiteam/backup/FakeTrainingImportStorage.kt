package by.rowing.sanbaiteam.backup

import by.rowing.sanbaiteam.training.data.trainingimport.TrainingImportSource
import by.rowing.sanbaiteam.training.data.trainingimport.TrainingImportStorage

internal class FakeTrainingImportStorage : TrainingImportStorage {

    val savedFiles = linkedMapOf<String, ByteArray>()

    override fun saveImportFile(
        trainingId: Long,
        source: TrainingImportSource,
        serial: String?,
        fileExtension: String,
        bytes: ByteArray,
    ): String {
        val serialPart = serial?.takeIf { it.isNotBlank() } ?: "unknown"
        val safeExtension = fileExtension.trimStart('.').ifBlank { "fit" }
        val relativePath = "${source.storageFolder}/${trainingId}_${serialPart}.$safeExtension"
        savedFiles[relativePath] = bytes
        return relativePath
    }

    override fun readImportFile(relativePath: String): ByteArray? = savedFiles[relativePath]

    override fun deleteImportFile(relativePath: String) {
        savedFiles.remove(relativePath)
    }
}