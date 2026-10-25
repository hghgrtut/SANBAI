package by.rowing.sanbaiteam.training.data.trainingimport

internal class FakeTrainingImportStorage : TrainingImportStorage {

    val savedFiles = mutableMapOf<String, ByteArray>()
    val deletedPaths = mutableListOf<String>()

    override fun saveImportFile(
        trainingId: Long,
        source: TrainingImportSource,
        serial: String?,
        fileExtension: String,
        bytes: ByteArray
    ): String {
        val relativePath = "${source.storageFolder}/${trainingId}_${serial ?: "unknown"}.$fileExtension"
        savedFiles[relativePath] = bytes
        return relativePath
    }

    override fun readImportFile(relativePath: String): ByteArray? = savedFiles[relativePath]

    override fun deleteImportFile(relativePath: String) {
        deletedPaths += relativePath
        savedFiles.remove(relativePath)
    }
}
