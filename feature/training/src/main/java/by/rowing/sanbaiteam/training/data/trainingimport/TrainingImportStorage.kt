package by.rowing.sanbaiteam.training.data.trainingimport

import android.content.Context
import java.io.File

internal class TrainingImportStorage(private val context: Context) {

    fun saveCsv(
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

    fun readCsv(relativePath: String): String? {
        val file = File(context.filesDir, relativePath)
        return if (file.exists()) file.readText() else null
    }
}