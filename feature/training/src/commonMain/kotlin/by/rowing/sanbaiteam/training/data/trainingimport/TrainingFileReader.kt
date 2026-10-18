package by.rowing.sanbaiteam.training.data.trainingimport

internal data class PickedImportFile(
    val bytes: ByteArray,
    val fileName: String?,
)

internal interface TrainingFileReader {

    suspend fun readFiles(handles: List<String>): List<PickedImportFile>
}
