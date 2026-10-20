package by.rowing.sanbaiteam.training.data.trainingimport

import android.content.Context
import java.io.File

internal class AndroidTrainingImportStorage(private val context: Context) : TrainingImportStorage {

    override fun saveCsv(
        trainingId: Long,
        source: TrainingImportSource,
        serial: String?,
        csvText: String
    ): String {
        val dir = File(context.filesDir, source.storageFolder)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val fileName = "${trainingId}_${serial?.ifBlank { null } ?: "unknown"}_${System.currentTimeMillis()}.csv"
        val file = File(dir, fileName)
        file.writeText(csvText)
        return "${source.storageFolder}/$fileName"
    }

    override fun readCsv(relativePath: String): String? {
        val file = File(context.filesDir, relativePath)
        return if (file.exists()) file.readText() else null
    }

    override fun deleteCsv(relativePath: String) {
        File(context.filesDir, relativePath).delete()
    }
}
