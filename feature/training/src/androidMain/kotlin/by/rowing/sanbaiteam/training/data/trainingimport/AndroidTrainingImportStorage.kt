package by.rowing.sanbaiteam.training.data.trainingimport

import android.content.Context
import java.io.File

internal class AndroidTrainingImportStorage(private val context: Context) : TrainingImportStorage {

    override fun saveImportFile(
        trainingId: Long,
        source: TrainingImportSource,
        serial: String?,
        fileExtension: String,
        bytes: ByteArray
    ): String {
        val dir = File(context.filesDir, source.storageFolder)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val safeExtension = fileExtension.trimStart('.').ifBlank { FIT_FILE_EXTENSION }
        val deviceSerial = serial?.ifBlank { null } ?: "unknown"
        val fileName = "${trainingId}_${deviceSerial}_${System.currentTimeMillis()}.$safeExtension"
        val file = File(dir, fileName)
        file.writeBytes(bytes)
        return "${source.storageFolder}/$fileName"
    }

    override fun readImportFile(relativePath: String): ByteArray? =
        File(context.filesDir, relativePath).takeIf { file -> file.exists() }?.readBytes()

    override fun deleteImportFile(relativePath: String) {
        File(context.filesDir, relativePath).delete()
    }
}
